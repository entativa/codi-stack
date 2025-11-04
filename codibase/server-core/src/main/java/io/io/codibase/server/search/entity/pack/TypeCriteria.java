package io.codibase.server.search.entity.pack;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.codibase.server.model.Pack;
import io.codibase.server.util.ProjectScope;
import io.codibase.server.util.criteria.Criteria;

public class TypeCriteria extends Criteria<Pack> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	private final int operator;
	
	public TypeCriteria(String value, int operator) {
		this.value = value;
		this.operator = operator;
	}

	public String getValue() {
		return value;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Pack, Pack> from, CriteriaBuilder builder) {
		var predicate = builder.equal(from.get(Pack.PROP_TYPE), value);
		if (operator == PackQueryLexer.IsNot)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public boolean matches(Pack pack) {
		var matches = pack.getType().equals(value);
		if (operator == PackQueryLexer.IsNot)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Pack.NAME_TYPE) + " " 
				+ PackQuery.getRuleName(operator) + " " 
				+ quote(value);
	}

}
