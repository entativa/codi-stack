package io.codibase.server.search.entity.agent;

import static io.codibase.server.search.entity.agent.AgentQueryLexer.Is;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.codibase.commons.utils.StringUtils;
import io.codibase.server.model.Agent;
import io.codibase.server.util.ProjectScope;
import io.codibase.server.util.criteria.Criteria;
import io.codibase.server.util.criteria.OrCriteria;

public class FuzzyCriteria extends Criteria<Agent> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public FuzzyCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Agent, Agent> from, CriteriaBuilder builder) {
		return parse(value).getPredicate(projectScope, query, from, builder);
	}

	@Override
	public boolean matches(Agent agent) {
		return parse(value).matches(agent);
	}
	
	@SuppressWarnings("unchecked")
	private Criteria<Agent> parse(String value) {
		return new OrCriteria<>(
				new NameCriteria("*" + value + "*", Is),
				new IpAddressCriteria("*" + value + "*", Is),
				new OsCriteria("*" + value + "*", Is),
				new OsVersionCriteria("*" + value + "*", Is),
				new OsArchCriteria("*" + value + "*", Is));
	}

	@Override
	public String toStringWithoutParens() {
		return "~" + StringUtils.escape(value, "~") + "~";
	}

}
