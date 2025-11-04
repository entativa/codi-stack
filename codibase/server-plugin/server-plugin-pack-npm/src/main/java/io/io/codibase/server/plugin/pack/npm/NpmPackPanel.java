package io.codibase.server.plugin.pack.npm;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.codibase.server.CodiBase;
import io.codibase.server.service.SettingService;
import io.codibase.server.model.Pack;
import io.codibase.server.web.component.codesnippet.CodeSnippetPanel;
import io.codibase.server.web.component.markdown.MarkdownViewer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import java.io.IOException;

import static io.codibase.server.web.translation.Translation._T;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;

public class NpmPackPanel extends GenericPanel<Pack> {
	
	public NpmPackPanel(String id, IModel<Pack> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		var registryUrl = getServerUrl() + "/" + getPack().getProject().getPath() + "/~" + NpmPackHandler.HANDLER_ID + "/";
		if (getPack().getName().contains("/")) {
			var scope = substringBefore(getPack().getName(), "/");
			add(new Label("registryConfig", "$ npm config set " + scope + ":registry " + registryUrl));
		} else {
			add(new Label("registryConfig", "$ npm config set registry " + registryUrl));
		}
		add(new Label("registryAuth", "$ npm config set -- '" + substringAfter(registryUrl, ":") + ":_authToken' \"codibase_access_token\""));
		add(new Label("installPack", "$ npm install " + getPack().getReference(false)));

		add(new CodeSnippetPanel("jobCommands", new LoadableDetachableModel<>() {

			@Override
			protected String load() {
				var registryUrl = getServerUrl() + "/" + getPack().getProject().getPath() + "/~npm/";
				String registryConfig;
				if (getPack().getName().contains("/")) {
					var scope = substringBefore(getPack().getName(), "/");
					registryConfig = "" +
							"# " + _T("Use @@ to reference scope in job commands to avoid being interpreted as variable") + "\n" +
							"npm config set @" + scope + ":registry " + registryUrl + "\n\n";
				} else {
					registryConfig = "npm config set registry " + registryUrl + "\n\n";
				}
				return registryConfig +
						"# " + _T("Use job token to tell CodiBase the build using the package") + "\n" +
						"# " + _T("Job secret 'access-token' should be defined in project build setting as an access token with package read permission") + "\n" +
						"npm config set -- '" + substringAfter(registryUrl, ":") + ":_authToken' \"@job_token@:@secret:access-token@\"\n\n" +
						"npm install";
			}

		}));
		
		var packData = (NpmData) getPack().getData();
		try {
			String readmeContent = null;
			var metadata = CodiBase.getInstance(ObjectMapper.class).readTree(packData.getMetadata());
			if (metadata.hasNonNull("readme")) {
				readmeContent = metadata.get("readme").asText();
				if (readmeContent.startsWith("ERROR:")) {
					readmeContent = null;	
				} 
			}
			if (readmeContent != null)
				add(new MarkdownViewer("readme", Model.of(readmeContent), null));
			else 
				add(new WebMarkupContainer("readme").setVisible(false));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Pack getPack() {
		return getModelObject();
	}

	private String getServerUrl() {
		return CodiBase.getInstance(SettingService.class).getSystemSetting().getServerUrl();
	}
	
}
