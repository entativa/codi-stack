package io.codibase.server.search.commit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

import com.google.common.base.Preconditions;

import io.codibase.server.CodiBase;
import io.codibase.server.event.project.RefUpdated;
import io.codibase.server.git.command.RevListOptions;
import io.codibase.server.git.service.GitService;
import io.codibase.server.model.Project;
import io.codibase.commons.utils.match.Matcher;
import io.codibase.commons.utils.match.PathMatcher;

public class PathCriteria extends CommitCriteria {

	private static final long serialVersionUID = 1L;

	private final List<String> values;
	
	public PathCriteria(List<String> values) {
		Preconditions.checkArgument(!values.isEmpty());
		this.values = values;
	}
	
	public List<String> getValues() {
		return values;
	}

	@Override
	public void fill(Project project, RevListOptions options) {
		for (String value: values)
			options.paths().add(value);
	}

	@Override
	public boolean matches(RefUpdated event) {
		Project project = event.getProject();
		RevCommit commit = project.getRevCommit(event.getNewCommitId(), true);
		
		GitService gitService = CodiBase.getInstance(GitService.class);
		Collection<String> changedFiles;
		if (!event.getOldCommitId().equals(ObjectId.zeroId())) 
			changedFiles = gitService.getChangedFiles(project, event.getOldCommitId(), event.getNewCommitId(), null);
		else if (commit.getParentCount() != 0)
			changedFiles = gitService.getChangedFiles(project, commit.getParent(0), event.getNewCommitId(), null);
		else
			changedFiles = new HashSet<>();
		
		Matcher matcher = new PathMatcher();
		for (String value: values) {
			for (String changedFile: changedFiles) {
				if (matcher.matches(value, changedFile)) 
					return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		List<String> parts = new ArrayList<>();
		for (String value: values) 
			parts.add(getRuleName(CommitQueryLexer.PATH) + parens(value));
		return StringUtils.join(parts, " ");
	}
	
}
