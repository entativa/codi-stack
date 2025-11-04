package io.codibase.server.web.util;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import io.codibase.server.model.support.build.NamedBuildQuery;
import io.codibase.server.web.component.savedquery.NamedQueriesBean;
import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.OmitName;

@Editable
public class NamedBuildQueriesBean extends NamedQueriesBean<NamedBuildQuery> {

	private static final long serialVersionUID = 1L;

	private List<NamedBuildQuery> queries = new ArrayList<>();

	@NotNull
	@Editable
	@OmitName
	public List<NamedBuildQuery> getQueries() {
		return queries;
	}

	public void setQueries(List<NamedBuildQuery> queries) {
		this.queries = queries;
	}

}