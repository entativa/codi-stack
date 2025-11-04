package io.codibase.server.service;

import io.codibase.server.model.PullRequestComment;
import io.codibase.server.model.PullRequestCommentReaction;
import io.codibase.server.model.User;

public interface PullRequestCommentReactionService extends EntityService<PullRequestCommentReaction> {

    void create(PullRequestCommentReaction reaction);

    void toggleEmoji(User user, PullRequestComment comment, String emoji);

} 