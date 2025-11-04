package io.codibase.server.service;

import io.codibase.server.model.PullRequest;
import io.codibase.server.model.PullRequestReaction;
import io.codibase.server.model.User;

public interface PullRequestReactionService extends EntityService<PullRequestReaction> {

    void create(PullRequestReaction reaction);

    void toggleEmoji(User user, PullRequest request, String emoji);

} 