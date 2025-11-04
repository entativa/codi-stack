package io.codibase.server.web.page.project.blob.render.renderers.symbollink;

import java.io.File;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.eclipse.jgit.lib.ObjectId;

import io.codibase.commons.utils.PathUtils;
import io.codibase.server.CodiBase;
import io.codibase.server.git.Blob;
import io.codibase.server.git.BlobIdent;
import io.codibase.server.git.service.GitService;
import io.codibase.server.web.component.link.ViewStateAwarePageLink;
import io.codibase.server.web.page.project.blob.ProjectBlobPage;
import io.codibase.server.web.page.project.blob.render.BlobRenderContext;
import io.codibase.server.web.page.project.blob.render.view.BlobViewPanel;

public class SymbolLinkPanel extends BlobViewPanel {

	public SymbolLinkPanel(String id, BlobRenderContext context) {
		super(id, context);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Blob blob = context.getProject().getBlob(context.getBlobIdent(), true);
		String targetPath = PathUtils.normalizeDots(
				PathUtils.resolveSibling(context.getBlobIdent().path, blob.getText().getContent()));
		if (targetPath != null && (targetPath.startsWith("/") || new File(targetPath).isAbsolute())) 
			targetPath = null;

		BlobIdent targetBlobIdent;
		if (targetPath != null) {
			GitService gitService = CodiBase.getInstance(GitService.class);
			ObjectId commitId = context.getProject().getObjectId(context.getBlobIdent().revision, true);
			int mode = gitService.getMode(context.getProject(), commitId, targetPath);
			if (mode != 0) 
				targetBlobIdent = new BlobIdent(context.getBlobIdent().revision, targetPath, mode);
			else
				targetBlobIdent = null;
		} else {
			targetBlobIdent = null;
		}

		WebMarkupContainer link;
		if (targetBlobIdent == null) {
			link = new Link<Void>("link") {

				@Override
				public void onClick() {
				}
				
			};
			link.setEnabled(false);
		} else {
			ProjectBlobPage.State state = new ProjectBlobPage.State(targetBlobIdent);
			link = new ViewStateAwarePageLink<Void>("link", ProjectBlobPage.class, 
					ProjectBlobPage.paramsOf(context.getProject(), state));
		} 
		link.add(new Label("label", blob.getText().getContent()));
		add(link);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(new SymbolLinkResourceReference()));
	}

	@Override
	protected boolean isEditSupported() {
		return false;
	}

	@Override
	protected boolean isViewPlainSupported() {
		return false;
	}

}
