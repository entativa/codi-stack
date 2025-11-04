package io.codibase.server.web.component.user.profile.activity;

import static io.codibase.server.web.translation.Translation._T;

import java.text.MessageFormat;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.cycle.RequestCycle;
import org.unbescape.html.HtmlEscape;

import io.codibase.server.CodiBase;
import io.codibase.server.service.PullRequestCommentService;
import io.codibase.server.model.PullRequest;
import io.codibase.server.model.PullRequestComment;
import io.codibase.server.web.page.project.pullrequests.detail.activities.PullRequestActivitiesPage;

public class CommentPullRequest extends PullRequestActivity {

    private final Long commentId;

    public CommentPullRequest(PullRequestComment comment) {
        super(comment.getDate());
        this.commentId = comment.getId();
    }

    private PullRequestComment getComment() {
        return CodiBase.getInstance(PullRequestCommentService.class).load(commentId);
    }

    @Override
    public PullRequest getPullRequest() {
        return getComment().getRequest();
    }
    
    @Override
    public Component render(String id) {
        var comment = getComment();
        var url = RequestCycle.get().urlFor(PullRequestActivitiesPage.class, PullRequestActivitiesPage.paramsOf(comment.getRequest()));
        var label = MessageFormat.format(_T("Commented on pull request \"{0}\" ({1})"), "<a href=\"" + url + "\">" + comment.getRequest().getReference() + "</a>", HtmlEscape.escapeHtml5(comment.getRequest().getTitle()));
        return new Label(id, label).setEscapeModelStrings(false);
    }
}