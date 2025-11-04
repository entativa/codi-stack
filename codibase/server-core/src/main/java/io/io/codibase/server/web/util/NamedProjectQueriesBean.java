package io.codibase.server.web.util;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import io.codibase.server.model.support.NamedProjectQuery;
import io.codibase.server.web.component.savedquery.NamedQueriesBean;
import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.OmitName;

@Editable
public class NamedProjectQueriesBean extends NamedQueriesBean<NamedProjectQuery> {

	private static final long serialVersionUID = 1L;

	private List<NamedProjectQuery> queries = new ArrayList<>();

	@NotNull
	@Editable
	@OmitName
	public List<NamedProjectQuery> getQueries() {
		return queries;
	}

	public void setQueries(List<NamedProjectQuery> queries) {
		this.queries = queries;
	}

}