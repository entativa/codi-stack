package io.codibase.server.search.entity.codecomment;

import static io.codibase.server.web.translation.Translation._T;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import io.codibase.commons.utils.ExplicitException;
import io.codibase.server.model.CodeComment;
import io.codibase.server.model.CodeCommentReply;
import io.codibase.server.model.User;
import io.codibase.server.util.ProjectScope;
import io.codibase.server.util.criteria.Criteria;

public class RepliedByMeCriteria extends Criteria<CodeComment> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<CodeComment, CodeComment> from, CriteriaBuilder builder) {
		if (User.get() != null) {
			Subquery<CodeCommentReply> replyQuery = query.subquery(CodeCommentReply.class);
			Root<CodeCommentReply> reply = replyQuery.from(CodeCommentReply.class);
			replyQuery.select(reply);
			replyQuery.where(builder.and(
					builder.equal(reply.get(CodeCommentReply.PROP_COMMENT), from),
					builder.equal(reply.get(CodeCommentReply.PROP_USER), User.get())));
			return builder.exists(replyQuery);
		} else {
			throw new ExplicitException(_T("Please login to perform this query"));
		}
	}

	@Override
	public boolean matches(CodeComment comment) {
		if (User.get() != null)
			return comment.getReplies().stream().anyMatch(it->it.getUser().equals(User.get()));
		else
			throw new ExplicitException(_T("Please login to perform this query"));
	}

	@Override
	public String toStringWithoutParens() {
		return CodeCommentQuery.getRuleName(CodeCommentQueryLexer.RepliedByMe);
	}

}
