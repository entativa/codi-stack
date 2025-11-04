package io.codibase.server.plugin.pack.helm;

import org.apache.wicket.Component;
import org.apache.wicket.model.LoadableDetachableModel;

import io.codibase.server.CodiBase;
import io.codibase.server.service.PackService;
import io.codibase.server.model.Pack;
import io.codibase.server.model.Project;
import io.codibase.server.pack.PackSupport;

public class HelmPackSupport implements PackSupport {

	public static final String TYPE = "Helm";
	
	@Override
	public int getOrder() {
		return 500;
	}

	@Override
	public String getPackType() {
		return TYPE;
	}

	@Override
	public String getPackIcon() {
		return "helm";
	}

	@Override
	public String getReference(Pack pack, boolean withProject) {
		var reference = pack.getName() + "-" + pack.getVersion();
		if (withProject)
			reference = pack.getProject().getPath() + ":" + reference;
		return reference;
	}

	@Override
	public Component renderContent(String componentId, Pack pack) {
		var packId = pack.getId();
		return new HelmPackPanel(componentId, new LoadableDetachableModel<>() {
			@Override
			protected Pack load() {
				return CodiBase.getInstance(PackService.class).load(packId);
			}
		});
	}

	@Override
	public Component renderHelp(String componentId, Project project) {
		return new HelmHelpPanel(componentId, project.getPath());
	}
}
