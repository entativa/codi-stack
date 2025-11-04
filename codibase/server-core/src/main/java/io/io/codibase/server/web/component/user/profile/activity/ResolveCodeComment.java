package io.codibase.server.web.component.user.profile.activity;

import static io.codibase.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.util.Date;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.unbescape.html.HtmlEscape;

import io.codibase.server.CodiBase;
import io.codibase.server.service.CodeCommentService;
import io.codibase.server.model.CodeComment;
import io.codibase.server.web.UrlService;

public class ResolveCodeComment extends CodeCommentActivity {

    private final Long commentId;

    public ResolveCodeComment(Date date, CodeComment comment) {
        super(date);
        this.commentId = comment.getId();
    }

    @Override
    public CodeComment getComment() {
        return CodiBase.getInstance(CodeCommentService.class).load(commentId);
    }
    
    @Override
    public Component render(String id) {
        var comment = getComment();
        var url = CodiBase.getInstance(UrlService.class).urlFor(comment, false);
        var label = MessageFormat.format(_T("Resolved comment on file \"{0}\" in project \"{1}\""), "<a href=\"" + url + "\">" + HtmlEscape.escapeHtml5(comment.getMark().getPath()) + "</a>", comment.getProject().getPath());
        return new Label(id, label).setEscapeModelStrings(false);
    }
}
