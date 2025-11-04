package io.codibase.server.service;

import io.codibase.server.model.PullRequest;
import io.codibase.server.model.PullRequestMention;
import io.codibase.server.model.User;

public interface PullRequestMentionService extends EntityService<PullRequestMention> {

	void mention(PullRequest request, User user);

}
