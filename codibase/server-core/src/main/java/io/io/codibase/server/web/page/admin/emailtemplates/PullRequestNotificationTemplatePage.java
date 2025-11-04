package io.codibase.server.web.page.admin.emailtemplates;

import static io.codibase.server.model.support.administration.emailtemplates.EmailTemplates.PROP_PULL_REQUEST_NOTIFICATION;
import static io.codibase.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.codibase.server.util.CollectionUtils;

public class PullRequestNotificationTemplatePage extends AbstractNotificationTemplatePage {

	public PullRequestNotificationTemplatePage(PageParameters params) {
		super(params);
	}

	@Override
	protected String getPropertyName() {
		return PROP_PULL_REQUEST_NOTIFICATION;
	}

	@Override
	protected String getHelpText() {
		return MessageFormat.format(_T("A {0} used as body of various pull request notification emails"), GROOVY_TEMPLATE_LINK);
	}

	@Override
	protected Map<String, String> getVariableHelp() {
		return CollectionUtils.newLinkedHashMap("pullRequest", 
				_T("represents the <a href='https://code.codibase.io/codibase/server/~files/main/server-core/src/main/java/io/codibase/server/model/PullRequest.java' target='_blank'>pull request</a> object to be notified"));
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, _T("Pull Request Notification Template"));
	}

}