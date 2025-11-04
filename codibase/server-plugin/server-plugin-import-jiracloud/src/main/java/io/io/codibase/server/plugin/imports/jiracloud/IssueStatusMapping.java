package io.codibase.server.plugin.imports.jiracloud;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotEmpty;

import io.codibase.server.CodiBase;
import io.codibase.server.service.SettingService;
import io.codibase.server.model.support.administration.GlobalIssueSetting;
import io.codibase.server.annotation.ChoiceProvider;
import io.codibase.server.annotation.Editable;

@Editable
public class IssueStatusMapping implements Serializable {

	private static final long serialVersionUID = 1L;

	private String jiraIssueStatus;
	
	private String oneDevIssueState;

	@Editable(order=100, name="JIRA Issue Status")
	@NotEmpty
	public String getJiraIssueStatus() {
		return jiraIssueStatus;
	}

	public void setJiraIssueStatus(String jiraIssueStatus) {
		this.jiraIssueStatus = jiraIssueStatus;
	}

	@Editable(order=200, name="CodiBase Issue State", description="CodiBase Issue State")
	@ChoiceProvider("getCodiBaseIssueStateChoices")
	@NotEmpty
	public String getCodiBaseIssueState() {
		return oneDevIssueState;
	}

	public void setCodiBaseIssueState(String oneDevIssueState) {
		this.oneDevIssueState = oneDevIssueState;
	}

	@SuppressWarnings("unused")
	private static List<String> getCodiBaseIssueStateChoices() {
		GlobalIssueSetting issueSetting = CodiBase.getInstance(SettingService.class).getIssueSetting();
		return issueSetting.getStateSpecs().stream().map(it->it.getName()).collect(Collectors.toList());
	}
	
}
