package io.codibase.server.search.entity.pack;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;

import io.codibase.commons.utils.match.WildcardUtils;
import io.codibase.server.CodiBase;
import io.codibase.server.service.ProjectService;
import io.codibase.server.model.Pack;
import io.codibase.server.util.ProjectScope;
import io.codibase.server.util.criteria.Criteria;

public class ProjectCriteria extends Criteria<Pack> {

	private static final long serialVersionUID = 1L;
	
	private final String projectPath;
	
	private final int operator;

	public ProjectCriteria(String projectPath, int operator) {
		this.projectPath = projectPath;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Pack, Pack> from, CriteriaBuilder builder) {
		var predicate = CodiBase.getInstance(ProjectService.class).getPathMatchPredicate(
				builder, from.join(Pack.PROP_PROJECT, JoinType.INNER), projectPath);
		if (operator == PackQueryLexer.IsNot)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public boolean matches(Pack pack) {
		var matches = WildcardUtils.matchPath(projectPath, pack.getProject().getPath());
		if (operator == PackQueryLexer.IsNot)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Pack.NAME_PROJECT) + " " 
				+ PackQuery.getRuleName(operator) + " " 
				+ quote(projectPath);
	}

}
