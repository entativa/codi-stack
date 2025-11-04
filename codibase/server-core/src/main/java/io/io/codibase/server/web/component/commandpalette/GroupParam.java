package io.codibase.server.web.component.commandpalette;

import java.util.LinkedHashMap;
import java.util.Map;

import io.codibase.server.CodiBase;
import io.codibase.server.service.GroupService;
import io.codibase.server.model.Group;
import io.codibase.server.web.page.admin.groupmanagement.GroupPage;

public class GroupParam extends ParamSegment {

	private static final long serialVersionUID = 1L;
	
	public GroupParam(boolean optional) {
		super(GroupPage.PARAM_GROUP, optional);
	}
	
	@Override
	public Map<String, String> suggest(String matchWith, 
			Map<String, String> paramValues, int count) {
		Map<String, String> suggestions = new LinkedHashMap<>();
		if (matchWith.length() == 0) 
			matchWith = null;
		for (Group group: CodiBase.getInstance(GroupService.class).query(matchWith, 0, count))
			suggestions.put(group.getName(), String.valueOf(group.getId()));
		return suggestions;
	}

	@Override
	public boolean isExactMatch(String matchWith, Map<String, String> paramValues) {
		GroupService groupService = CodiBase.getInstance(GroupService.class);
		try {
			Long groupId = Long.valueOf(matchWith);
			if (groupService.get(groupId) != null) 
				return true;
		} catch (NumberFormatException e) {
		}
		return false;
	}
		
}
