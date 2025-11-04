package io.codibase.server.web.page.admin.emailtemplates;

import static io.codibase.server.model.support.administration.emailtemplates.EmailTemplates.DEFAULT_SERVICE_DESK_ISSUE_OPENED;
import static io.codibase.server.model.support.administration.emailtemplates.EmailTemplates.PROP_SERVICE_DESK_ISSUE_OPENED;
import static io.codibase.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.codibase.server.util.CollectionUtils;

public class ServiceDeskIssueOpenedTemplatePage extends AbstractTemplatePage {

	public ServiceDeskIssueOpenedTemplatePage(PageParameters params) {
		super(params);
	}

	@Override
	protected String getPropertyName() {
		return PROP_SERVICE_DESK_ISSUE_OPENED;
	}

	@Override
	protected String getDefaultTemplate() {
		return DEFAULT_SERVICE_DESK_ISSUE_OPENED;
	}

	@Override
	protected String getHelpText() {
		return MessageFormat.format(_T("A {0} used as body of feedback email when issue is opened via service desk"), GROOVY_TEMPLATE_LINK);
	}

	@Override
	protected Map<String, String> getVariableHelp() {
		return CollectionUtils.newLinkedHashMap("issue", 
				_T("represents the <a href='https://code.codibase.io/codibase/server/~files/main/server-core/src/main/java/io/codibase/server/model/Issue.java' target='_blank'>issue</a> being opened via service desk"));
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, _T("Service Desk Issue Opened Template"));
	}

}