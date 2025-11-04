package io.codibase.server.plugin.pack.nuget;

import io.codibase.server.CodiBase;
import io.codibase.server.service.SettingService;
import io.codibase.server.model.Pack;
import io.codibase.server.web.component.codesnippet.CodeSnippetPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import static io.codibase.server.web.translation.Translation._T;

import java.nio.charset.StandardCharsets;

public class NugetPackPanel extends GenericPanel<Pack> {
	
	public NugetPackPanel(String id, IModel<Pack> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		var registryUrl = getServerUrl() + "/" + getPack().getProject().getPath() + "/~" + NugetPackHandler.HANDLER_ID + "/index.json";
		add(new Label("addSource", "$ dotnet nuget add source --name codibase --username <codibase_account_name> --password <codibase_password_or_access_token> --store-password-in-clear-text " + registryUrl));
		add(new Label("addPack", "$ dotnet add package " + getPack().getName() + " -v " + getPack().getVersion()));
		
		add(new CodeSnippetPanel("jobCommands", new LoadableDetachableModel<String>() {
			@Override
			protected String load() {
				return "" +
						"# " + _T("Use job token to tell CodiBase the build using the package") + "\n" +
						"# " + _T("Job secret 'access-token' should be defined in project build setting as an access token with package read permission") + "\n" +
						"dotnet nuget add source --name codibase --username @job_token@ --password @secret:access-token@ --store-password-in-clear-text " + registryUrl;
			}
			
		}));
		add(new CodeSnippetPanel("nuspec", new LoadableDetachableModel<>() {
			@Override
			protected String load() {
				var data = (NugetData) getPack().getData();
				return new String(data.getMetadata(), StandardCharsets.UTF_8);
			}
			
		}));
	}

	private Pack getPack() {
		return getModelObject();
	}

	private String getServerUrl() {
		return CodiBase.getInstance(SettingService.class).getSystemSetting().getServerUrl();
	}
	
}
