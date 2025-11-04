package io.codibase.server.service;

import io.codibase.server.model.Dashboard;
import io.codibase.server.model.DashboardUserShare;

import java.util.Collection;

public interface DashboardUserShareService extends EntityService<DashboardUserShare> {

	void syncShares(Dashboard dashboard, Collection<String> userNames);

}