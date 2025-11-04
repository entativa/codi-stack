package io.codibase.server.search.entity.project;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.codibase.server.model.Project;
import io.codibase.server.util.ProjectScope;
import io.codibase.server.util.criteria.Criteria;

public class ForkRootsCriteria extends Criteria<Project> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Project, Project> from, CriteriaBuilder builder) {
		return builder.isNull(from.get(Project.PROP_FORKED_FROM));
	}

	@Override
	public boolean matches(Project project) {
		return project.getForkedFrom() == null;
	}

	@Override
	public String toStringWithoutParens() {
		return ProjectQuery.getRuleName(ProjectQueryLexer.ForkRoots);
	}

}
