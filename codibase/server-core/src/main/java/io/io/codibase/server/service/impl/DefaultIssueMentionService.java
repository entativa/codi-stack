package io.codibase.server.service.impl;

import javax.inject.Singleton;

import io.codibase.server.model.Issue;
import io.codibase.server.model.IssueMention;
import io.codibase.server.model.User;
import io.codibase.server.service.IssueMentionService;

@Singleton
public class DefaultIssueMentionService extends BaseEntityService<IssueMention>
		implements IssueMentionService {

	@Override
	public void mention(Issue issue, User user) {
		if (issue.getMentions().stream().noneMatch(it->it.getUser().equals(user))) {
			IssueMention mention = new IssueMention();
			mention.setIssue(issue);
			mention.setUser(user);
			dao.persist(mention);
		}
	}

}
