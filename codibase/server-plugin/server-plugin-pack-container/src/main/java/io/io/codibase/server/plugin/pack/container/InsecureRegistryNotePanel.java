package io.codibase.server.plugin.pack.container;

import io.codibase.server.CodiBase;
import io.codibase.server.service.SettingService;
import org.apache.wicket.markup.html.panel.Panel;

public class InsecureRegistryNotePanel extends Panel {
	
	public InsecureRegistryNotePanel(String id) {
		super(id);
	}

	private String getServerUrl() {
		return CodiBase.getInstance(SettingService.class).getSystemSetting().getServerUrl();
	}
	
	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(getServerUrl().startsWith("http://"));
	}
	
}
