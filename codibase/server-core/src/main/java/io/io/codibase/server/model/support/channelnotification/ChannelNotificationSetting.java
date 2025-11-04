package io.codibase.server.model.support.channelnotification;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintValidatorContext;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.codibase.server.annotation.ClassValidating;
import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.OmitName;
import io.codibase.server.annotation.Vertical;
import io.codibase.server.validation.Validatable;
import io.codibase.server.web.page.project.setting.ContributedProjectSetting;

@ClassValidating
public class ChannelNotificationSetting implements ContributedProjectSetting, Validatable {

	private static final long serialVersionUID = 1L;

	private List<ChannelNotification> notifications = new ArrayList<>();

	@Editable
	@Vertical
	@OmitName
	@NotNull
	@Valid
	public List<ChannelNotification> getNotifications() {
		return notifications;
	}

	public void setNotifications(List<ChannelNotification> notifications) {
		this.notifications = notifications;
	}
	
	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		Set<String> webhookUrls = new HashSet<>();
		for (ChannelNotification notification: notifications) {
			if (notification.getWebhookUrl() != null
					&& !webhookUrls.add(notification.getWebhookUrl())) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate("Duplicate webhook url found").addConstraintViolation();
				return false;
			}
		}
		return true;
	}
	
}
