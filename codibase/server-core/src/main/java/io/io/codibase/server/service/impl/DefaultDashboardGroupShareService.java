package io.codibase.server.service.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

import io.codibase.server.service.DashboardGroupShareService;
import io.codibase.server.service.GroupService;
import io.codibase.server.model.Dashboard;
import io.codibase.server.model.DashboardGroupShare;
import io.codibase.server.model.Group;
import io.codibase.server.persistence.annotation.Transactional;

@Singleton
public class DefaultDashboardGroupShareService extends BaseEntityService<DashboardGroupShare>
		implements DashboardGroupShareService {
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultDashboardGroupShareService.class);

	@Inject
	private GroupService groupService;

	@Transactional
	@Override
	public void syncShares(Dashboard dashboard, Collection<String> groupNames) {
    	Map<String, DashboardGroupShare> syncMap = new HashMap<>();
    	for (String groupName: groupNames) {
    		Group group = groupService.find(groupName);
    		if (group == null) {
    			logger.warn("Unable to find group: " + groupName);
    		} else {
    			DashboardGroupShare share = new DashboardGroupShare();
    			share.setGroup(group);
    			share.setDashboard(dashboard);
    			syncMap.put(groupName, share);
    		}
    	}

    	Map<String, DashboardGroupShare> currentMap = new HashMap<>();
		dashboard.getGroupShares().forEach(share -> 
				currentMap.put(share.getGroup().getName(), share));
		
		MapDifference<String, DashboardGroupShare> diff = Maps.difference(currentMap, syncMap);
		
		diff.entriesOnlyOnLeft().values().forEach(share -> delete(share));
		diff.entriesOnlyOnRight().values().forEach(share -> dao.persist(share));		
	}

	@Override
	public List<DashboardGroupShare> query() {
		return query(true);
	}

	@Override
	public int count() {
		return count(true);
	}
	
}
