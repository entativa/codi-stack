package io.codibase.server.search.commit;

import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;

import io.codibase.server.event.project.RefUpdated;
import io.codibase.server.git.command.RevListOptions;
import io.codibase.server.model.Project;

public class AuthorCriteria extends PersonCriteria {

	private static final long serialVersionUID = 1L;
	
	public AuthorCriteria(List<String> values) {
		super(values);
	}

	@Override
	public void fill(Project project, RevListOptions options) {
		fill(project, options.authors());
	}

	@Override
	public boolean matches(RefUpdated event) {
		RevCommit commit = event.getProject().getRevCommit(event.getNewCommitId(), true);
		return matches(commit.getAuthorIdent());
	}

	@Override
	public String toString() {
		return toString(CommitQueryLexer.AUTHOR, CommitQueryLexer.AuthoredByMe);
	}
	
}
