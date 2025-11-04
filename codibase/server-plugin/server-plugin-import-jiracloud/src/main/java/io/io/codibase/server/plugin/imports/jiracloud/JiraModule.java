package io.codibase.server.plugin.imports.jiracloud;

import java.util.Collection;

import com.google.common.collect.Lists;

import io.codibase.commons.loader.AbstractPluginModule;
import io.codibase.server.imports.IssueImporter;
import io.codibase.server.imports.IssueImporterContribution;
import io.codibase.server.imports.ProjectImporter;
import io.codibase.server.imports.ProjectImporterContribution;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class JiraModule extends AbstractPluginModule {

	static final String NAME = "JIRA Cloud";
	
	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
		contribute(ProjectImporterContribution.class, new ProjectImporterContribution() {

			@Override
			public Collection<ProjectImporter> getImporters() {
				return Lists.newArrayList(new JiraProjectImporter());
			}

			@Override
			public int getOrder() {
				return 350;
			}
			
		});

		contribute(IssueImporterContribution.class, new IssueImporterContribution() {

			@Override
			public Collection<IssueImporter> getImporters() {
				return Lists.newArrayList(new JiraIssueImporter());
			}

			@Override
			public int getOrder() {
				return 350;
			}
			
		});
	}

}
