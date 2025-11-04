package io.codibase.server.service.impl;

import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import io.codibase.server.model.SsoAccount;
import io.codibase.server.model.SsoProvider;
import io.codibase.server.persistence.annotation.Sessional;
import io.codibase.server.persistence.annotation.Transactional;
import io.codibase.server.persistence.dao.EntityCriteria;
import io.codibase.server.service.SsoAccountService;

@Singleton
public class DefaultSsoAccountService extends BaseEntityService<SsoAccount> implements SsoAccountService {

	@Transactional
	@Override
	public void create(SsoAccount ssoAccount) {
		dao.persist(ssoAccount);
	}

	@Sessional
	@Override
	public SsoAccount find(SsoProvider provider, String subject) {
		var criteria = EntityCriteria.of(SsoAccount.class);
		criteria.add(Restrictions.eq(SsoAccount.PROP_PROVIDER, provider));
		criteria.add(Restrictions.eq(SsoAccount.PROP_SUBJECT, subject));
		return dao.find(criteria);
	}
	
}
