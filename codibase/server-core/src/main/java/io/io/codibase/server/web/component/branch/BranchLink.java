package io.codibase.server.web.component.branch;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.FileMode;

import io.codibase.server.git.BlobIdent;
import io.codibase.server.security.SecurityUtils;
import io.codibase.server.util.ProjectAndBranch;
import io.codibase.server.web.component.link.ViewStateAwarePageLink;
import io.codibase.server.web.page.project.ProjectPage;
import io.codibase.server.web.page.project.blob.ProjectBlobPage;

public class BranchLink extends ViewStateAwarePageLink<Void> {

	private final ProjectAndBranch projectAndBranch;
	
	public BranchLink(String id, ProjectAndBranch projectAndBranch) {
		super(id, ProjectBlobPage.class, paramsOf(projectAndBranch));
		this.projectAndBranch = projectAndBranch;
	}
	
	private static PageParameters paramsOf(ProjectAndBranch projectAndBranch) {
		BlobIdent blobIdent = new BlobIdent(projectAndBranch.getBranch(), null, FileMode.TREE.getBits());
		ProjectBlobPage.State state = new ProjectBlobPage.State(blobIdent);
		return ProjectBlobPage.paramsOf(projectAndBranch.getProject(), state);
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setEnabled(SecurityUtils.canReadCode(projectAndBranch.getProject()) 
				&& projectAndBranch.getObjectName(false) != null);
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		configure();
		if (!isEnabled())
			tag.setName("span");
	}

	@Override
	public IModel<?> getBody() {
		String label;
		if (getPage() instanceof ProjectPage) {
			ProjectPage page = (ProjectPage) getPage();
			if (page.getProject().equals(projectAndBranch.getProject())) 
				label = projectAndBranch.getBranch();
			else 
				label = projectAndBranch.getFQN();
		} else {
			label = projectAndBranch.getFQN();
		}
		return Model.of(label);
	}

}
