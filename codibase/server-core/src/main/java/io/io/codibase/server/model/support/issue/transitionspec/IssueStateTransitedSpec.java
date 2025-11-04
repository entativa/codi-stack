package io.codibase.server.model.support.issue.transitionspec;

import static io.codibase.server.web.translation.Translation._T;

import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.IssueQuery;

@Editable(order=550, name="State of an issue is transited")
public class IssueStateTransitedSpec extends AutoSpec {

	private static final long serialVersionUID = 1L;
		
	@Editable(order=9900, name="Applicable Issues", placeholder="All", description="Optionally specify issues "
			+ "applicable for this transition. Leave empty for all issues")
	@IssueQuery(withOrder = false, withCurrentIssueCriteria = true)
	@Override
	public String getIssueQuery() {
		return super.getIssueQuery();
	}

	public void setIssueQuery(String issueQuery) {
		super.setIssueQuery(issueQuery);
	}

	@Override
	public String getTriggerDescription() {
		return _T("state of an issue is transited");
	}
	
}
