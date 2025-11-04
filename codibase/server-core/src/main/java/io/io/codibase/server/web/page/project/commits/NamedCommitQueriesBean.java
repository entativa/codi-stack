package io.codibase.server.web.page.project.commits;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import io.codibase.server.model.support.NamedCommitQuery;
import io.codibase.server.web.component.savedquery.NamedQueriesBean;
import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.OmitName;

@Editable
public class NamedCommitQueriesBean extends NamedQueriesBean<NamedCommitQuery> {

	private static final long serialVersionUID = 1L;

	private List<NamedCommitQuery> queries = new ArrayList<>();

	@Override
	@NotNull
	@Editable
	@OmitName
	public List<NamedCommitQuery> getQueries() {
		return queries;
	}

	@Override
	public void setQueries(List<NamedCommitQuery> queries) {
		this.queries = queries;
	}

}