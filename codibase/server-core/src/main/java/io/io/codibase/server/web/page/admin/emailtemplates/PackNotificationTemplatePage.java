package io.codibase.server.web.page.admin.emailtemplates;

import static io.codibase.server.model.support.administration.emailtemplates.EmailTemplates.PROP_PACK_NOTIFICATION;
import static io.codibase.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.codibase.server.util.CollectionUtils;

public class PackNotificationTemplatePage extends AbstractSimpleNotificationTemplatePage {

	public PackNotificationTemplatePage(PageParameters params) {
		super(params);
	}

	@Override
	protected String getPropertyName() {
		return PROP_PACK_NOTIFICATION;
	}
	
	@Override
	protected String getHelpText() {
		return MessageFormat.format(_T("A {0} used as body of package notification email"), GROOVY_TEMPLATE_LINK);
	}

	@Override
	protected Map<String, String> getVariableHelp() {
		return CollectionUtils.newLinkedHashMap("pack",
				_T("represents the <a href='https://code.codibase.io/codibase/server/~files/main/server-core/src/main/java/io/codibase/server/model/Pack.java' target='_blank'>package</a> object to be notified"));
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, _T("Package Notification Template"));
	}

}