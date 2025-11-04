package io.codibase.server.markdown;

import com.vladsch.flexmark.ast.InlineLinkNode;
import com.vladsch.flexmark.formatter.MarkdownWriter;
import com.vladsch.flexmark.formatter.NodeFormatterContext;
import com.vladsch.flexmark.formatter.NodeFormattingHandler;

import io.codibase.server.CodiBase;

public class ExternalImageFormatter<N extends InlineLinkNode> implements NodeFormattingHandler.CustomNodeFormatter<N> {

	@Override
	public void render(InlineLinkNode node, NodeFormatterContext context, MarkdownWriter markdown) {
		markdown.append("![");
		context.renderChildren(node);
		markdown.append("](");
		markdown.append(CodiBase.getInstance(MarkdownService.class).toExternal(node.getUrl().toString()));
		markdown.append(")");
	}

}
