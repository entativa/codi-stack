package io.codibase.server.web.component.commandpalette;

import io.codibase.server.CodiBase;
import io.codibase.server.cluster.ClusterService;
import io.codibase.server.web.page.admin.ServerDetailPage;

import java.util.LinkedHashMap;
import java.util.Map;

public class ServerParam extends ParamSegment {

	private static final long serialVersionUID = 1L;
	
	public ServerParam() {
		super(ServerDetailPage.PARAM_SERVER, false);
	}
	
	@Override
	public Map<String, String> suggest(String matchWith, 
			Map<String, String> paramValues, int count) {
		Map<String, String> suggestions = new LinkedHashMap<>();
		if (matchWith.length() == 0) 
			matchWith = null;
		for (String server: CodiBase.getInstance(ClusterService.class).getServerAddresses()) {
			if (matchWith == null || server.toLowerCase().contains(matchWith.toLowerCase()))
				suggestions.put(server, server);
		}
		return suggestions;
	}

	@Override
	public boolean isExactMatch(String matchWith, Map<String, String> paramValues) {
		return CodiBase.getInstance(ClusterService.class).getServer(matchWith, false) != null;
	}
		
}
