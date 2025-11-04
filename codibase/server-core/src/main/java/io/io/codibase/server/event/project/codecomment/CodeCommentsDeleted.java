package io.codibase.server.event.project.codecomment;

import io.codibase.server.event.project.ProjectEvent;
import io.codibase.server.model.CodeComment;
import io.codibase.server.model.Project;
import io.codibase.server.security.SecurityUtils;

import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

public class CodeCommentsDeleted extends ProjectEvent {
	
	private static final long serialVersionUID = 1L;
	
	private final Collection<Long> commentIds;
	
	public CodeCommentsDeleted(Project project, Collection<CodeComment> comments) {
		super(SecurityUtils.getAuthUser(), new Date(), project);
		commentIds = comments.stream().map(CodeComment::getId).collect(Collectors.toSet());
	}

	public Collection<Long> getCommentIds() {
		return commentIds;
	}

	@Override
	public String getActivity() {
		return "code comments deleted";
	}
	
}
