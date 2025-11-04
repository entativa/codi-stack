package io.codibase.server.util;

public interface CommitAware {
	
	ProjectScopedCommit getCommit();
	
}
