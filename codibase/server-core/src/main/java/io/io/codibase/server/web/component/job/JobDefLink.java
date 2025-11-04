package io.codibase.server.web.component.job;

import org.jspecify.annotations.Nullable;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;

import io.codibase.server.buildspec.BuildSpec;
import io.codibase.server.buildspec.job.Job;
import io.codibase.server.git.BlobIdent;
import io.codibase.server.model.Project;
import io.codibase.server.model.PullRequest;
import io.codibase.server.security.SecurityUtils;
import io.codibase.server.web.page.project.blob.ProjectBlobPage;
import io.codibase.server.web.page.project.blob.render.renderers.buildspec.BuildSpecRenderer;

public abstract class JobDefLink extends BookmarkablePageLink<Void> {

	private final ObjectId commitId;
	
	private final String jobName;
	
	public JobDefLink(String id, ObjectId commitId, String jobName) {
		super(id, ProjectBlobPage.class);
		this.commitId = commitId;
		this.jobName = jobName;
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setEnabled(SecurityUtils.canReadCode(getProject()));
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		if (!isEnabled())
			tag.setName("span");
	}

	protected abstract Project getProject();
	
	@Nullable
	protected PullRequest getPullRequest() {
		return null;
	}
	
	@Override
	public PageParameters getPageParameters() {
		ProjectBlobPage.State state = new ProjectBlobPage.State();
		state.blobIdent = new BlobIdent(commitId.name(), BuildSpec.BLOB_PATH, FileMode.REGULAR_FILE.getBits()); 
		if (getProject().getBlob(state.blobIdent, false) == null)
			state.blobIdent = new BlobIdent(commitId.name(), ".codibase-buildspec", FileMode.REGULAR_FILE.getBits());
		state.position = BuildSpecRenderer.getPosition(Job.SELECTION_PREFIX + jobName);
		state.requestId = PullRequest.idOf(getPullRequest());
		return ProjectBlobPage.paramsOf(getProject(), state);
	}

}
