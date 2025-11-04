package io.codibase.server.search.entitytext;

import java.util.List;

import org.jspecify.annotations.Nullable;

import io.codibase.server.model.Project;
import io.codibase.server.model.CodeComment;

public interface CodeCommentTextService {

	List<Long> query(@Nullable Project project, String queryString, int count);
			
	boolean matches(CodeComment comment, String queryString);

}
