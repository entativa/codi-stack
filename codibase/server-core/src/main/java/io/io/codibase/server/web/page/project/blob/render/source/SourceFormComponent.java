package io.codibase.server.web.page.project.blob.render.source;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.Model;
import org.unbescape.javascript.JavaScriptEscape;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.codibase.commons.utils.PlanarRange;
import io.codibase.commons.utils.StringUtils;
import io.codibase.server.CodiBase;
import io.codibase.server.util.ContentDetector;
import io.codibase.server.web.component.sourceformat.SourceFormatPanel;
import io.codibase.server.web.page.project.blob.render.BlobRenderContext;
import io.codibase.server.web.page.project.blob.render.BlobRenderContext.Mode;
import io.codibase.server.web.page.project.blob.render.BlobRenderer;

abstract class SourceFormComponent extends FormComponentPanel<byte[]> {

	private TextArea<String> input;

	public SourceFormComponent(String id, byte[] initialContent) {
		super(id, Model.of(initialContent));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Charset detectedCharset = ContentDetector.detectCharset(getModelObject());
		Charset charset = detectedCharset!=null?detectedCharset:Charset.defaultCharset();
		
		String source = new String(getModelObject(), charset);
		add(input = new TextArea<String>("input", Model.of(source)) {

			@Override
			protected boolean shouldTrimInput() {
				return false;
			}
			
		});
		setOutputMarkupId(true);
	}

	@Override
	public void convertInput() {
		String content = input.getConvertedInput();
		if (content != null) {
			/*
			 * Textarea always uses CRLF as line ending, and below we change back to original EOL
			 */
			String initialContent = input.getModelObject();
			if (initialContent == null || !initialContent.contains("\r\n"))
				content = StringUtils.replace(content, "\r\n", "\n");
			setConvertedInput(content.getBytes(StandardCharsets.UTF_8));
		} else {
			setConvertedInput(new byte[0]);
		}
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		PlanarRange mark = BlobRenderer.getSourceRange(getContext().getPosition());
		String jsonOfMark;
		if (mark != null) {
			try {
				jsonOfMark = CodiBase.getInstance(ObjectMapper.class).writeValueAsString(mark);
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		} else {
			jsonOfMark = "undefined";
		}
		String script = String.format("codibase.server.sourceEdit.onDomReady("
				+ "'%s', '%s', %s, '%s', %s, '%s', %b);", 
				getMarkupId(), 
				JavaScriptEscape.escapeJavaScript(getContext().getNewPath()), 
				jsonOfMark,
				getSourceFormat().getIndentType(), 
				getSourceFormat().getTabSize(), 
				getSourceFormat().getLineWrapMode(), 
				getContext().getMode() == Mode.EDIT || getContext().getInitialNewPath() != null);
		response.render(OnDomReadyHeaderItem.forScript(script));
	}
	
	protected abstract BlobRenderContext getContext();
	
	protected abstract SourceFormatPanel getSourceFormat();
	
}
