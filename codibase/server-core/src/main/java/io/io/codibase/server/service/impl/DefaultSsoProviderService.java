package io.codibase.server.service.impl;

import java.util.List;

import javax.inject.Singleton;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import io.codibase.server.model.SsoProvider;
import io.codibase.server.persistence.annotation.Sessional;
import io.codibase.server.persistence.annotation.Transactional;
import io.codibase.server.persistence.dao.EntityCriteria;
import io.codibase.server.service.SsoProviderService;

@Singleton
public class DefaultSsoProviderService extends BaseEntityService<SsoProvider> implements SsoProviderService {

	@Transactional
	@Override
	public void createOrUpdate(SsoProvider ssoProvider) {
		dao.persist(ssoProvider);
	}
	
	@Sessional
    @Override
    public SsoProvider find(String name) {
		var criteria = EntityCriteria.of(SsoProvider.class);
		criteria.add(Restrictions.eq(SsoProvider.PROP_NAME, name));
		return dao.find(criteria);
    }

	@Sessional
	public List<SsoProvider> query() {
		var criteria = EntityCriteria.of(SsoProvider.class);
		criteria.addOrder(Order.asc(SsoProvider.PROP_NAME));
		criteria.setCacheable(true);
		return query(criteria);
	}
	
}
