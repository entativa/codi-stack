package io.codibase.server.service.impl;

import static java.util.stream.Collectors.toSet;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import io.codibase.server.model.Agent;
import io.codibase.server.model.AgentToken;
import io.codibase.server.persistence.annotation.Sessional;
import io.codibase.server.persistence.annotation.Transactional;
import io.codibase.server.persistence.dao.EntityCriteria;
import io.codibase.server.service.AgentService;
import io.codibase.server.service.AgentTokenService;

@Singleton
public class DefaultAgentTokenService extends BaseEntityService<AgentToken> implements AgentTokenService {

	@Inject
	private AgentService agentService;

	@Transactional
	@Override
	public void createOrUpdate(AgentToken token) {
		dao.persist(token);
	}

	@Override
	public AgentToken find(String value) {
		EntityCriteria<AgentToken> criteria = newCriteria();
		criteria.add(Restrictions.eq(AgentToken.PROP_VALUE, value));
		criteria.setCacheable(true);
		return find(criteria);
	}

	@Sessional
	@Override
	public List<AgentToken> queryUnused() {
		var tokens = query();
		var usedTokens = agentService.query().stream().map(Agent::getToken).collect(toSet());
		tokens.removeAll(usedTokens);
		return tokens;
	}

	@Transactional
	@Override
	public void deleteUnused() {
		for (var token: queryUnused())
			delete(token);
	}

}
