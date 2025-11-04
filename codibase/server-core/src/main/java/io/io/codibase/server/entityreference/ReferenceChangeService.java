package io.codibase.server.entityreference;

import org.jspecify.annotations.Nullable;

import io.codibase.server.model.CodeComment;
import io.codibase.server.model.Issue;
import io.codibase.server.model.PullRequest;
import io.codibase.server.model.User;

public interface ReferenceChangeService {

	void addReferenceChange(User user, Issue issue, @Nullable String markdown);
	
	void addReferenceChange(User user, PullRequest request, @Nullable String markdown);
	
	void addReferenceChange(User user, CodeComment comment, @Nullable String markdown);
	
}
