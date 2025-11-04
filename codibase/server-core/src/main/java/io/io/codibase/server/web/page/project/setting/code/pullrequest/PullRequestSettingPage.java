package io.codibase.server.web.page.project.setting.code.pullrequest;

import static io.codibase.server.web.translation.Translation._T;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.codibase.server.CodiBase;
import io.codibase.server.data.migration.VersionedXmlDoc;
import io.codibase.server.service.ProjectService;
import io.codibase.server.model.Project;
import io.codibase.server.model.support.pullrequest.ProjectPullRequestSetting;
import io.codibase.server.security.SecurityUtils;
import io.codibase.server.web.component.link.ViewStateAwarePageLink;
import io.codibase.server.web.editable.BeanContext;
import io.codibase.server.web.page.project.ProjectPage;
import io.codibase.server.web.page.project.dashboard.ProjectDashboardPage;
import io.codibase.server.web.page.project.setting.ProjectSettingPage;

public class PullRequestSettingPage extends ProjectSettingPage {

	public PullRequestSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		ProjectPullRequestSetting bean = getProject().getPullRequestSetting();
		var oldAuditContent = VersionedXmlDoc.fromBean(bean).toXML();
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();	
				var newAuditContent = VersionedXmlDoc.fromBean(bean).toXML();
				getProject().setPullRequestSetting(bean);
				CodiBase.getInstance(ProjectService.class).update(getProject());
				auditService.audit(getProject(), "changed pull request settings", oldAuditContent, newAuditContent);
				setResponsePage(PullRequestSettingPage.class, PullRequestSettingPage.paramsOf(getProject()));
				Session.get().success(_T("Pull request settings updated"));
			}
			
		};
		form.add(BeanContext.edit("editor", bean));
		
		add(form);
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, "<span class='text-truncate'>" + _T("Pull Request Settings") + "</span>").setEscapeModelStrings(false);
	}

	@Override
	protected BookmarkablePageLink<Void> navToProject(String componentId, Project project) {
		if (SecurityUtils.canManageProject(project)) 
			return new ViewStateAwarePageLink<Void>(componentId, PullRequestSettingPage.class, paramsOf(project.getId()));
		else 
			return new ViewStateAwarePageLink<Void>(componentId, ProjectDashboardPage.class, ProjectPage.paramsOf(project.getId()));
	}
	
}
