package io.codibase.server.web.page.project.blob.render.renderers.image;

import org.apache.wicket.Component;

import io.codibase.server.web.page.project.blob.render.BlobRenderContext;
import io.codibase.server.web.page.project.blob.render.BlobRenderContext.Mode;
import io.codibase.server.web.page.project.blob.render.BlobRenderer;

public class ImageRenderer implements BlobRenderer {

	private static final long serialVersionUID = 1L;

	@Override
	public Component render(String componentId, BlobRenderContext context) {
		if (context.getMode() == Mode.VIEW 
				&& context.getProject().detectMediaType(context.getBlobIdent()).getType().equalsIgnoreCase("image")) { 
			return new ImageViewPanel(componentId, context);
		} else { 
			return null;
		}
	}

}
