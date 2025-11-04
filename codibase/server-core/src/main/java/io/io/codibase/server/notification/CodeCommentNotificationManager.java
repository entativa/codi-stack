package io.codibase.server.notification;

import com.google.common.collect.Lists;
import io.codibase.commons.utils.StringUtils;
import io.codibase.server.service.CodeCommentMentionService;
import io.codibase.server.service.UserService;
import io.codibase.server.event.Listen;
import io.codibase.server.event.project.codecomment.CodeCommentEdited;
import io.codibase.server.event.project.codecomment.CodeCommentEvent;
import io.codibase.server.mail.MailService;
import io.codibase.server.markdown.MentionParser;
import io.codibase.server.model.CodeComment;
import io.codibase.server.model.CodeCommentStatusChange;
import io.codibase.server.model.User;
import io.codibase.server.model.support.EntityComment;
import io.codibase.server.persistence.annotation.Transactional;
import io.codibase.server.util.commenttext.MarkdownText;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static io.codibase.server.notification.NotificationUtils.getEmailBody;
import static java.util.stream.Collectors.toSet;

@Singleton
public class CodeCommentNotificationManager {

	@Inject
	private MailService mailService;

	@Inject
	private UserService userService;

	@Inject
	private CodeCommentMentionService mentionService;

	@Transactional
	@Listen
	public void on(CodeCommentEvent event) {
		CodeComment comment = event.getComment();
		if (comment.getCompareContext().getPullRequest() == null && !event.getUser().isServiceAccount()) {
			MarkdownText markdown = (MarkdownText) event.getCommentText();

			Collection<User> notifyUsers = new HashSet<>();

			notifyUsers.add(event.getUser());
			notifyUsers.add(comment.getUser());
			notifyUsers.addAll(comment.getReplies().stream().map(EntityComment::getUser).collect(toSet()));
			notifyUsers.addAll(comment.getChanges().stream().map(CodeCommentStatusChange::getUser).collect(toSet()));

			if (markdown != null) {
				for (String userName : new MentionParser().parseMentions(markdown.getRendered())) {
					User user = userService.findByName(userName);
					if (user != null) {
						mentionService.mention(comment, user);
						notifyUsers.add(user);
					}
				}
			}

			Set<String> emailAddresses = notifyUsers.stream()
					.filter(it -> it.isOrdinary()
							&& (!it.equals(event.getUser()) || it.isNotifyOwnEvents())
							&& it.getPrimaryEmailAddress() != null
							&& it.getPrimaryEmailAddress().isVerified())
					.map(it -> it.getPrimaryEmailAddress().getValue())
					.collect(toSet());

			if (!emailAddresses.isEmpty() && !(event instanceof CodeCommentEdited)) {
				String url = event.getUrl();
				String subject = String.format(
						"[Code Comment %s:%s] %s",
						event.getProject().getPath(), 
						comment.getMark().getPath(),
						StringUtils.capitalize(event.getActivity()));

				String summary = String.format(
						"%s %s", 
						event.getUser().getDisplayName(), event.getActivity());

				String threadingReferences = "<" + comment.getProject().getPath()
						+ "-codecomment-" + comment.getId() + "@codibase>";

				mailService.sendMailAsync(emailAddresses, Lists.newArrayList(),
						Lists.newArrayList(), subject,
						getEmailBody(true, event, summary, markdown != null ? markdown.getProcessed() : null, url, false, null),
						getEmailBody(false, event, summary, markdown != null ? markdown.getContent() : null, url, false, null),
						null, event.getUser().getDisplayName(), threadingReferences);
			}
		}
	}
}
