package io.codibase.server.service;

import io.codibase.server.model.DashboardVisit;

public interface DashboardVisitService extends EntityService<DashboardVisit> {

	void createOrUpdate(DashboardVisit visit);

}