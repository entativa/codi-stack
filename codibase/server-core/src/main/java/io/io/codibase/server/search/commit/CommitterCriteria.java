package io.codibase.server.search.commit;

import io.codibase.server.event.project.RefUpdated;
import io.codibase.server.git.command.RevListOptions;
import io.codibase.server.model.Project;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.List;

public class CommitterCriteria extends PersonCriteria {

	private static final long serialVersionUID = 1L;
	
	public CommitterCriteria(List<String> values) {
		super(values);
	}
	
	@Override
	public void fill(Project project, RevListOptions options) {
		fill(project, options.committers());
	}

	@Override
	public boolean matches(RefUpdated event) {
		RevCommit commit = event.getProject().getRevCommit(event.getNewCommitId(), true);
		return matches(commit.getCommitterIdent());
	}

	@Override
	public String toString() {
		return toString(CommitQueryLexer.COMMITTER, CommitQueryLexer.CommittedByMe);
	}
	
}
