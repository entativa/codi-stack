package io.codibase.server.web.page.project.issues.create;

import static io.codibase.server.web.translation.Translation._T;

import java.util.List;

import org.jspecify.annotations.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.codibase.commons.utils.ExplicitException;
import io.codibase.server.CodiBase;
import io.codibase.server.buildspecmodel.inputspec.InputContext;
import io.codibase.server.buildspecmodel.inputspec.InputSpec;
import io.codibase.server.service.IssueService;
import io.codibase.server.service.SettingService;
import io.codibase.server.model.Issue;
import io.codibase.server.model.Project;
import io.codibase.server.model.User;
import io.codibase.server.model.support.administration.GlobalIssueSetting;
import io.codibase.server.search.entity.issue.IssueQuery;
import io.codibase.server.search.entity.issue.IssueQueryParseOption;
import io.codibase.server.util.criteria.Criteria;
import io.codibase.server.web.component.issue.create.NewIssueEditor;
import io.codibase.server.web.component.issue.workflowreconcile.WorkflowChangeAlertPanel;
import io.codibase.server.web.component.link.ViewStateAwarePageLink;
import io.codibase.server.web.page.project.ProjectPage;
import io.codibase.server.web.page.project.dashboard.ProjectDashboardPage;
import io.codibase.server.web.page.project.issues.detail.IssueActivitiesPage;
import io.codibase.server.web.page.project.issues.list.ProjectIssueListPage;
import io.codibase.server.web.page.security.LoginPage;

public class NewIssuePage extends ProjectPage implements InputContext {

	private static final String PARAM_TEMPLATE = "template";
	
	private IModel<Criteria<Issue>> templateModel;
	
	public NewIssuePage(PageParameters params) {
		super(params);
		
		if (!getProject().isIssueManagement())
			throw new ExplicitException(_T("Issue management not enabled in this project"));
		
		User currentUser = getLoginUser();
		if (currentUser == null)
			throw new RestartResponseAtInterceptPageException(LoginPage.class);
		
		String queryString = params.get(PARAM_TEMPLATE).toString();
		templateModel = new LoadableDetachableModel<Criteria<Issue>>() {

			@Override
			protected Criteria<Issue> load() {
				try {
					IssueQueryParseOption option = new IssueQueryParseOption().withCurrentUserCriteria(true);
					return IssueQuery.parse(getProject(), queryString, option, true).getCriteria();
				} catch (Exception e) {
					return null;
				}
			}
			
		};
	}

	@Override
	protected void onDetach() {
		templateModel.detach();
		super.onDetach();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new WorkflowChangeAlertPanel("workflowChangeAlert") {

			@Override
			protected void onCompleted(AjaxRequestTarget target) {
				setResponsePage(getPageClass(), getPageParameters());
			}
			
		});
		NewIssueEditor editor = new NewIssueEditor("newIssue") {

			@Override
			protected Project getProject() {
				return NewIssuePage.this.getProject();
			}

			@Override
			protected Criteria<Issue> getTemplate() {
				return templateModel.getObject();
			}
			
		};		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				Issue issue = editor.getConvertedInput();
				CodiBase.getInstance(IssueService.class).open(issue);
				setResponsePage(IssueActivitiesPage.class, IssueActivitiesPage.paramsOf(issue));
			}
			
		};
		
		form.add(editor);
		
		add(form);
	}

	private GlobalIssueSetting getIssueSetting() {
		return CodiBase.getInstance(SettingService.class).getIssueSetting();
	}
	
	@Override
	public List<String> getInputNames() {
		return getIssueSetting().getFieldNames();
	}

	@Override
	public InputSpec getInputSpec(String inputName) {
		return getIssueSetting().getFieldSpec(inputName);
	}

	public static PageParameters paramsOf(Project project, @Nullable String template) {
		PageParameters params = paramsOf(project);
		if (template != null)
			params.add(PARAM_TEMPLATE, template);
		return params;
	}
	
	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, "<span class='text-nowrap'>" + _T("Create Issue") + "</span>").setEscapeModelStrings(false);
	}
	
	@Override
	protected BookmarkablePageLink<Void> navToProject(String componentId, Project project) {
		if (project.isIssueManagement()) 
			return new ViewStateAwarePageLink<Void>(componentId, ProjectIssueListPage.class, ProjectIssueListPage.paramsOf(project, 0));
		else
			return new ViewStateAwarePageLink<Void>(componentId, ProjectDashboardPage.class, ProjectDashboardPage.paramsOf(project.getId()));
	}
	
}
