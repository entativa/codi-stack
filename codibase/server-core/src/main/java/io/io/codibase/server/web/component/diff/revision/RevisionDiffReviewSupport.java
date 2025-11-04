package io.codibase.server.web.component.diff.revision;

import java.io.Serializable;

public interface RevisionDiffReviewSupport extends Serializable {
	
	boolean isReviewed(String blobPath);
	
	void setReviewed(String blobPath, boolean reviewed);
	
}
