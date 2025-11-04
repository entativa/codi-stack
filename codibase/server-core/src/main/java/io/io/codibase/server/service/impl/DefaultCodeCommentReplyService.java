package io.codibase.server.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import com.google.common.base.Preconditions;

import io.codibase.server.event.ListenerRegistry;
import io.codibase.server.event.project.codecomment.CodeCommentReplyCreated;
import io.codibase.server.event.project.codecomment.CodeCommentReplyDeleted;
import io.codibase.server.event.project.codecomment.CodeCommentReplyEdited;
import io.codibase.server.event.project.pullrequest.PullRequestCodeCommentReplyCreated;
import io.codibase.server.model.CodeComment;
import io.codibase.server.model.CodeCommentReply;
import io.codibase.server.model.PullRequest;
import io.codibase.server.model.User;
import io.codibase.server.persistence.annotation.Sessional;
import io.codibase.server.persistence.annotation.Transactional;
import io.codibase.server.service.CodeCommentReplyService;

@Singleton
public class DefaultCodeCommentReplyService extends BaseEntityService<CodeCommentReply>
		implements CodeCommentReplyService {

	@Inject
	private ListenerRegistry listenerRegistry;

	@Transactional
	@Override
	public void create(CodeCommentReply reply) {
		Preconditions.checkState(reply.isNew());
		dao.persist(reply);
		
		CodeComment comment = reply.getComment();
		comment.setReplyCount(comment.getReplyCount()+1);
		
		listenerRegistry.post(new CodeCommentReplyCreated(reply));

		PullRequest request = comment.getCompareContext().getPullRequest();
		if (request != null) 
			listenerRegistry.post(new PullRequestCodeCommentReplyCreated(request, reply));
	}

	@Transactional
	@Override
	public void update(CodeCommentReply reply) {
 		Preconditions.checkState(!reply.isNew());
		dao.persist(reply);
		listenerRegistry.post(new CodeCommentReplyEdited(reply));
	}
	
	@Transactional
	@Override
	public void delete(CodeCommentReply reply) {
		super.delete(reply);
		CodeComment comment = reply.getComment();
		comment.setReplyCount(comment.getReplyCount()-1);
		listenerRegistry.post(new CodeCommentReplyDeleted(reply));
	}
	
	@Sessional
	@Override
	public List<CodeCommentReply> query(User creator, Date fromDate, Date toDate) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<CodeCommentReply> query = builder.createQuery(CodeCommentReply.class);
		From<CodeCommentReply, CodeCommentReply> root = query.from(CodeCommentReply.class);
		
		List<Predicate> predicates = new ArrayList<>();

		predicates.add(builder.equal(root.get(CodeCommentReply.PROP_USER), creator));
		predicates.add(builder.greaterThanOrEqualTo(root.get(CodeCommentReply.PROP_DATE), fromDate));
		predicates.add(builder.lessThanOrEqualTo(root.get(CodeCommentReply.PROP_DATE), toDate));
			
		query.where(predicates.toArray(new Predicate[0]));
		
		return getSession().createQuery(query).getResultList();
	}

}
