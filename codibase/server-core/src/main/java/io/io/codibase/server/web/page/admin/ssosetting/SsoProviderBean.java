package io.codibase.server.web.page.admin.ssosetting;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import io.codibase.server.CodiBase;
import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.GroupChoice;
import io.codibase.server.annotation.UrlSegment;
import io.codibase.server.service.GroupService;
import io.codibase.server.model.SsoProvider;
import io.codibase.server.model.support.administration.sso.SsoConnector;
import io.codibase.server.web.page.security.SsoProcessPage;

@Editable
public class SsoProviderBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;

	private String defaultGroup;
	
	private SsoConnector connector;

	@Editable(order=100, description="Name of the provider will serve two purpose: "
	+ "<ul>"
	+ "<li>Display on login button"
	+ "<li>Form the authorization callback url which will be <i>&lt;server url&gt;/" + SsoProcessPage.MOUNT_PATH + "/" + SsoProcessPage.STAGE_CALLBACK + "/&lt;name&gt;</i>"
	+ "</ul>")
	@UrlSegment // will be used as part of callback url
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=200, name="Type")
	@NotNull
	public SsoConnector getConnector() {
		return connector;
	}

	public void setConnector(SsoConnector connector) {
		this.connector = connector;
	}

	@Editable(order=300, placeholder="No default group", description="Optionally add newly authenticated "
			+ "user to specified group if membership information is not available")
	@GroupChoice
	public String getDefaultGroup() {
		return defaultGroup;
	}

	public void setDefaultGroup(String defaultGroup) {
		this.defaultGroup = defaultGroup;
	}

	public void populate(SsoProvider provider) {
		provider.setName(name);
		provider.setConnector(connector);
		if (defaultGroup != null)
			provider.setDefaultGroup(CodiBase.getInstance(GroupService.class).find(defaultGroup));
	}

	public static SsoProviderBean of(SsoProvider provider) {
		SsoProviderBean bean = new SsoProviderBean();
		bean.name = provider.getName();
		bean.connector = provider.getConnector();
		bean.defaultGroup = provider.getDefaultGroup() != null ? provider.getDefaultGroup().getName() : null;
		return bean;
	}
}
