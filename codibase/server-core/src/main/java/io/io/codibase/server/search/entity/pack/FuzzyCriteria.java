package io.codibase.server.search.entity.pack;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.codibase.commons.utils.StringUtils;
import io.codibase.server.model.Pack;
import io.codibase.server.util.ProjectScope;
import io.codibase.server.util.criteria.Criteria;
import io.codibase.server.util.criteria.OrCriteria;

public class FuzzyCriteria extends Criteria<Pack> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public FuzzyCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Pack, Pack> from, CriteriaBuilder builder) {
		return parse(value).getPredicate(projectScope, query, from, builder);
	}

	@Override
	public boolean matches(Pack build) {
		return parse(value).matches(build);
	}
	
	@SuppressWarnings("unchecked")
	private Criteria<Pack> parse(String value) {
		return new OrCriteria<>(
				new NameCriteria("*" + value + "*", PackQueryLexer.Is),
				new VersionCriteria("*" + value + "*", PackQueryLexer.Is));
	}

	@Override
	public String toStringWithoutParens() {
		return "~" + StringUtils.escape(value, "~") + "~";
	}

}
