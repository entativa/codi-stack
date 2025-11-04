package io.codibase.server.web.page.admin.buildsetting.agent;

import static io.codibase.agent.job.LogRequest.toZoneId;
import static io.codibase.server.util.DateUtils.getZoneId;
import static io.codibase.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Joiner;

import io.codibase.server.CodiBase;
import io.codibase.server.service.AgentService;
import io.codibase.server.web.resource.AgentLogResource;
import io.codibase.server.web.resource.AgentLogResourceReference;

public class AgentLogPage extends AgentDetailPage {

	private static final int MAX_DISPLAY_LINES = 5000;
	
	public AgentLogPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		if (getAgent().isOnline()) {
			Fragment fragment = new Fragment("content", "onlineFrag", this);
			
			fragment.add(new ResourceLink<Void>("download", 
					new AgentLogResourceReference(), AgentLogResource.paramsOf(getAgent())));
			
			List<String> lines = CodiBase.getInstance(AgentService.class).getAgentLog(getAgent());
			String content;
			if (lines.size() > MAX_DISPLAY_LINES) {
				fragment.add(new Label("warning", MessageFormat.format(_T("Too many log entries, displaying recent {0}"), MAX_DISPLAY_LINES)));
				content = Joiner.on("\n").join(toZoneId(lines.subList(lines.size()-MAX_DISPLAY_LINES, lines.size()), getZoneId()));
			} else {
				fragment.add(new WebMarkupContainer("warning").setVisible(false));
				content = Joiner.on("\n").join(toZoneId(lines, getZoneId()));
			}
			fragment.add(new Label("logContent", content));
			
			add(fragment);
		} else {
			add(new Fragment("content", "offlineFrag", this));
		}
	}

}
