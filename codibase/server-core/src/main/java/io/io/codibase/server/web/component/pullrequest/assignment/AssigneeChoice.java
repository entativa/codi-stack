package io.codibase.server.web.component.pullrequest.assignment;

import io.codibase.server.web.page.base.BasePage;

import static io.codibase.server.web.translation.Translation._T;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.codibase.server.CodiBase;
import io.codibase.server.service.PullRequestAssignmentService;
import io.codibase.server.model.PullRequest;
import io.codibase.server.model.PullRequestAssignment;
import io.codibase.server.model.User;
import io.codibase.server.web.component.select2.SelectToActChoice;
import io.codibase.server.web.component.user.choice.UserChoiceResourceReference;

public abstract class AssigneeChoice extends SelectToActChoice<User> {

	public AssigneeChoice(String id) {
		super(id);
		
		setProvider(new AssigneeProvider() {

			@Override
			protected PullRequest getPullRequest() {
				return AssigneeChoice.this.getPullRequest();
			}
			
		});
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		getSettings().setPlaceholder(_T("Add assignee..."));
		getSettings().setFormatResult("codibase.server.userChoiceFormatter.formatResult");
		getSettings().setFormatSelection("codibase.server.userChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("codibase.server.userChoiceFormatter.escapeMarkup");
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new UserChoiceResourceReference()));
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, User user) {
		PullRequestAssignment assignment = new PullRequestAssignment();
		assignment.setRequest(getPullRequest());
		assignment.setUser(user);

		if (!getPullRequest().isNew()) {
			CodiBase.getInstance(PullRequestAssignmentService.class).create(assignment);
			((BasePage)getPage()).notifyObservableChange(target,
					PullRequest.getChangeObservable(getPullRequest().getId()));
		} else {
			getPullRequest().getAssignments().add(assignment);
		}
	};
	
	protected abstract PullRequest getPullRequest();
}
