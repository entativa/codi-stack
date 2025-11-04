package io.codibase.server.service;

import io.codibase.server.model.CodeCommentQueryPersonalization;
import io.codibase.server.model.Project;
import io.codibase.server.model.User;

public interface CodeCommentQueryPersonalizationService extends EntityService<CodeCommentQueryPersonalization> {
	
	CodeCommentQueryPersonalization find(Project project, User user);

    void createOrUpdate(CodeCommentQueryPersonalization personalization);

}
