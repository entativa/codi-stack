package io.codibase.server.web.page.project.issues.iteration;

import static io.codibase.server.web.translation.Translation._T;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
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

public class IterationEditPage extends ProjectPage {

	private static final String PARAM_ITERATION = "iteration";
	
	private final IModel<Iteration> iterationIModel;
	
	public IterationEditPage(PageParameters params) {
		super(params);
		
		Long iterationId = params.get(PARAM_ITERATION).toLong();
		iterationIModel = new LoadableDetachableModel<Iteration>() {

			@Override
			protected Iteration load() {
				return getIterationService().load(iterationId);
			}
			
		};
	}

	private Iteration getIteration() {
		return iterationIModel.getObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		var bean = IterationEditBean.of(getIteration(), "");
		BeanEditor editor = BeanContext.edit("editor", bean);
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				var oldAuditContent = VersionedXmlDoc.fromBean(getIteration()).toXML();
				bean.update(getIteration());
				var newAuditContent = VersionedXmlDoc.fromBean(getIteration()).toXML();
				getIterationService().createOrUpdate(getIteration());
				auditService.audit(getIteration().getProject(), "changed iteration \"" + getIteration().getName() + "\"", oldAuditContent, newAuditContent);
				Session.get().success(_T("Iteration saved"));
				setResponsePage(IterationIssuesPage.class, 
						IterationIssuesPage.paramsOf(getIteration().getProject(), getIteration(), null));
				
			}
			
		};
		form.add(editor);
		add(form);
	}
	
	@Override
	protected void onDetach() {
		iterationIModel.detach();
		super.onDetach();
	}

	public static PageParameters paramsOf(Iteration iteration) {
		PageParameters params = paramsOf(iteration.getProject());
		params.add(PARAM_ITERATION, iteration.getId());
		return params;
	}
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canManageIssues(getProject());
	}

	private IterationService getIterationService() {
		return CodiBase.getInstance(IterationService.class);
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		Fragment fragment = new Fragment(componentId, "projectTitleFrag", this);
		fragment.add(new BookmarkablePageLink<Void>("iterations", IterationListPage.class, 
				IterationListPage.paramsOf(getProject())));
		Link<Void> link = new BookmarkablePageLink<Void>("iteration", IterationIssuesPage.class, 
				IterationIssuesPage.paramsOf(getIteration().getProject(), getIteration(), null));
		link.add(new Label("name", getIteration().getName()));
		fragment.add(link);
		return fragment;
	}
	
	@Override
	protected BookmarkablePageLink<Void> navToProject(String componentId, Project project) {
		if (project.isIssueManagement()) 
			return new ViewStateAwarePageLink<Void>(componentId, IterationListPage.class, IterationListPage.paramsOf(project, false, null));
		else
			return new ViewStateAwarePageLink<Void>(componentId, ProjectDashboardPage.class, ProjectDashboardPage.paramsOf(project));
	}
	
}
