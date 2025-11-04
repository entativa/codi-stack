package io.codibase.server.buildspec.job.action;

import javax.validation.constraints.NotEmpty;

import io.codibase.server.CodiBase;
import io.codibase.server.event.project.build.BuildFinished;
import io.codibase.server.model.Build;
import io.codibase.server.notification.BuildNotificationManager;
import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.NotificationReceiver;

@Editable(name="Send notification", order=200)
public class SendNotificationAction extends PostBuildAction {

	private static final long serialVersionUID = 1L;
	
	private String receivers;
	
	@Editable(order=1000)
	@NotificationReceiver
	@NotEmpty
	public String getReceivers() {
		return receivers;
	}

	public void setReceivers(String receivers) {
		this.receivers = receivers;
	}

	@Override
	public void execute(Build build) {
		io.codibase.server.buildspec.job.action.notificationreceiver.NotificationReceiver parsedReceiver = 
				io.codibase.server.buildspec.job.action.notificationreceiver.NotificationReceiver.parse(getReceivers(), build);
		CodiBase.getInstance(BuildNotificationManager.class).notify(new BuildFinished(build), parsedReceiver.getEmails());
	}

	@Override
	public String getDescription() {
		return "Send notification to " + receivers;
	}

}
