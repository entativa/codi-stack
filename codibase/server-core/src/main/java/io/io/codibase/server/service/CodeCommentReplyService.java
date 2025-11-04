package io.codibase.server.service;

import java.util.Date;
import java.util.List;

import io.codibase.server.model.CodeCommentReply;
import io.codibase.server.model.User;

public interface CodeCommentReplyService extends EntityService<CodeCommentReply> {

	void create(CodeCommentReply reply);

	void update(CodeCommentReply reply);
	
 	List<CodeCommentReply> query(User creator, Date fromDate, Date toDate);

}	
