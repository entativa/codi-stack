package io.codibase.server.model.support.pullrequest;

import javax.validation.constraints.NotEmpty;

import io.codibase.server.model.support.NamedQuery;
import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.PullRequestQuery;

@Editable
public class NamedPullRequestQuery implements NamedQuery {
	
	private static final long serialVersionUID = 1L;

	private String name;
	
	private String query;
	
	public NamedPullRequestQuery(String name, String query) {
		this.name = name;
		this.query = query;
	}
	
	public NamedPullRequestQuery() {
	}

	@Editable
	@NotEmpty
	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(placeholder="All")
	@PullRequestQuery(withCurrentUserCriteria=true)
	@Override
	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

}