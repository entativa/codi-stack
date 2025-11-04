package io.codibase.server.service;

import io.codibase.server.model.Issue;
import io.codibase.server.model.IssueMention;
import io.codibase.server.model.User;

public interface IssueMentionService extends EntityService<IssueMention> {

	void mention(Issue issue, User user);

}
