package io.codibase.server.model.support.issue;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;

import io.codibase.server.annotation.IssueQuery;
import io.codibase.server.search.entity.issue.IssueQueryUpdater;
import io.codibase.server.util.usage.Usage;
import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.Markdown;

@Editable
public class IssueTemplate implements Serializable {

	private static final long serialVersionUID = 1L;

	private String issueQuery;

	private String issueDescription;
	
	@Editable(order=100, name="Applicable Issues", placeholder="All", 
			description="Optionally specify issues applicable for this template. "
			+ "Leave empty for all")
	@IssueQuery(withOrder = false)
	public String getIssueQuery() {
		return issueQuery;
	}

	public void setIssueQuery(String issueQuery) {
		this.issueQuery = issueQuery;
	}
	
	@Editable(order=200)
	@Markdown
	@NotEmpty
	public String getIssueDescription() {
		return issueDescription;
	}

	public void setIssueDescription(String issueDescription) {
		this.issueDescription = issueDescription;
	}

	public IssueQueryUpdater getQueryUpdater() {
		
		return new IssueQueryUpdater() {

			@Override
			protected Usage getUsage() {
				return new Usage().add("applicable issues");
			}

			@Override
			protected boolean isAllowEmpty() {
				return true;
			}

			@Override
			protected String getIssueQuery() {
				return issueQuery;
			}

			@Override
			protected void setIssueQuery(String issueQuery) {
				IssueTemplate.this.issueQuery = issueQuery;
			}
			
		};
		
	}
	
}
