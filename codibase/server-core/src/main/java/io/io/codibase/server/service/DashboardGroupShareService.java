package io.codibase.server.service;

import io.codibase.server.model.Dashboard;
import io.codibase.server.model.DashboardGroupShare;

import java.util.Collection;

public interface DashboardGroupShareService extends EntityService<DashboardGroupShare> {

	void syncShares(Dashboard dashboard, Collection<String> groupNames);

}
