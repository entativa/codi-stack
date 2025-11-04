package io.codibase.server.markdown;

import static io.codibase.server.entityreference.ReferenceUtils.transformReferences;

import org.jspecify.annotations.Nullable;

import org.apache.wicket.request.cycle.RequestCycle;
import org.jsoup.nodes.Document;
import org.unbescape.html.HtmlEscape;

import io.codibase.server.CodiBase;
import io.codibase.server.service.BuildService;
import io.codibase.server.entityreference.BuildReference;
import io.codibase.server.entityreference.IssueReference;
import io.codibase.server.entityreference.PullRequestReference;
import io.codibase.server.model.Project;
import io.codibase.server.web.UrlService;
import io.codibase.server.web.component.markdown.SuggestionSupport;
import io.codibase.server.web.page.project.blob.render.BlobRenderContext;
import io.codibase.server.web.page.project.builds.detail.dashboard.BuildDashboardPage;
import io.codibase.server.web.page.project.issues.detail.IssueActivitiesPage;
import io.codibase.server.web.page.project.pullrequests.detail.activities.PullRequestActivitiesPage;

public class ReferenceProcessor implements HtmlProcessor {

	@Override
	public void process(Document document, @Nullable Project project,
						@Nullable BlobRenderContext blobRenderContext,
						@Nullable SuggestionSupport suggestionSupport,
						boolean forExternal) {
		transformReferences(document, project, (reference, text) -> {
			text = HtmlEscape.escapeHtml5(text);
			if (reference instanceof IssueReference) {
				String url;
				if (RequestCycle.get() != null)
					url = RequestCycle.get().urlFor(IssueActivitiesPage.class, IssueActivitiesPage.paramsOf(reference.getProject(), reference.getNumber())).toString();
				else
					url = CodiBase.getInstance(UrlService.class).urlForIssue(reference.getProject(), reference.getNumber(), true);
				return String.format("<a href='%s' class='issue reference' data-reference='%s'>%s</a>",
						url, reference, text);
			} else if (reference instanceof BuildReference) {
				String url;
				if (RequestCycle.get() != null)
					url = RequestCycle.get().urlFor(BuildDashboardPage.class, BuildDashboardPage.paramsOf(reference.getProject(), reference.getNumber())).toString();
				else
					url = CodiBase.getInstance(UrlService.class).urlForBuild(reference.getProject(), reference.getNumber(), true);
				var build = CodiBase.getInstance(BuildService.class).find(reference.getProject(), reference.getNumber());
				if (build != null && build.getVersion() != null)
					text += " (" + HtmlEscape.escapeHtml5(build.getVersion()) + ")";
				return String.format("<a href='%s' class='build reference' data-reference='%s'>%s</a>",
						url, reference, text);
			} else if (reference instanceof PullRequestReference) {
				String url;
				if (RequestCycle.get() != null)
					url = RequestCycle.get().urlFor(PullRequestActivitiesPage.class, PullRequestActivitiesPage.paramsOf(reference.getProject(), reference.getNumber())).toString();
				else
					url = CodiBase.getInstance(UrlService.class).urlForPullRequest(reference.getProject(), reference.getNumber(), true);
				return String.format("<a href='%s' class='pull-request reference' data-reference='%s'>%s</a>",
						url, reference, text);
			} else {
				return text;
			}
		});
	}

}
