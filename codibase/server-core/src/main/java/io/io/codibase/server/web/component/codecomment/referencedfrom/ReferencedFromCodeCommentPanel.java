package io.codibase.server.web.component.codecomment.referencedfrom;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.unbescape.html.HtmlEscape;

import io.codibase.server.CodiBase;
import io.codibase.server.service.CodeCommentService;
import io.codibase.server.model.CodeComment;
import io.codibase.server.security.SecurityUtils;
import io.codibase.server.web.UrlService;

public class ReferencedFromCodeCommentPanel extends GenericPanel<CodeComment> {

	public ReferencedFromCodeCommentPanel(String id, Long commentId) {
		super(id, new LoadableDetachableModel<CodeComment>() {

			@Override
			protected CodeComment load() {
				return CodiBase.getInstance(CodeCommentService.class).load(commentId);
			}
			
		});
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		CodeComment comment = getModelObject();
		
		if (SecurityUtils.canReadCode(comment.getProject())) {
			String title = String.format("<a href='%s'>%s</a>",
					CodiBase.getInstance(UrlService.class).urlFor(comment, false),
					HtmlEscape.escapeHtml5(comment.getMark().getPath()));
			add(new Label("title", title).setEscapeModelStrings(false));
		} else {
			add(new Label("title", comment.getMark().getPath()));  
		}
	}

}
