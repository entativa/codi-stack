package io.codibase.server.web.component.user.profile.activity;

import static io.codibase.server.web.translation.Translation._T;

import java.text.MessageFormat;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.unbescape.html.HtmlEscape;

import io.codibase.server.CodiBase;
import io.codibase.server.service.IssueCommentService;
import io.codibase.server.model.Issue;
import io.codibase.server.model.IssueComment;
import io.codibase.server.web.UrlService;

public class CommentIssue extends IssueActivity {

    private final Long commentId;

    public CommentIssue(IssueComment comment) {
        super(comment.getDate());
        this.commentId = comment.getId();
    }

    private IssueComment getComment() {
        return CodiBase.getInstance(IssueCommentService.class).load(commentId);
    }
    
    @Override
    public Issue getIssue() {
        return getComment().getIssue();
    }
    
    @Override
    public Component render(String id) {
        var comment = getComment();
        var url = CodiBase.getInstance(UrlService.class).urlFor(comment, false);
        var label = MessageFormat.format(_T("Commented on issue \"{0}\" ({1})"), "<a href=\"" + url + "\">" + comment.getIssue().getReference() + "</a>", HtmlEscape.escapeHtml5(comment.getIssue().getTitle()));
        return new Label(id, label).setEscapeModelStrings(false);
    }
}