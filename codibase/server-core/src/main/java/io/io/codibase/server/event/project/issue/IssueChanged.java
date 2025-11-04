package io.codibase.server.event.project.issue;

import io.codibase.server.CodiBase;
import io.codibase.server.service.IssueChangeService;
import io.codibase.server.model.Group;
import io.codibase.server.model.IssueChange;
import io.codibase.server.model.User;
import io.codibase.server.model.support.issue.changedata.IssueStateChangeData;
import io.codibase.server.notification.ActivityDetail;
import io.codibase.server.util.CommitAware;
import io.codibase.server.util.ProjectScopedCommit;
import io.codibase.server.util.commenttext.CommentText;
import io.codibase.server.util.commenttext.MarkdownText;
import io.codibase.server.web.UrlService;

import org.jspecify.annotations.Nullable;
import java.util.Collection;
import java.util.Map;

public class IssueChanged extends IssueEvent implements CommitAware {

	private static final long serialVersionUID = 1L;

	private final Long changeId;
	
	private final String comment;
	
	public IssueChanged(IssueChange change, @Nullable String comment) {
		this(change, comment, !change.getUser().isServiceAccount());
	}

	public IssueChanged(IssueChange change, @Nullable String comment, boolean sendNotifications) {
		super(change.getUser(), change.getDate(), change.getIssue(), sendNotifications);
		changeId = change.getId();
		this.comment = comment;
	}
	
	public IssueChange getChange() {
		return CodiBase.getInstance(IssueChangeService.class).load(changeId);
	}

	@Override
	protected CommentText newCommentText() {
		return comment!=null? new MarkdownText(getProject(), comment): null;
	}

	@Nullable
	public String getComment() {
		return comment;
	}

	@Override
	public boolean affectsListing() {
		return getChange().affectsBoards();
	}

	@Override
	public Map<String, Collection<User>> getNewUsers() {
		return getChange().getData().getNewUsers();
	}

	@Override
	public Map<String, Group> getNewGroups() {
		return getChange().getData().getNewGroups();
	}

	@Override
	public String getActivity() {
		return getChange().getData().getActivity();
	}

	@Override
	public ActivityDetail getActivityDetail() {
		return getChange().getData().getActivityDetail();
	}

	@Override
	public String getUrl() {
		return CodiBase.getInstance(UrlService.class).urlFor(getChange(), true);
	}

	@Override
	public boolean isMinor() {
		return getChange().isMinor();
	}

	@Override
	public ProjectScopedCommit getCommit() {
		if (getChange().getData() instanceof IssueStateChangeData) {
			var project = getIssue().getProject();
			if (project.getDefaultBranch() != null)
				return new ProjectScopedCommit(project, project.getObjectId(project.getDefaultBranch(), true));
			else
				return null;
		} else {
			return null;
		}
	}

}
