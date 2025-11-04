package io.codibase.server.web.page.admin.usermanagement;

import static io.codibase.server.web.translation.Translation._T;

import java.text.MessageFormat;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.codibase.commons.utils.TaskLogger;
import io.codibase.server.CodiBase;
import io.codibase.server.service.AuditService;
import io.codibase.server.service.SettingService;
import io.codibase.server.service.UserInvitationService;
import io.codibase.server.model.UserInvitation;
import io.codibase.server.web.component.taskbutton.TaskButton;
import io.codibase.server.web.component.taskbutton.TaskResult;
import io.codibase.server.web.component.taskbutton.TaskResult.PlainMessage;
import io.codibase.server.web.editable.BeanContext;
import io.codibase.server.web.page.admin.AdministrationPage;
import io.codibase.server.web.page.admin.mailservice.MailConnectorPage;

public class NewInvitationPage extends AdministrationPage {

	public NewInvitationPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		var mailConnector = CodiBase.getInstance(SettingService.class).getMailConnector();
		if (mailConnector != null) {
			Fragment fragment = new Fragment("content", "inviteFrag", this);
			NewInvitationBean bean = new NewInvitationBean();
			Form<?> form = new Form<Void>("form");
			form.add(BeanContext.edit("editor", bean));
			form.add(new FencedFeedbackPanel("feedback", form));
			form.add(new TaskButton("invite") {

				@Override
				protected TaskResult runTask(TaskLogger logger) throws InterruptedException {
					for (String emailAddress: bean.getListOfEmailAddresses()) {
						logger.log(MessageFormat.format(_T("Sending invitation to \"{0}\"..."), emailAddress));
						UserInvitation invitation = new UserInvitation();
						invitation.setEmailAddress(emailAddress);
						UserInvitationService userInvitationService = CodiBase.getInstance(UserInvitationService.class);
						userInvitationService.create(invitation);
						CodiBase.getInstance(AuditService.class).audit(null, "created invitation for \"" + emailAddress + "\"", null, null);
						userInvitationService.sendInvitationEmail(invitation);
						if (Thread.interrupted())
							throw new InterruptedException();
					}
					return new TaskResult(true, new PlainMessage(_T("Invitations sent")));
				}

				@Override
				protected void onCompleted(AjaxRequestTarget target, boolean successful) {
					if (successful)
						setResponsePage(InvitationListPage.class);
				}
				
			});
			fragment.add(form);
			add(fragment);
		} else {
			Fragment fragment = new Fragment("content", "noMailServiceFrag", this);
			fragment.add(new BookmarkablePageLink<Void>("mailSetting", MailConnectorPage.class));
			add(fragment);
		}
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, _T("Invite Users"));
	}

}
