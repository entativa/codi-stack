package io.codibase.server.web.component.pullrequest.review;

import static io.codibase.server.web.translation.Translation._T;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.codibase.server.CodiBase;
import io.codibase.server.model.PullRequest;
import io.codibase.server.model.PullRequestReview;
import io.codibase.server.model.PullRequestReview.Status;
import io.codibase.server.model.User;
import io.codibase.server.security.SecurityUtils;
import io.codibase.server.service.PullRequestReviewService;
import io.codibase.server.web.component.select2.SelectToActChoice;
import io.codibase.server.web.component.user.choice.UserChoiceResourceReference;
import io.codibase.server.web.page.base.BasePage;

public abstract class ReviewerChoice extends SelectToActChoice<User> {

	public ReviewerChoice(String id) {
		super(id);
		
		setProvider(new ReviewerProvider() {

			@Override
			protected PullRequest getPullRequest() {
				return ReviewerChoice.this.getPullRequest();
			}
			
		});
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		getSettings().setPlaceholder(_T("Add reviewer..."));
		getSettings().setFormatResult("codibase.server.userChoiceFormatter.formatResult");
		getSettings().setFormatSelection("codibase.server.userChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("codibase.server.userChoiceFormatter.escapeMarkup");
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new UserChoiceResourceReference()));
	}

	protected abstract PullRequest getPullRequest();

	@Override
	protected void onSelect(AjaxRequestTarget target, User user) {
		PullRequestReview review = getPullRequest().getReview(user);
		if (review == null) {
			review = new PullRequestReview();
			review.setRequest(getPullRequest());
			review.setUser(user);
			getPullRequest().getReviews().add(review);
		} else {
			review.setStatus(Status.PENDING);
		}

		if (!getPullRequest().isNew()) {
			CodiBase.getInstance(PullRequestReviewService.class).createOrUpdate(SecurityUtils.getUser(), review);
			((BasePage)getPage()).notifyObservableChange(target,
					PullRequest.getChangeObservable(getPullRequest().getId()));
		}
	};
	
}
