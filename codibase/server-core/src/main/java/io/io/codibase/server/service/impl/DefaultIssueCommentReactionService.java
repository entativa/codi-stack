package io.codibase.server.service.impl;

import javax.inject.Singleton;

import com.google.common.base.Preconditions;

import io.codibase.server.model.IssueComment;
import io.codibase.server.model.IssueCommentReaction;
import io.codibase.server.model.User;
import io.codibase.server.persistence.annotation.Transactional;
import io.codibase.server.service.IssueCommentReactionService;

@Singleton
public class DefaultIssueCommentReactionService extends BaseEntityService<IssueCommentReaction>
        implements IssueCommentReactionService {

    @Transactional
    @Override
    public void create(IssueCommentReaction reaction) {
        Preconditions.checkState(reaction.isNew());
        dao.persist(reaction);
    }

    @Transactional
    @Override
    public void toggleEmoji(User user, IssueComment comment, String emoji) {
        var reaction = comment.getReactions().stream()
                .filter(r -> r.getUser().equals(user) && r.getEmoji().equals(emoji))
                .findFirst()
                .orElse(null);
        if (reaction == null) {
            reaction = new IssueCommentReaction();
            reaction.setComment(comment);
            reaction.setUser(user);
            reaction.setEmoji(emoji);
            create(reaction);
            comment.getReactions().add(reaction);
        } else {
            comment.getReactions().remove(reaction);
            dao.remove(reaction);
        }
    }

} 