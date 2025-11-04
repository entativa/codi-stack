package io.codibase.server.event.project.pullrequest;

import org.jspecify.annotations.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import io.codibase.server.CodiBase;
import io.codibase.server.service.PullRequestChangeService;
import io.codibase.server.web.UrlService;
import io.codibase.server.model.PullRequestChange;
import io.codibase.server.model.support.pullrequest.MergePreview;
import io.codibase.server.model.support.pullrequest.changedata.PullRequestDiscardData;
import io.codibase.server.model.support.pullrequest.changedata.PullRequestMergeData;
import io.codibase.server.util.CommitAware;
import io.codibase.server.util.ProjectScopedCommit;
import io.codibase.server.util.commenttext.CommentText;
import io.codibase.server.util.commenttext.MarkdownText;

public class PullRequestChanged extends PullRequestEvent implements CommitAware {

	private static final long serialVersionUID = 1L;

	private final Long changeId;
	
	private final String note;
	
	public PullRequestChanged(PullRequestChange change, @Nullable String note) {
		super(change.getUser(), change.getDate(), change.getRequest());
		changeId = change.getId();
		this.note = note;
	}

	public PullRequestChange getChange() {
		return CodiBase.getInstance(PullRequestChangeService.class).load(changeId);
	}

	@Override
	protected CommentText newCommentText() {
		return note!=null? new MarkdownText(getProject(), note): null;
	}
	
	@Nullable
	public String getComment() {
		return note;
	}

	@Override
	public String getActivity() {
		return getChange().getData().getActivity();
	}

	@Override
	public ProjectScopedCommit getCommit() {
		ObjectId commitId;
		if (getChange().getData() instanceof PullRequestMergeData) {
			MergePreview preview = getRequest().checkMergePreview();
			if (preview != null)
				commitId = ObjectId.fromString(preview.getMergeCommitHash());
			else
				commitId = ObjectId.zeroId(); // Merged outside
		} else if (getChange().getData() instanceof PullRequestDiscardData) {
			commitId = ObjectId.fromString(getRequest().getLatestUpdate().getTargetHeadCommitHash());
		} else {
			commitId = ObjectId.zeroId();
		}
		return new ProjectScopedCommit(getProject(), commitId);
	}

	@Override
	public String getUrl() {
		return CodiBase.getInstance(UrlService.class).urlFor(getChange(), true);
	}

	@Override
	public boolean isMinor() {
		return getChange().isMinor();
	}
	
}
