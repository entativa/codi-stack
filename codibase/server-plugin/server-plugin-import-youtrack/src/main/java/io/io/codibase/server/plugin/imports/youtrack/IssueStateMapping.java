package io.codibase.server.plugin.imports.youtrack;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotEmpty;

import io.codibase.server.CodiBase;
import io.codibase.server.service.SettingService;
import io.codibase.server.model.support.administration.GlobalIssueSetting;
import io.codibase.server.model.support.issue.StateSpec;
import io.codibase.server.annotation.ChoiceProvider;
import io.codibase.server.annotation.Editable;

@Editable
public class IssueStateMapping implements Serializable {

	private static final long serialVersionUID = 1L;

	private String youTrackIssueState;
	
	private String oneDevIssueState;

	@Editable(order=100, name="YouTrack Issue State")
	@NotEmpty
	public String getYouTrackIssueState() {
		return youTrackIssueState;
	}

	public void setYouTrackIssueState(String youTrackIssueState) {
		this.youTrackIssueState = youTrackIssueState;
	}

	@Editable(order=200, name="CodiBase Issue State")
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
		List<String> choices = new ArrayList<>();
		GlobalIssueSetting issueSetting = CodiBase.getInstance(SettingService.class).getIssueSetting();
		for (StateSpec state: issueSetting.getStateSpecs()) 
			choices.add(state.getName());
		return choices;
	}
	
}
