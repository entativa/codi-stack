package io.codibase.server.search.entity.agent;

import java.util.Collection;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.codibase.server.CodiBase;
import io.codibase.server.service.AgentService;
import io.codibase.server.model.Agent;
import io.codibase.server.util.ProjectScope;

public class OnlineCriteria extends StatusCriteria {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Agent, Agent> from, CriteriaBuilder builder) {
		Path<?> attribute = from.get(Agent.PROP_ID);
		var agentService = CodiBase.getInstance(AgentService.class);
		Collection<Long> agentIds = agentService.getOnlineAgents();
		if (!agentIds.isEmpty())
			return attribute.in(agentIds);
		else
			return builder.disjunction();
	}

	@Override
	public boolean matches(Agent agent) {
		return agent.isOnline();
	}

	@Override
	public String toStringWithoutParens() {
		return AgentQuery.getRuleName(AgentQueryLexer.Online);
	}

}
