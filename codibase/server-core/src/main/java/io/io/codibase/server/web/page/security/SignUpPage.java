package io.codibase.server.web.page.security;

import static io.codibase.server.model.User.PROP_NOTIFY_OWN_EVENTS;
import static io.codibase.server.model.User.PROP_SERVICE_ACCOUNT;
import static io.codibase.server.web.page.security.SignUpBean.PROP_EMAIL_ADDRESS;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Sets;

import io.codibase.commons.utils.match.StringMatcher;
import io.codibase.server.CodiBase;
import io.codibase.server.service.EmailAddressService;
import io.codibase.server.service.MembershipService;
import io.codibase.server.service.SettingService;
import io.codibase.server.service.UserService;
import io.codibase.server.model.EmailAddress;
import io.codibase.server.model.Group;
import io.codibase.server.model.Membership;
import io.codibase.server.model.User;
import io.codibase.server.model.support.administration.SecuritySetting;
import io.codibase.server.persistence.TransactionService;
import io.codibase.server.security.SecurityUtils;
import io.codibase.server.util.Path;
import io.codibase.server.util.PathNode;
import io.codibase.server.util.patternset.PatternSet;
import io.codibase.server.web.editable.BeanContext;
import io.codibase.server.web.editable.BeanEditor;
import io.codibase.server.web.page.HomePage;
import io.codibase.server.web.page.simple.SimplePage;

public class SignUpPage extends SimplePage {
	
	public SignUpPage(PageParameters params) {
		super(params);
		
		if (!getSecuritySetting().isEnableSelfRegister())
			throw new UnauthenticatedException("User sign-up is disabled");
		if (getLoginUser() != null)
			throw new IllegalStateException("Can not sign up a user while signed in");
	}
	
	private SecuritySetting getSecuritySetting() {
		return CodiBase.getInstance(SettingService.class).getSecuritySetting();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		SignUpBean bean = new SignUpBean();
		BeanEditor editor = BeanContext.edit("editor", bean, Sets.newHashSet(PROP_SERVICE_ACCOUNT, PROP_NOTIFY_OWN_EVENTS), true);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				User userWithSameName = getUserService().findByName(bean.getName());
				if (userWithSameName != null) {
					editor.error(new Path(new PathNode.Named(User.PROP_NAME)),
							"Login name already used by another account");
				} 

				var invalidEmailAddress = false;
				if (getSecuritySetting().getAllowedSelfRegisterEmailDomain() != null) {
					var emailDomain = StringUtils.substringAfter(bean.getEmailAddress(), "@").toLowerCase();
					var patternSet = PatternSet.parse(getSecuritySetting().getAllowedSelfRegisterEmailDomain().toLowerCase());
					if (!patternSet.matches(new StringMatcher(), emailDomain)) {
						editor.error(new Path(new PathNode.Named(PROP_EMAIL_ADDRESS)),
								"This email domain is not accepted for self sign-up");
						invalidEmailAddress = true;
					}
				}
				if (!invalidEmailAddress && getEmailAddressService().findByValue(bean.getEmailAddress()) != null) {
					editor.error(new Path(new PathNode.Named(PROP_EMAIL_ADDRESS)),
							"Email address already used by another user");
				} 
				if (editor.isValid()) {
					User user = new User();
					user.setName(bean.getName());
					user.setFullName(bean.getFullName());
					user.setPassword(getPasswordService().encryptPassword(bean.getPassword()));
					
					EmailAddress emailAddress = new EmailAddress();
					emailAddress.setValue(bean.getEmailAddress());
					emailAddress.setOwner(user);

					var defaultLoginGroup = getSettingService().getSecuritySetting().getDefaultGroup();
					
					getTransactionService().run(() -> {
						getUserService().create(user);
						getEmailAddressService().create(emailAddress);
						if (defaultLoginGroup != null) 
							createMembership(user, defaultLoginGroup);
					});
					
					Session.get().success("Account sign up successfully");
					SecurityUtils.getSubject().runAs(user.getPrincipals());
					setResponsePage(HomePage.class);
				}
			}
			
		};
		form.add(editor);
		
		form.add(new SubmitLink("save"));
		form.add(new Link<Void>("cancel") {

			@Override
			public void onClick() {
				throw new RestartResponseException(HomePage.class);
			}
			
		});
		add(form);
	}

	private PasswordService getPasswordService() {
		return CodiBase.getInstance(PasswordService.class);
	}
	
	private UserService getUserService() {
		return CodiBase.getInstance(UserService.class);
	}
	
	private EmailAddressService getEmailAddressService() {
		return CodiBase.getInstance(EmailAddressService.class);
	}

	private SettingService getSettingService() {
		return CodiBase.getInstance(SettingService.class);
	}

	private TransactionService getTransactionService() {
		return CodiBase.getInstance(TransactionService.class);
	}

	private MembershipService getMembershipService() {
		return CodiBase.getInstance(MembershipService.class);
	}
	
	private void createMembership(User user, Group group) {
		var membership = new Membership();
		membership.setUser(user);
		membership.setGroup(group);
		user.getMemberships().add(membership);
		getMembershipService().create(membership);
	}

	@Override
	protected String getTitle() {
		return "Sign Up";
	}

	@Override
	protected String getSubTitle() {
		return "Enter your details to create your account";
	}

}
