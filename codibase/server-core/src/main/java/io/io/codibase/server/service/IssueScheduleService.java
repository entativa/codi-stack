package io.codibase.server.service;

import java.util.Collection;

import io.codibase.server.model.Issue;
import io.codibase.server.model.IssueSchedule;
import io.codibase.server.model.Iteration;

public interface IssueScheduleService extends EntityService<IssueSchedule> {
	
 	void syncIterations(Issue issue, Collection<Iteration> iterations);

    void create(IssueSchedule schedule);

    void populateSchedules(Collection<Issue> issues);
	
}