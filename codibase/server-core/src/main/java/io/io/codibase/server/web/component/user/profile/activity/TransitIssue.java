package io.codibase.server.web.component.user.profile.activity;

import static io.codibase.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.util.Date;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.unbescape.html.HtmlEscape;

import io.codibase.server.CodiBase;
import io.codibase.server.service.IssueService;
import io.codibase.server.model.Issue;
import io.codibase.server.web.UrlService;

public class TransitIssue extends IssueActivity {

    private final Long issueId;

    private final String state;

    public TransitIssue(Date date, Issue issue, String state) {
        super(date);
        this.issueId = issue.getId();
        this.state = state;
    }

    @Override
    public Issue getIssue() {
        return CodiBase.getInstance(IssueService.class).load(issueId);
    }
    
    public String getState() {
        return state;
    }
    
    @Override
    public Component render(String id) {
        var issue = getIssue();
        var url = CodiBase.getInstance(UrlService.class).urlFor(issue, false);
        var label = MessageFormat.format(_T("Transited state of issue \"{0}\" to \"{1}\" ({2})"), "<a href=\"" + url + "\">" + issue.getReference() + "</a>", state, HtmlEscape.escapeHtml5(issue.getTitle()));
        return new Label(id, label).setEscapeModelStrings(false);
    }
}