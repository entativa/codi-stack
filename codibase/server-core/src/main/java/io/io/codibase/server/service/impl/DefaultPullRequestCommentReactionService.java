package io.codibase.server.service.impl;

import javax.inject.Singleton;

import com.google.common.base.Preconditions;

import io.codibase.server.model.PullRequestComment;
import io.codibase.server.model.PullRequestCommentReaction;
import io.codibase.server.model.User;
import io.codibase.server.persistence.annotation.Transactional;
import io.codibase.server.service.PullRequestCommentReactionService;

@Singleton
public class DefaultPullRequestCommentReactionService extends BaseEntityService<PullRequestCommentReaction>
        implements PullRequestCommentReactionService {

    @Transactional
    @Override
    public void create(PullRequestCommentReaction reaction) {
        Preconditions.checkState(reaction.isNew());
        dao.persist(reaction);
    }

    @Transactional
    @Override
    public void toggleEmoji(User user, PullRequestComment comment, String emoji) {
        var reaction = comment.getReactions().stream()
                .filter(r -> r.getUser().equals(user) && r.getEmoji().equals(emoji))
                .findFirst()
                .orElse(null);
        if (reaction == null) {
            reaction = new PullRequestCommentReaction();
            reaction.setUser(user);
            reaction.setComment(comment);
            reaction.setEmoji(emoji);
            create(reaction);
            comment.getReactions().add(reaction);
        } else {
            comment.getReactions().remove(reaction);
            dao.remove(reaction);
        }
    }

} 