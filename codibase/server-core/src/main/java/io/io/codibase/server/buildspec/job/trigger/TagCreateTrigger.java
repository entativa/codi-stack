package io.codibase.server.buildspec.job.trigger;

import io.codibase.commons.codeassist.InputSuggestion;
import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.Patterns;
import io.codibase.server.buildspec.job.Job;
import io.codibase.server.buildspec.job.TriggerMatch;
import io.codibase.server.event.project.ProjectEvent;
import io.codibase.server.event.project.RefUpdated;
import io.codibase.server.git.GitUtils;
import io.codibase.server.model.Project;
import io.codibase.commons.utils.match.PathMatcher;
import io.codibase.server.util.patternset.PatternSet;
import io.codibase.server.web.util.SuggestionUtils;
import org.eclipse.jgit.lib.ObjectId;

import java.util.List;

@Editable(order=200, name="Tag creation")
public class TagCreateTrigger extends JobTrigger {

	private static final long serialVersionUID = 1L;

	private String tags;
	
	private String branches;
	
	@Editable(name="Tags", order=100, placeholder="Any tag", description=""
			+ "Optionally specify space-separated tags to check. Use '**', '*' or '?' for "
			+ "<a href='https://docs.codibase.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. "
			+ "Prefix with '-' to exclude. Leave empty to match all tags")
	@Patterns(suggester="suggestTags", path=true)
	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestTags(String matchWith) {
		return SuggestionUtils.suggestTags(Project.get(), matchWith);
	}

	@Editable(name="On Branches", order=200, placeholder="Any branch", description=""
			+ "This trigger will only be applicable if tagged commit is reachable from branches specified here. "
			+ "Multiple branches should be separated with spaces. Use '**', '*' or '?' for "
			+ "<a href='https://docs.codibase.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. "
			+ "Prefix with '-' to exclude. Leave empty to match all branches")
	@Patterns(suggester="suggestBranches", path=true)
	public String getBranches() {
		return branches;
	}

	public void setBranches(String branches) {
		this.branches = branches;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestBranches(String matchWith) {
		return SuggestionUtils.suggestBranches(Project.get(), matchWith);
	}
	
	@Override
	protected TriggerMatch triggerMatches(ProjectEvent event, Job job) {
		if (event instanceof RefUpdated) {
			RefUpdated refUpdated = (RefUpdated) event;
			String updatedTag = GitUtils.ref2tag(refUpdated.getRefName());
			ObjectId commitId = refUpdated.getNewCommitId();
			Project project = event.getProject();
			if (updatedTag != null && !commitId.equals(ObjectId.zeroId()) 
					&& (tags == null || PatternSet.parse(tags).matches(new PathMatcher(), updatedTag))
					&& (branches == null || project.isCommitOnBranches(commitId, PatternSet.parse(branches)))) {
				return new TriggerMatch(refUpdated.getRefName(), null, null, getParamMatrix(), 
						getExcludeParamMaps(), "Tag '" + updatedTag + "' is created");
			}
		}
		return null;
	}

	@Override
	public String getTriggerDescription() {
		String description = "When create tags";
		if (tags != null)
			description += " '" + tags + "'";
		if (branches != null)
			description += " on branches '" + branches + "'";
		return description;
	}

}
