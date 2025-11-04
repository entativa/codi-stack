package io.codibase.server.web.page.project.blob.render.renderers.image;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.image.Image;

import io.codibase.server.web.page.project.blob.render.BlobRenderContext;
import io.codibase.server.web.page.project.blob.render.view.BlobViewPanel;
import io.codibase.server.web.resource.RawBlobResource;
import io.codibase.server.web.resource.RawBlobResourceReference;

public class ImageViewPanel extends BlobViewPanel {

	public ImageViewPanel(String id, BlobRenderContext context) {
		super(id, context);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Image("img", new RawBlobResourceReference(), 
				RawBlobResource.paramsOf(context.getProject(), context.getBlobIdent())));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ImageViewResourceReference()));
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
