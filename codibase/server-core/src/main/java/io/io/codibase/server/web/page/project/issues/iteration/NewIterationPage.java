package io.codibase.server.web.page.project.issues.iteration;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.codibase.server.CodiBase;
import io.codibase.server.data.migration.VersionedXmlDoc;
import io.codibase.server.service.IterationService;
import io.codibase.server.model.Iteration;
import io.codibase.server.model.Project;
import io.codibase.server.security.SecurityUtils;
import io.codibase.server.web.component.link.ViewStateAwarePageLink;
import io.codibase.server.web.editable.BeanContext;
import io.codibase.server.web.editable.BeanEditor;
import io.codibase.server.web.page.project.ProjectPage;
import io.codibase.server.web.page.project.dashboard.ProjectDashboardPage;
import io.codibase.server.web.util.editbean.IterationEditBean;

public class NewIterationPage extends ProjectPage {

	public NewIterationPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		var bean = IterationEditBean.ofNew(getProject(), "");
		BeanEditor editor = BeanContext.edit("editor", bean);
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();

				var iteration = new Iteration();
				iteration.setProject(getProject());
				bean.update(iteration);
				CodiBase.getInstance(IterationService.class).createOrUpdate(iteration);
				var newAuditContent = VersionedXmlDoc.fromBean(iteration).toXML();
				auditService.audit(getProject(), "created iteration \"" + iteration.getName() + "\"", null, newAuditContent);
				Session.get().success("New iteration created");
				setResponsePage(IterationIssuesPage.class, IterationIssuesPage.paramsOf(getProject(), iteration, null));
			}
			
		};
		form.add(editor);
		add(form);
	}

	public static PageParameters paramsOf(Project project) {
		return ProjectPage.paramsOf(project);
	}
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canManageIssues(getProject());
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, "Create Iteration");
	}

	@Override
	protected BookmarkablePageLink<Void> navToProject(String componentId, Project project) {
		if (project.isIssueManagement()) 
			return new ViewStateAwarePageLink<Void>(componentId, IterationListPage.class, IterationListPage.paramsOf(project, false, null));
		else
			return new ViewStateAwarePageLink<Void>(componentId, ProjectDashboardPage.class, ProjectDashboardPage.paramsOf(project));
	}
	
}
