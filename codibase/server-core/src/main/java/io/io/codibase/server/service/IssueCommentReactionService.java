package io.codibase.server.service;

import io.codibase.server.model.IssueComment;
import io.codibase.server.model.IssueCommentReaction;
import io.codibase.server.model.User;

public interface IssueCommentReactionService extends EntityService<IssueCommentReaction> {

    void create(IssueCommentReaction reaction);

    void toggleEmoji(User user, IssueComment comment, String emoji);

} 