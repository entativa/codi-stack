package io.codibase.server.web.page.admin.emailtemplates;

import static io.codibase.server.model.support.administration.emailtemplates.EmailTemplates.DEFAULT_PASSWORD_RESET;
import static io.codibase.server.model.support.administration.emailtemplates.EmailTemplates.PROP_PASSWORD_RESET;
import static io.codibase.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.codibase.server.util.CollectionUtils;

public class PasswordResetTemplatePage extends AbstractTemplatePage {

	public PasswordResetTemplatePage(PageParameters params) {
		super(params);
	}

	@Override
	protected String getPropertyName() {
		return PROP_PASSWORD_RESET;
	}

	@Override
	protected String getDefaultTemplate() {
		return DEFAULT_PASSWORD_RESET;
	}

	@Override
	protected String getHelpText() {
		return MessageFormat.format(_T("A {0} used as body of password reset email"), GROOVY_TEMPLATE_LINK);
	}

	@Override
	protected Map<String, String> getVariableHelp() {
		return CollectionUtils.newLinkedHashMap(
				"user", _T("<a href=\"https://code.codibase.io/codibase/server/~files/main/server-core/src/main/java/io/codibase/server/model/User.java\">user</a> to reset password for"),
				"passwordResetUrl", _T("url to reset password"));
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, _T("Password Reset Template"));
	}

}