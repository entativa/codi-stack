package io.codibase.server.service;

import io.codibase.server.model.IssueReaction;
import io.codibase.server.model.Issue;
import io.codibase.server.model.User;

public interface IssueReactionService extends EntityService<IssueReaction> {

    void create(IssueReaction reaction);
    
    void toggleEmoji(User user, Issue issue, String emoji);

}
