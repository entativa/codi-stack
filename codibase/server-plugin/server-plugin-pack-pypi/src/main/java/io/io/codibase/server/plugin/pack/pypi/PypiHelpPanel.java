package io.codibase.server.plugin.pack.pypi;

import io.codibase.server.CodiBase;
import io.codibase.server.service.SettingService;
import io.codibase.server.web.component.codesnippet.CodeSnippetPanel;

import static io.codibase.server.web.translation.Translation._T;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

public class PypiHelpPanel extends Panel {
	
	private final String projectPath;
	
	public PypiHelpPanel(String id, String projectPath) {
		super(id);
		this.projectPath = projectPath;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		var registryUrl = getServerUrl() + "/" + projectPath + "/~" + PypiPackHandler.HANDLER_ID;
		add(new CodeSnippetPanel("addRepository", new AbstractReadOnlyModel<>() {
			@Override
			public String getObject() {
				return "" +
						"[distutils]\n" + 
						"index-servers=\n" +
						"  codibase\n\n" +
						"[codibase]\n" + 
						"repository=" + registryUrl + "\n" + 
						"username=<codibase_account_name>\n" +
						"password=<codibase_account_password>";
			}
			
		}));
		
		add(new CodeSnippetPanel("uploadCommand", Model.of("$ python3 -m twine upload --repository codibase /path/to/files_to_upload")));

		add(new CodeSnippetPanel("jobCommands", new LoadableDetachableModel<>() {
			@Override
			protected String load() {
				return "" +
						"# " + _T("Use job token to tell CodiBase the build publishing the package") + "\n" +
						"# " + _T("Job secret 'access-token' should be defined in project build setting as an access token with package write permission") + "\n\n" +
						"cat << EOF > $HOME/.pypirc\n" +
						"[distutils]\n" +
						"index-servers=\n" +
						"  codibase\n\n" +
						"[codibase]\n" +
						"repository=" + registryUrl + "\n" +
						"username=@job_token@\n" +
						"password=@secret:access-token@\n" +
						"EOF";
			}

		}));
	}

	private String getServerUrl() {
		return CodiBase.getInstance(SettingService.class).getSystemSetting().getServerUrl();
	}
	
}
