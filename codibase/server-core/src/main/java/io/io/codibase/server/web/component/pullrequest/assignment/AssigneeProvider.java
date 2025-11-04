package io.codibase.server.web.component.pullrequest.assignment;

import io.codibase.server.CodiBase;
import io.codibase.server.service.UserService;
import io.codibase.server.model.PullRequest;
import io.codibase.server.model.User;
import io.codibase.server.security.SecurityUtils;
import io.codibase.server.security.permission.WriteCode;
import io.codibase.server.util.Similarities;
import io.codibase.server.util.facade.UserCache;
import io.codibase.server.web.WebConstants;
import io.codibase.server.web.component.select2.Response;
import io.codibase.server.web.component.select2.ResponseFiller;
import io.codibase.server.web.component.user.choice.AbstractUserChoiceProvider;

import java.util.ArrayList;
import java.util.List;

public abstract class AssigneeProvider extends AbstractUserChoiceProvider {

	private static final long serialVersionUID = 1L;

	@Override
	public void query(String term, int page, Response<User> response) {
		PullRequest request = getPullRequest();
		UserService userService = CodiBase.getInstance(UserService.class);

		List<User> users = new ArrayList<>(SecurityUtils.getAuthorizedUsers(request.getProject(), new WriteCode()));
		
		users.removeAll(request.getAssignees());
		
		UserCache cache = userService.cloneCache();
		users.sort(cache.comparingDisplayName(request.getParticipants()));

		new ResponseFiller<>(response).fill(new Similarities<>(users) {

			private static final long serialVersionUID = 1L;

			@Override
			public double getSimilarScore(User object) {
				return cache.getSimilarScore(object, term);
			}

		}, page, WebConstants.PAGE_SIZE);
	}

	protected abstract PullRequest getPullRequest();
	
}