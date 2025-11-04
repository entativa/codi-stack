package io.codibase.server.service;

import io.codibase.server.model.CodeComment;
import io.codibase.server.model.CodeCommentMention;
import io.codibase.server.model.User;

public interface CodeCommentMentionService extends EntityService<CodeCommentMention> {

	void mention(CodeComment comment, User user);

}
