package io.codibase.server.web.component.commit.status;

import org.jspecify.annotations.Nullable;

public interface CommitStatusSupport {
	
	@Nullable
	String getRefName();
	
}
