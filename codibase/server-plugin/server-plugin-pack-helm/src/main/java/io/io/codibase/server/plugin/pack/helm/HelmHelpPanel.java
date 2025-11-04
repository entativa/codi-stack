package io.codibase.server.plugin.pack.helm;

import io.codibase.server.CodiBase;
import io.codibase.server.service.SettingService;
import io.codibase.server.web.component.codesnippet.CodeSnippetPanel;

import static io.codibase.server.web.translation.Translation._T;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

public class HelmHelpPanel extends Panel {
	
	private final String projectPath;
	
	public HelmHelpPanel(String id, String projectPath) {
		super(id);
		this.projectPath = projectPath;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		var registryUrl = getServerUrl() + "/" + projectPath + "/~" + HelmPackHandler.HANDLER_ID;
		add(new CodeSnippetPanel("pushChart", Model.of("$ curl -u <codibase_account_name>:<codibase_password_or_access_token> -X POST --upload-file /path/to/chart.tgz " + registryUrl)));
		
		add(new CodeSnippetPanel("jobCommands", Model.of("" +
				"# " + _T("Use job token to tell CodiBase the build pushing the chart") + "\n" +
				"# " + _T("Job secret 'access-token' should be defined in project build setting as an access token with package write permission") + "\n\n" +
				"curl -u @job_token@:@secret:access-token@ -X POST --upload-file /path/to/chart.tgz " + registryUrl)));
	}

	private String getServerUrl() {
		return CodiBase.getInstance(SettingService.class).getSystemSetting().getServerUrl();
	}
} 