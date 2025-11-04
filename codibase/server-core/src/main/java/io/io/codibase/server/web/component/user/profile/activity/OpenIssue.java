package io.codibase.server.web.component.user.profile.activity;

import static io.codibase.server.web.translation.Translation._T;

import java.text.MessageFormat;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.unbescape.html.HtmlEscape;

import io.codibase.server.CodiBase;
import io.codibase.server.service.IssueService;
import io.codibase.server.model.Issue;
import io.codibase.server.web.UrlService;

public class OpenIssue extends IssueActivity {

    private final Long issueId;

    public OpenIssue(Issue issue) {
        super(issue.getSubmitDate());
        this.issueId = issue.getId();
    }

    @Override
    public Issue getIssue() {
        return CodiBase.getInstance(IssueService.class).load(issueId);
    }
    
    @Override
    public Component render(String id) {
        var issue = getIssue();
        var url = CodiBase.getInstance(UrlService.class).urlFor(issue, false);
        var label = MessageFormat.format(_T("Opened issue \"{0}\" ({1})"), "<a href=\"" + url + "\">" + issue.getReference() + "</a>", HtmlEscape.escapeHtml5(issue.getTitle()));
        return new Label(id, label).setEscapeModelStrings(false);
    }

}