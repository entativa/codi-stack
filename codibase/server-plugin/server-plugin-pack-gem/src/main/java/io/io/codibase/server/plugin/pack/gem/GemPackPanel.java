package io.codibase.server.plugin.pack.gem;

import io.codibase.server.CodiBase;
import io.codibase.server.service.SettingService;
import io.codibase.server.model.Pack;
import io.codibase.server.util.UrlUtils;
import io.codibase.server.web.component.codesnippet.CodeSnippetPanel;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.net.MalformedURLException;
import java.net.URL;

import static io.codibase.server.web.translation.Translation._T;
import static java.nio.charset.StandardCharsets.UTF_8;

public class GemPackPanel extends GenericPanel<Pack> {
	
	public GemPackPanel(String id, IModel<Pack> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		var pack = getPack();
		var serverUrl = getServerUrl();
		String protocol;
		try {
			var parsedUrl = new URL(serverUrl);
			protocol = parsedUrl.getProtocol();
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		var installCommand = String.format("$ gem install %s --version \"%s\" --source \"%s://<codibase_account_name>:<codibase_password>@%s/%s/~rubygems\"", 
				pack.getName(), pack.getVersion(), protocol, UrlUtils.getServer(serverUrl), pack.getProject().getPath());
		add(new CodeSnippetPanel("install", Model.of(installCommand)));
		
		var gemfileContent = String.format("" +
						"source \"%s/%s/~rubygems\" do\n" +
						"	gem \"%s\", \"%s\"\n" +
						"end", serverUrl, pack.getProject().getPath(), pack.getName(), pack.getVersion());
		add(new CodeSnippetPanel("gemfile", Model.of(gemfileContent)));
		
		var resolveDependencyCommands = String.format("" +
				"# " + _T("Use job token to tell CodiBase the build using the package") + "\n" +
				"# " + _T("Job secret 'access-token' should be defined in project build setting as an access token with package read permission") + "\n\n" +
				"bundle config set --global %s/%s/~rubygems/ @job_token@:@secret:access-token@\n" +
				"bundle install", serverUrl, pack.getProject().getPath());
		add(new CodeSnippetPanel("resolveDependencies", Model.of(resolveDependencyCommands)));
		
		var data = (GemData) pack.getData();
		add(new CodeSnippetPanel("metainfo", Model.of(new String(data.getMetadata(), UTF_8))));
	}

	private Pack getPack() {
		return getModelObject();
	}

	private String getServerUrl() {
		return CodiBase.getInstance(SettingService.class).getSystemSetting().getServerUrl();
	}
	
}
