package io.codibase.server.markdown;

import com.vladsch.flexmark.formatter.NodeFormattingHandler;
import io.codibase.server.model.Project;
import io.codibase.server.web.component.markdown.SuggestionSupport;
import io.codibase.server.web.page.project.blob.render.BlobRenderContext;

import org.jspecify.annotations.Nullable;
import java.util.Set;

public interface MarkdownService {
	
	/**
	 * Render specified markdown into html
	 * 
	 * @param markdown
	 * 			markdown to be rendered
	 * 			
	 * @return
	 * 			rendered html
	 */
	String render(String markdown);
	
	String process(String html, @Nullable Project project,
				   @Nullable BlobRenderContext blobRenderContext,
				   @Nullable SuggestionSupport suggestionSupport,
				   boolean forExternal);
	
	String toExternal(String url);
	
	String format(String markdown, Set<NodeFormattingHandler<?>> handlers);
	
}
