package io.codibase.server.web.component.commandpalette;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.codibase.server.CodiBase;
import io.codibase.server.service.IssueService;
import io.codibase.server.model.Issue;
import io.codibase.server.search.entity.issue.FuzzyCriteria;
import io.codibase.server.search.entity.issue.IssueQuery;
import io.codibase.server.search.entity.issue.IssueQueryParser;
import io.codibase.server.search.entity.issue.ReferenceCriteria;
import io.codibase.server.security.SecurityUtils;
import io.codibase.server.util.ProjectScope;
import io.codibase.server.util.criteria.Criteria;
import io.codibase.server.web.page.project.issues.detail.IssueDetailPage;

public class IssueParam extends ParamSegment {

	private static final long serialVersionUID = 1L;
	
	public IssueParam(boolean optional) {
		super(IssueDetailPage.PARAM_ISSUE, optional);
	}

	@Override
	public Map<String, String> suggest(String matchWith, 
			Map<String, String> paramValues, int count) {
		Map<String, String> suggestions = new LinkedHashMap<>();
		List<Issue> issues;
		var project = ParsedUrl.getProject(paramValues);
		ProjectScope projectScope = new ProjectScope(project, false, false);
		IssueService issueService = CodiBase.getInstance(IssueService.class);
		var subject = SecurityUtils.getSubject();
		if (matchWith.length() == 0) {
			issues = issueService.query(subject, projectScope, new IssueQuery(), false, 0, count);
		} else {
			Criteria<Issue> criteria;
			try {
				criteria = new ReferenceCriteria(project, matchWith, IssueQueryParser.Is);
			} catch (Exception e) {
				criteria = new FuzzyCriteria(matchWith);
			}
			issues = issueService.query(subject, projectScope, new IssueQuery(criteria), false, 0, count);
		}
		for (Issue issue: issues) 
			suggestions.put(issue.getSummary(project), String.valueOf(issue.getNumber()));
		return suggestions;
	}

	@Override
	public boolean isExactMatch(String matchWith, Map<String, String> paramValues) {
		IssueService issueService = CodiBase.getInstance(IssueService.class);
		try {
			Long issueNumber = Long.valueOf(matchWith);
			if (issueService.find(ParsedUrl.getProject(paramValues), issueNumber) != null) 
				return true;
		} catch (NumberFormatException e) {
		}
		return false;
	}

}
