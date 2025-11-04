package io.codibase.server.plugin.imports.github;

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
public class GitHubModule extends AbstractPluginModule {

	static final String NAME = "GitHub";

	@Override
	protected void configure() {
		super.configure();
		
		contribute(ProjectImporterContribution.class, new ProjectImporterContribution() {

			@Override
			public Collection<ProjectImporter> getImporters() {
				return Lists.newArrayList(new GitHubProjectImporter());
			}

			@Override
			public int getOrder() {
				return 100;
			}
			
		});
		
		contribute(IssueImporterContribution.class, new IssueImporterContribution() {

			@Override
			public Collection<IssueImporter> getImporters() {
				return Lists.newArrayList(new GitHubIssueImporter());
			}

			@Override
			public int getOrder() {
				return 100;
			}
			
		});
	}
	
}
