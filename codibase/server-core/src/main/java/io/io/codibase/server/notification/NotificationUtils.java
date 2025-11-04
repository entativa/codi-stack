package io.codibase.server.notification;

import io.codibase.server.CodiBase;
import io.codibase.server.service.SettingService;
import io.codibase.server.event.Event;
import io.codibase.server.event.project.RefUpdated;
import io.codibase.server.event.project.build.BuildEvent;
import io.codibase.server.event.project.issue.IssueEvent;
import io.codibase.server.event.project.pack.PackEvent;
import io.codibase.server.event.project.pullrequest.PullRequestEvent;
import io.codibase.server.model.EmailAddress;
import io.codibase.server.model.User;
import io.codibase.server.model.support.administration.emailtemplates.EmailTemplates;
import org.unbescape.html.HtmlEscape;

import org.jspecify.annotations.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class NotificationUtils {

	public static String getEmailBody(boolean htmlVersion, Event event, String eventSummary, @Nullable String eventBody,
								  String eventUrl, boolean replyable, @Nullable Unsubscribable unsubscribable) {
		String template = null;

		Map<String, Object> bindings = new HashMap<>();

		if (htmlVersion)
			eventSummary = HtmlEscape.escapeHtml5(eventSummary);

		bindings.put("event", event);
		bindings.put("eventSummary", eventSummary);
		bindings.put("eventBody", eventBody);
		bindings.put("eventUrl", eventUrl);
		bindings.put("replyable", replyable);
		bindings.put("unsubscribable", unsubscribable);

		var templates = CodiBase.getInstance(SettingService.class).getEmailTemplates();
		if (event instanceof IssueEvent) {
			template = templates.getIssueNotification();
			bindings.put("issue", ((IssueEvent) event).getIssue());
		} else if (event instanceof PullRequestEvent) {
			template = templates.getPullRequestNotification();
			bindings.put("pullRequest", ((PullRequestEvent) event).getRequest());
		} else if (event instanceof BuildEvent) {
			template = templates.getBuildNotification();
			bindings.put("build", ((BuildEvent) event).getBuild());
		} else if (event instanceof PackEvent) {
			template = templates.getPackNotification();
			bindings.put("pack", ((PackEvent) event).getPack());
		} else if (event instanceof RefUpdated) {
			var refUpdated = (RefUpdated) event;
			template = templates.getCommitNotification();
			var commit = refUpdated.getProject().getRevCommit(refUpdated.getNewCommitId(), true);
			bindings.put("commit", commit);
		}
		if (template == null)
			template = EmailTemplates.DEFAULT_NOTIFICATION;

		return EmailTemplates.evalTemplate(htmlVersion, template, bindings);
	}
	
	public static boolean isNotified(Collection<String> notifiedEmailAddresses, User user) {
		for (EmailAddress emailAddress: user.getEmailAddresses()) {
			if (emailAddress.isVerified() && notifiedEmailAddresses.contains(emailAddress.getValue()))
				return true;
		}
		return false;
	}
	
}
