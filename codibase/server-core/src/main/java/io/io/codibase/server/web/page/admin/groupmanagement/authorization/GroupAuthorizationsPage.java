package io.codibase.server.web.page.admin.groupmanagement.authorization;

import static io.codibase.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Session;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.codibase.server.CodiBase;
import io.codibase.server.data.migration.VersionedXmlDoc;
import io.codibase.server.service.GroupAuthorizationService;
import io.codibase.server.service.ProjectService;
import io.codibase.server.service.RoleService;
import io.codibase.server.model.GroupAuthorization;
import io.codibase.server.web.editable.PropertyContext;
import io.codibase.server.web.page.admin.groupmanagement.GroupPage;
import io.codibase.server.web.util.editbean.ProjectAuthorizationBean;
import io.codibase.server.web.util.editbean.ProjectAuthorizationsBean;

public class GroupAuthorizationsPage extends GroupPage {

	private String oldAuditContent;

	public GroupAuthorizationsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		ProjectAuthorizationsBean authorizationsBean = new ProjectAuthorizationsBean();
		var projectRoles = new HashMap<String, List<String>>();		
		for (var authorization: getGroup().getAuthorizations()) {
			String projectPath = authorization.getProject().getPath();
			String roleName = authorization.getRole().getName();			
			projectRoles.computeIfAbsent(projectPath, k -> new ArrayList<>()).add(roleName);
		}
		for (var entry: projectRoles.entrySet()) {
			ProjectAuthorizationBean authorizationBean = new ProjectAuthorizationBean();
			authorizationBean.setProjectPath(entry.getKey());
			authorizationBean.setRoleNames(entry.getValue());
			authorizationsBean.getAuthorizations().add(authorizationBean);
		}
		oldAuditContent = VersionedXmlDoc.fromBean(authorizationsBean).toXML();

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();

				Set<String> projectPaths = new HashSet<>();
				Collection<GroupAuthorization> authorizations = new ArrayList<>();
				for (ProjectAuthorizationBean authorizationBean: authorizationsBean.getAuthorizations()) {
					if (!projectPaths.add(authorizationBean.getProjectPath())) {
						error(MessageFormat.format(_T("Duplicate authorizations found: {0}"), authorizationBean.getProjectPath()));
						return;
					} else {
						var project = getProjectService().findByPath(authorizationBean.getProjectPath());
						authorizationBean.getRoleNames().stream().forEach(it -> {
							GroupAuthorization authorization = new GroupAuthorization();
							authorization.setGroup(getGroup());
							authorization.setProject(project);
							authorization.setRole(getRoleService().find(it));
							authorizations.add(authorization);
						});
					}
				}
				
				var newAuditContent = VersionedXmlDoc.fromBean(authorizationsBean).toXML();
				getGroupAuthorizationService().syncAuthorizations(getGroup(), authorizations);
				auditService.audit(null, "changed authorizations of group \"" + getGroup().getName() + "\"", oldAuditContent, newAuditContent);
				oldAuditContent = newAuditContent;
				Session.get().success("Project authorizations updated");
			}
			
		};
		form.add(new FencedFeedbackPanel("feedback", form));
		form.add(PropertyContext.edit("editor", authorizationsBean, "authorizations"));
		add(form);
	}

	private RoleService getRoleService() {
		return CodiBase.getInstance(RoleService.class);
	}

	private ProjectService getProjectService() {
		return CodiBase.getInstance(ProjectService.class);
	}
	
	private GroupAuthorizationService getGroupAuthorizationService() {
		return CodiBase.getInstance(GroupAuthorizationService.class);
	}

}
