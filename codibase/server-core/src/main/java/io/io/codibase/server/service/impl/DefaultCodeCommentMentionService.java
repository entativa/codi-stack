package io.codibase.server.service.impl;

import javax.inject.Singleton;

import io.codibase.server.model.CodeComment;
import io.codibase.server.model.CodeCommentMention;
import io.codibase.server.model.User;
import io.codibase.server.service.CodeCommentMentionService;

@Singleton
public class DefaultCodeCommentMentionService extends BaseEntityService<CodeCommentMention>
		implements CodeCommentMentionService {

	@Override
	public void mention(CodeComment comment, User user) {
		if (comment.getMentions().stream().noneMatch(it->it.getUser().equals(user))) {
			CodeCommentMention mention = new CodeCommentMention();
			mention.setComment(comment);
			mention.setUser(user);
			dao.persist(mention);
		}
	}

}
