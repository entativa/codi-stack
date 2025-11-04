package io.codibase.server.service.impl;

import static io.codibase.server.model.Stopwatch.PROP_ISSUE;
import static io.codibase.server.model.Stopwatch.PROP_USER;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import io.codibase.commons.utils.ExplicitException;
import io.codibase.server.model.Issue;
import io.codibase.server.model.IssueWork;
import io.codibase.server.model.Stopwatch;
import io.codibase.server.model.User;
import io.codibase.server.persistence.annotation.Sessional;
import io.codibase.server.persistence.annotation.Transactional;
import io.codibase.server.service.IssueWorkService;
import io.codibase.server.service.StopwatchService;
import io.codibase.server.util.DateUtils;

@Singleton
public class DefaultStopwatchService extends BaseEntityService<Stopwatch> implements StopwatchService {

	@Inject
	private IssueWorkService workService;

	@Override
	@Sessional
	public Stopwatch find(User user, Issue issue) {
		var criteria = newCriteria();
		criteria.add(Restrictions.eq(PROP_USER, user));
		criteria.add(Restrictions.eq(PROP_ISSUE, issue));
		return find(criteria);
	}
	
	@Transactional
	@Override
	public Stopwatch startWork(User user, Issue issue) {
		var watch = find(user, issue);
		if (watch == null) {
			watch = new Stopwatch();
			watch.setUser(user);
			watch.setIssue(issue);
			watch.setDate(new Date());
			dao.persist(watch);
			return watch;
		} else {
			throw new ExplicitException("Work already started");
		}
	}

	@Transactional
	@Override
	public void stopWork(Stopwatch stopwatch) {
		int spentMinutes = (int) ((System.currentTimeMillis() - stopwatch.getDate().getTime()) / 60000);
		if (spentMinutes > 0) {
			var localDate = DateUtils.toLocalDate(stopwatch.getDate());
			Date startOfDay = DateUtils.toDate(localDate.atStartOfDay());
			Date endOfDay = DateUtils.toDate(localDate.atTime(23, 59, 59));
			var works = workService.query(stopwatch.getUser(), stopwatch.getIssue(), startOfDay, endOfDay);
			works.sort((o1, o2) -> o2.getDate().compareTo(o1.getDate()));
			IssueWork workWithoutNote = null;
			for (var work: works) {
				if (work.getNote() == null) {
					workWithoutNote = work;
					break;
				}
			}
			if (workWithoutNote == null) {
				workWithoutNote = new IssueWork();
				workWithoutNote.setUser(stopwatch.getUser());
				workWithoutNote.setIssue(stopwatch.getIssue());
				workWithoutNote.setDate(new Date());
			}
			workWithoutNote.setMinutes(workWithoutNote.getMinutes() + spentMinutes);
			workService.createOrUpdate(workWithoutNote);
		}
		dao.remove(stopwatch);
	}
		
}