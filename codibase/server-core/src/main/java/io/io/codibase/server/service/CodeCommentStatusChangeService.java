package io.codibase.server.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.jspecify.annotations.Nullable;

import io.codibase.server.model.CodeCommentStatusChange;
import io.codibase.server.model.User;

public interface CodeCommentStatusChangeService extends EntityService<CodeCommentStatusChange> {

	void create(CodeCommentStatusChange change, @Nullable String note);
	
	void create(Collection<CodeCommentStatusChange> changes, @Nullable String note);
	
	List<CodeCommentStatusChange> query(User creator, Date fromDate, Date toDate);

}
