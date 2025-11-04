package io.codibase.server.web.page.project.pullrequests.detail;

import java.util.List;

import io.codibase.commons.loader.ExtensionPoint;
import io.codibase.server.model.PullRequest;

@ExtensionPoint
public interface PullRequestSummaryContribution {

	List<PullRequestSummaryPart> getParts(PullRequest request);
	
	int getOrder();
	
}
