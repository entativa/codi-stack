package io.codibase.server.service.impl;

import javax.inject.Singleton;

import io.codibase.server.model.PullRequest;
import io.codibase.server.model.PullRequestMention;
import io.codibase.server.model.User;
import io.codibase.server.service.PullRequestMentionService;

@Singleton
public class DefaultPullRequestMentionService extends BaseEntityService<PullRequestMention>
		implements PullRequestMentionService {

	@Override
	public void mention(PullRequest request, User user) {
		if (request.getMentions().stream().noneMatch(it->it.getUser().equals(user))) {
			PullRequestMention mention = new PullRequestMention();
			mention.setRequest(request);
			mention.setUser(user);
			dao.persist(mention);
		}
	}

}
