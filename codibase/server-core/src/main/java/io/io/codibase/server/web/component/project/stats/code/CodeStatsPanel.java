package io.codibase.server.web.component.project.stats.code;

import static io.codibase.server.web.translation.Translation._T;

import java.text.MessageFormat;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.Constants;

import io.codibase.server.CodiBase;
import io.codibase.server.git.service.GitService;
import io.codibase.server.model.Project;
import io.codibase.server.web.page.project.blob.ProjectBlobPage;
import io.codibase.server.web.page.project.branches.ProjectBranchesPage;
import io.codibase.server.web.page.project.commits.ProjectCommitsPage;
import io.codibase.server.web.page.project.tags.ProjectTagsPage;
import io.codibase.server.xodus.CommitInfoService;

public class CodeStatsPanel extends GenericPanel<Project> {

	private final IModel<Integer> commitCountModel = new LoadableDetachableModel<Integer>() {

		@Override
		protected Integer load() {
			return getCommitInfoManager().getCommitCount(getProject().getId());
		}
		
	};
	
	public CodeStatsPanel(String id, IModel<Project> projectModel) {
		super(id, projectModel);
	}

	private CommitInfoService getCommitInfoManager() {
		return CodiBase.getInstance(CommitInfoService.class);
	}
	
	private Project getProject() {
		return getModelObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		PageParameters params = ProjectBlobPage.paramsOf(getProject());
		Link<Void> filesLink = new BookmarkablePageLink<Void>("files", ProjectBlobPage.class, params) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getCommitInfoManager().getFileCount(getProject().getId()) != 0);
			}
			
		};
		filesLink.add(new Label("label", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return MessageFormat.format(_T("{0} files"), getCommitInfoManager().getFileCount(getProject().getId()));
			}
			
		}));
		add(filesLink);
		
		params = ProjectCommitsPage.paramsOf(getProject(), null);
		Link<Void> commitsLink = new BookmarkablePageLink<Void>("commits", ProjectCommitsPage.class, params);
		commitsLink.add(new Label("label", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return MessageFormat.format(_T("{0} commits"), commitCountModel.getObject());
			}
			
		}));
		add(commitsLink);
		
		params = ProjectBranchesPage.paramsOf(getProject());
		Link<Void> branchesLink = new BookmarkablePageLink<Void>("branches", ProjectBranchesPage.class, params);
		branchesLink.add(new Label("label", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				int branchCount = CodiBase.getInstance(GitService.class)
						.countRefs(getProject().getId(), Constants.R_HEADS);
				return MessageFormat.format(_T("{0} branches"), branchCount);
			}
			
		}));
		add(branchesLink);
		
		params = ProjectTagsPage.paramsOf(getProject());
		Link<Void> tagsLink = new BookmarkablePageLink<Void>("tags", ProjectTagsPage.class, params);
		tagsLink.add(new Label("label", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				int tagCount = CodiBase.getInstance(GitService.class)
						.countRefs(getProject().getId(), Constants.R_TAGS);
				return MessageFormat.format(_T("{0} tags"), tagCount);
			}
			
		}));
		add(tagsLink);
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(commitCountModel.getObject() != 0);
	}

	@Override
	protected void onDetach() {
		commitCountModel.detach();
		super.onDetach();
	}

}
