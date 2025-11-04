package io.codibase.server.web.component.user.profile.activity;

import static io.codibase.server.web.translation.Translation._T;
import static org.unbescape.html.HtmlEscape.escapeHtml5;

import java.text.MessageFormat;
import java.util.Date;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.cycle.RequestCycle;

import io.codibase.server.CodiBase;
import io.codibase.server.service.PullRequestService;
import io.codibase.server.model.PullRequest;
import io.codibase.server.web.page.project.pullrequests.detail.activities.PullRequestActivitiesPage;

public class ApprovePullRequest extends PullRequestActivity {

    private final Long requestId;

    public ApprovePullRequest(Date date, PullRequest request) {
        super(date);
        this.requestId = request.getId();
    }

    @Override
    public PullRequest getPullRequest() {
        return CodiBase.getInstance(PullRequestService.class).load(requestId);
    }

    @Override
    public Component render(String id) {
        var request = getPullRequest();
        var url = RequestCycle.get().urlFor(PullRequestActivitiesPage.class, PullRequestActivitiesPage.paramsOf(request));
        var label = MessageFormat.format(_T("Approved pull request \"{0}\" ({1})"), "<a href=\"" + url + "\">" + request.getReference() + "</a>", escapeHtml5(request.getTitle()));
        return new Label(id, label).setEscapeModelStrings(false);
    }

}