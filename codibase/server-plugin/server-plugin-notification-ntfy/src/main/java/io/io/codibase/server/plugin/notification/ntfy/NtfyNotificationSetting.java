package io.codibase.server.plugin.notification.ntfy;

import io.codibase.server.annotation.Editable;
import io.codibase.server.model.support.channelnotification.ChannelNotificationSetting;

@Editable(name="Ntfy.sh Notifications", group="Notification", order=300, description="Set up ntfy.sh notification " +
		"settings. Settings will be inherited by child projects, and can be overridden by defining settings with " +
		"same webhook url")
public class NtfyNotificationSetting extends ChannelNotificationSetting {

	private static final long serialVersionUID = 1L;
	
}
