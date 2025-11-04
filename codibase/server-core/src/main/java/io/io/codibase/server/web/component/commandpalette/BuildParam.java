package io.codibase.server.web.component.commandpalette;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.codibase.server.CodiBase;
import io.codibase.server.service.BuildService;
import io.codibase.server.model.Build;
import io.codibase.server.search.entity.build.BuildQuery;
import io.codibase.server.search.entity.build.BuildQueryParser;
import io.codibase.server.search.entity.build.FuzzyCriteria;
import io.codibase.server.search.entity.build.ReferenceCriteria;
import io.codibase.server.security.SecurityUtils;
import io.codibase.server.util.criteria.Criteria;
import io.codibase.server.web.page.project.builds.detail.BuildDetailPage;

public class BuildParam extends ParamSegment {

	private static final long serialVersionUID = 1L;
	
	public BuildParam(boolean optional) {
		super(BuildDetailPage.PARAM_BUILD, optional);
	}

	@Override
	public Map<String, String> suggest(String matchWith, 
			Map<String, String> paramValues, int count) {
		var project = ParsedUrl.getProject(paramValues);
		Map<String, String> suggestions = new LinkedHashMap<>();
		BuildService buildService = CodiBase.getInstance(BuildService.class);
		BuildQuery query;
		if (matchWith.length() == 0) {
			query = new BuildQuery();
		} else {	
			Criteria<Build> criteria;
			try {
				criteria = new ReferenceCriteria(project, matchWith, BuildQueryParser.Is);
			} catch (Exception e) {
				criteria = new FuzzyCriteria(matchWith);
			}
			query = new BuildQuery(criteria);
		}
		List<Build> builds = buildService.query(SecurityUtils.getSubject(), project, query, false, 0, count);
		
		for (Build build: builds) 
			suggestions.put(build.getSummary(project), String.valueOf(build.getNumber()));
		return suggestions;
	}

	@Override
	public boolean isExactMatch(String matchWith, Map<String, String> paramValues) {
		BuildService buildService = CodiBase.getInstance(BuildService.class);
		try {
			Long buildNumber = Long.valueOf(matchWith);
			if (buildService.find(ParsedUrl.getProject(paramValues), buildNumber) != null) 
				return true;
		} catch (NumberFormatException e) {
		}
		return false;
	}

}
