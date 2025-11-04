package io.codibase.server.plugin.report.markdown;

import com.google.common.collect.Sets;
import io.codibase.commons.loader.AbstractPluginModule;
import io.codibase.commons.loader.ImplementationProvider;
import io.codibase.server.CodiBase;
import io.codibase.server.buildspec.step.PublishReportStep;
import io.codibase.server.cluster.ClusterTask;
import io.codibase.server.service.BuildService;
import io.codibase.server.service.ProjectService;
import io.codibase.server.model.Build;
import io.codibase.server.model.PullRequest;
import io.codibase.server.replica.BuildStorageSyncer;
import io.codibase.server.security.SecurityUtils;
import io.codibase.server.web.WebApplicationConfigurator;
import io.codibase.server.web.mapper.ProjectPageMapper;
import io.codibase.server.web.page.project.builds.detail.BuildTab;
import io.codibase.server.web.page.project.builds.detail.BuildTabContribution;
import io.codibase.server.web.page.project.pullrequests.detail.PullRequestSummaryContribution;
import io.codibase.server.web.page.project.pullrequests.detail.PullRequestSummaryPart;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.codibase.commons.utils.LockUtils.read;
import static io.codibase.server.model.Build.getProjectRelativeDirPath;
import static io.codibase.server.util.DirectoryVersionUtils.isVersionFile;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class MarkdownModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		contribute(ImplementationProvider.class, new ImplementationProvider() {

			@Override
			public Class<?> getAbstractClass() {
				return PublishReportStep.class;
			}
			
			@Override
			public Collection<Class<?>> getImplementations() {
				return Sets.newHashSet(PublishMarkdownReportStep.class, PublishPullRequestMarkdownReportStep.class);
			}
			
		});
		
		contribute(BuildTabContribution.class, new BuildTabContribution() {
			
			@Override
			public List<BuildTab> getTabs(Build build) {
				Long projectId = build.getProject().getId();
				Long buildNumber = build.getNumber();
				
				return getProjectService().runOnActiveServer(projectId, new GetBuildTabs(projectId, buildNumber)).stream()
						.filter(it->SecurityUtils.canAccessReport(build, it.getTitle()))
						.collect(Collectors.toList());
			}
			
			@Override
			public int getOrder() {
				return 300;
			}
			
		});
		
		contribute(PullRequestSummaryContribution.class, new PullRequestSummaryContribution() {

			@Override
			public List<PullRequestSummaryPart> getParts(PullRequest request) {
				List<PullRequestSummaryPart> parts = new ArrayList<>();
				Long projectId = request.getProject().getId();
				for (Build build: request.getCurrentBuilds()) {
					Long buildNumber = build.getNumber();
					for (PullRequestSummaryPart part: getProjectService().runOnActiveServer(projectId, new GetPullRequestSummaryParts(projectId, buildNumber))) {
						if (SecurityUtils.canAccessReport(build, part.getReportName()))
							parts.add(part);
					}
				}
				return parts;
			}

			@Override
			public int getOrder() {
				return 100;
			}
			
		});
		
		contribute(WebApplicationConfigurator.class, application -> application.mount(new ProjectPageMapper(
				"${project}/~builds/${build}/markdown/${report}", 
				MarkdownReportPage.class)));

		contribute(BuildStorageSyncer.class, ((projectId, buildNumber, activeServer) -> {
			getProjectService().syncDirectory(projectId, 
					getProjectRelativeDirPath(buildNumber) + "/" + PublishMarkdownReportStep.CATEGORY,
					PublishMarkdownReportStep.getReportLockName(projectId, buildNumber), activeServer);
			getProjectService().syncDirectory(projectId, 
					getProjectRelativeDirPath(buildNumber) + "/" + PublishPullRequestMarkdownReportStep.CATEGORY,
					PublishPullRequestMarkdownReportStep.getReportLockName(projectId, buildNumber), activeServer);
		}));
		
	}
	
	private ProjectService getProjectService() {
		return CodiBase.getInstance(ProjectService.class);
	}

	private static class GetBuildTabs implements ClusterTask<List<BuildTab>> {

		private static final long serialVersionUID = 1L;
		
		private final Long projectId;
		
		private final Long buildNumber;
		
		public GetBuildTabs(Long projectId, Long buildNumber) {
			this.projectId = projectId;
			this.buildNumber = buildNumber;
		}

		@Override
		public List<BuildTab> call() {
			return read(PublishMarkdownReportStep.getReportLockName(projectId, buildNumber), () -> {
				List<BuildTab> tabs = new ArrayList<>();
				File categoryDir = new File(CodiBase.getInstance(BuildService.class).getBuildDir(projectId, buildNumber), PublishMarkdownReportStep.CATEGORY);
				if (categoryDir.exists()) {
					for (File reportDir: categoryDir.listFiles()) {
						if (!isVersionFile(reportDir))
							tabs.add(new MarkdownReportTab(reportDir.getName()));
					}
				}
				Collections.sort(tabs, (o1, o2) -> o1.getTitle().compareTo(o1.getTitle()));
				return tabs;
			});
		}
		
	}
	
	private static class GetPullRequestSummaryParts implements ClusterTask<List<PullRequestSummaryPart>> {

		private static final long serialVersionUID = 1L;

		private final Long projectId;
		
		private final Long buildNumber;
		
		private GetPullRequestSummaryParts(Long projectId, Long buildNumber) {
			this.projectId = projectId;
			this.buildNumber = buildNumber;
		}
		
		@Override
		public List<PullRequestSummaryPart> call() {
			return read(PublishPullRequestMarkdownReportStep.getReportLockName(projectId, buildNumber), () -> {
				List<PullRequestSummaryPart> parts = new ArrayList<>();
				File categoryDir = new File(CodiBase.getInstance(BuildService.class).getBuildDir(projectId, buildNumber), PublishPullRequestMarkdownReportStep.CATEGORY);
				if (categoryDir.exists()) {
					for (File reportDir: categoryDir.listFiles()) {
						if (!isVersionFile(reportDir))
							parts.add(new PullRequestSummaryMarkdownPart(projectId, buildNumber, reportDir.getName()));
					}
				}
				return parts;
			});
		}
		
	}
}
