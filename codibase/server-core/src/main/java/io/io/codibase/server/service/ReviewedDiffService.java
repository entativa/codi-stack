package io.codibase.server.service;

import io.codibase.server.model.ReviewedDiff;
import io.codibase.server.model.User;

import java.util.Map;

public interface ReviewedDiffService extends EntityService<ReviewedDiff> {
	
	Map<String, ReviewedDiff> query(User user, String oldCommitHash, String newCommitHash);

	void createOrUpdate(ReviewedDiff status);
	
}
