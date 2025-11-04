package io.codibase.server.xodus;

import io.codibase.server.model.Issue;

import java.util.Map;

public interface IssueInfoService {

	Map<Long, String> getDailyStates(Issue issue, Long fromDay, Long toDay);
	
	Map<Long, Integer> getDailySpentTimes(Issue issue, Long fromDay, Long toDay);

}
