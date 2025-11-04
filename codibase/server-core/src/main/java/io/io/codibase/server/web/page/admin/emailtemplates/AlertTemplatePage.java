package io.codibase.server.web.page.admin.emailtemplates;

import io.codibase.server.util.CollectionUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.text.MessageFormat;
import java.util.Map;

import static io.codibase.server.model.support.administration.emailtemplates.EmailTemplates.*;
import static io.codibase.server.web.translation.Translation._T;

public class AlertTemplatePage extends AbstractTemplatePage {

	public AlertTemplatePage(PageParameters params) {
		super(params);
	}

	@Override
	protected String getPropertyName() {
		return PROP_ALERT;
	}

	@Override
	protected String getDefaultTemplate() {
		return DEFAULT_ALERT;
	}

	@Override
	protected String getHelpText() {
		return MessageFormat.format(_T("A {0} used as body of system alert email"), GROOVY_TEMPLATE_LINK);
	}

	@Override
	protected Map<String, String> getVariableHelp() {
		return CollectionUtils.newLinkedHashMap(
				"alert", _T("<a href='https://code.codibase.io/codibase/server/~files/main/server-core/src/main/java/io/codibase/server/model/Alert.java'>alert</a> to display"),
				"serverUrl", _T("root url of CodiBase server"));
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, _T("System Alert Template"));
	}

}