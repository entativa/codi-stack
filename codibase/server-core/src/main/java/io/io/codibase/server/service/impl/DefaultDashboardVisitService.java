package io.codibase.server.service.impl;

import java.util.List;

import javax.inject.Singleton;

import io.codibase.server.model.DashboardVisit;
import io.codibase.server.persistence.annotation.Transactional;
import io.codibase.server.service.DashboardVisitService;

@Singleton
public class DefaultDashboardVisitService extends BaseEntityService<DashboardVisit>
		implements DashboardVisitService {

	@Override
	public List<DashboardVisit> query() {
		return query(true);
	}

	@Override
	public int count() {
		return count(true);
	}
	
	@Transactional
	@Override
	public void createOrUpdate(DashboardVisit visit) {
		dao.persist(visit);
	}
	
}
