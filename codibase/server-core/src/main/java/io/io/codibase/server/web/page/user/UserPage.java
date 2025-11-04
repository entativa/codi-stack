package io.codibase.server.web.page.user;

import static io.codibase.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Preconditions;

import io.codibase.server.CodiBase;
import io.codibase.server.ServerConfig;
import io.codibase.server.service.UserService;
import io.codibase.server.model.User;
import io.codibase.server.security.SecurityUtils;
import io.codibase.server.web.component.tabbable.PageTab;
import io.codibase.server.web.component.tabbable.Tabbable;
import io.codibase.server.web.page.admin.usermanagement.UserListPage;
import io.codibase.server.web.page.layout.LayoutPage;
import io.codibase.server.web.page.user.accesstoken.UserAccessTokensPage;
import io.codibase.server.web.page.user.authorization.UserAuthorizationsPage;
import io.codibase.server.web.page.user.avatar.UserAvatarPage;
import io.codibase.server.web.page.user.basicsetting.UserBasicSettingPage;
import io.codibase.server.web.page.user.emailaddresses.UserEmailAddressesPage;
import io.codibase.server.web.page.user.gpgkeys.UserGpgKeysPage;
import io.codibase.server.web.page.user.membership.UserMembershipsPage;
import io.codibase.server.web.page.user.password.UserPasswordPage;
import io.codibase.server.web.page.user.profile.UserProfilePage;
import io.codibase.server.web.page.user.querywatch.UserQueryWatchesPage;
import io.codibase.server.web.page.user.sshkeys.UserSshKeysPage;
import io.codibase.server.web.page.user.ssoaccounts.UserSsoAccountsPage;
import io.codibase.server.web.page.user.twofactorauthentication.UserTwoFactorAuthenticationPage;
import io.codibase.server.web.util.UserAware;

public abstract class UserPage extends LayoutPage implements UserAware {
	
	public static final String PARAM_USER = "user";
	
	protected final IModel<User> userModel;
	
	public UserPage(PageParameters params) {
		super(params);
		
		String userIdString = params.get(PARAM_USER).toString();
		if (StringUtils.isBlank(userIdString))
			throw new RestartResponseException(UserListPage.class);
		
		Long userId = Long.valueOf(userIdString);
		Preconditions.checkArgument(userId > 0);
		
		userModel = new LoadableDetachableModel<>() {

			@Override
			protected User load() {
				return getUserService().load(userId);
			}

		};
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.isAdministrator();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<PageTab> tabs = new ArrayList<>();
		
		var params = paramsOf(getUser());
		tabs.add(new PageTab(Model.of(_T("Profile")), Model.of("profile"), UserProfilePage.class, params));
		tabs.add(new PageTab(Model.of(_T("Basic Settings")), Model.of("info"), UserBasicSettingPage.class, params));
		if (!getUser().isServiceAccount()) 
			tabs.add(new PageTab(Model.of(_T("Email Addresses")), Model.of("mail"), UserEmailAddressesPage.class, params));		
		tabs.add(new PageTab(Model.of(_T("Edit Avatar")), Model.of("avatar"), UserAvatarPage.class, params));
		if (!getUser().isDisabled()) {
			if (!getUser().isServiceAccount())
				tabs.add(new PageTab(Model.of(_T("Password")), Model.of("password"), UserPasswordPage.class, params));
			tabs.add(new PageTab(Model.of(_T("Belonging Groups")), Model.of("group"), UserMembershipsPage.class, params));
			tabs.add(new PageTab(Model.of(_T("Authorized Projects")), Model.of("project"), UserAuthorizationsPage.class, params));
			if (CodiBase.getInstance(ServerConfig.class).getSshPort() != 0)
				tabs.add(new PageTab(Model.of(_T("SSH Keys")), Model.of("key"), UserSshKeysPage.class, params));
			tabs.add(new PageTab(Model.of(_T("GPG Keys")), Model.of("key"), UserGpgKeysPage.class, params));
			tabs.add(new PageTab(Model.of(_T("Access Tokens")), Model.of("token"), UserAccessTokensPage.class, params));
			if (!getUser().isServiceAccount() && getUser().isEnforce2FA())
				tabs.add(new PageTab(Model.of(_T("Two-factor Authentication")), Model.of("shield"), UserTwoFactorAuthenticationPage.class, params));
			if (!getUser().isServiceAccount()) {
				tabs.add(new PageTab(Model.of(_T("SSO Accounts")), Model.of("user"), UserSsoAccountsPage.class, params));
				tabs.add(new PageTab(Model.of(_T("Query Watches")), Model.of("bell"), UserQueryWatchesPage.class, params));
			}
		}
		
		add(new Tabbable("userTabs", tabs).setVisible(SecurityUtils.isAdministrator()));
	}
	
	@Override
	protected void onDetach() {
		userModel.detach();
		
		super.onDetach();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new UserCssResourceReference()));
	}
	
	@Override
	public User getUser() {
		return userModel.getObject();
	}
	
	private UserService getUserService() {
		return CodiBase.getInstance(UserService.class);
	}
	
	public static PageParameters paramsOf(User user) {
		PageParameters params = new PageParameters();
		params.add(PARAM_USER, user.getId());
		return params;
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		Fragment fragment = new Fragment(componentId, "topbarTitleFrag", this);
		fragment.add(new BookmarkablePageLink<Void>("users", UserListPage.class));
		fragment.add(new Label("userName", getUser().getDisplayName()));
		return fragment;
	}

}
