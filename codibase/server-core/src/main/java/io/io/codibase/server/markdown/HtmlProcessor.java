package io.codibase.server.markdown;

import org.jspecify.annotations.Nullable;

import org.jsoup.nodes.Document;

import io.codibase.commons.loader.ExtensionPoint;
import io.codibase.server.model.Project;
import io.codibase.server.web.component.markdown.SuggestionSupport;
import io.codibase.server.web.page.project.blob.render.BlobRenderContext;

@ExtensionPoint
public interface HtmlProcessor {
	
	void process(Document document, @Nullable Project project,
				 @Nullable BlobRenderContext blobRenderContext,
				 @Nullable SuggestionSupport suggestionSupport,
				 boolean forExternal);
	
}
