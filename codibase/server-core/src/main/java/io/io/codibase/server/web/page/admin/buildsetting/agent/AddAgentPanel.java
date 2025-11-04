package io.codibase.server.web.page.admin.buildsetting.agent;

import static io.codibase.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import io.codibase.server.CodiBase;
import io.codibase.server.service.AgentTokenService;
import io.codibase.server.service.SettingService;
import io.codibase.server.model.AgentToken;
import io.codibase.server.web.component.link.copytoclipboard.CopyToClipboardLink;
import io.codibase.server.web.component.tabbable.AjaxActionTab;
import io.codibase.server.web.component.tabbable.Tab;
import io.codibase.server.web.component.tabbable.Tabbable;

class AddAgentPanel extends Panel {

	public AddAgentPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		List<Tab> tabs = new ArrayList<>();
		
		tabs.add(new AjaxActionTab(Model.of(_T("Run via Docker Container"))) {

			@Override
			protected void onSelect(AjaxRequestTarget target, Component tabLink) {
				Component content = newDockerInstructions();
				target.add(content);
				AddAgentPanel.this.replace(content);
			}
			
		});
		tabs.add(new AjaxActionTab(Model.of(_T("Run on Bare Metal/Virtual Machine"))) {

			@Override
			protected void onSelect(AjaxRequestTarget target, Component tabLink) {
				Component content = newBareMetalInstructions();
				target.add(content);
				AddAgentPanel.this.replace(content);
			}
			
		});

		add(new Tabbable("tabs", tabs));
		
		add(newDockerInstructions());
	}
	
	private Component newDockerInstructions() {
		Fragment fragment = new Fragment("instructions", "dockerInstructionsFrag", this);
		fragment.add(new AjaxLink<Void>("showCommand") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				AgentToken token = new AgentToken();
				CodiBase.getInstance(AgentTokenService.class).createOrUpdate(token);
				StringBuilder builder = new StringBuilder("docker run -t -v /var/run/docker.sock:/var/run/docker.sock -v $(pwd)/agent/work:/agent/work -e serverUrl=");
				builder.append(CodiBase.getInstance(SettingService.class).getSystemSetting().getServerUrl());
				builder.append(" -e agentToken=").append(token.getValue()).append(" -h myagent").append(" 1dev/agent");
				Fragment commandFragment = new Fragment("command", "dockerCommandFrag", AddAgentPanel.this);
				commandFragment.add(new Label("command", builder.toString()));
				commandFragment.add(new CopyToClipboardLink("copy", Model.of(builder.toString())));
				fragment.replace(commandFragment);
				target.add(fragment);
			}
			
		});
		fragment.add(new WebMarkupContainer("command").setVisible(false));
		fragment.setOutputMarkupId(true);
		return fragment;
	}
	
	private Component newBareMetalInstructions() {
		Fragment fragment = new Fragment("instructions", "bareMetalInstructionsFrag", this);
		fragment.add(new ExternalLink("agentZip", "/~downloads/agent.zip"));
		fragment.add(new ExternalLink("agentTgz", "/~downloads/agent.tar.gz"));
		fragment.setOutputMarkupId(true);
		return fragment;
	}
	
}
