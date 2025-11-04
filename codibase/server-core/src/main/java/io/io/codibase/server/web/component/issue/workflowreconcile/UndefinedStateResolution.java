package io.codibase.server.web.component.issue.workflowreconcile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import io.codibase.server.CodiBase;
import io.codibase.server.annotation.ChoiceProvider;
import io.codibase.server.annotation.DependsOn;
import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.OmitName;
import io.codibase.server.service.SettingService;
import io.codibase.server.model.support.administration.GlobalIssueSetting;

@Editable
public class UndefinedStateResolution implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum FixType {CHANGE_TO_ANOTHER_STATE, DELETE_THIS_STATE}
	
	private FixType fixType = FixType.CHANGE_TO_ANOTHER_STATE;
	
	private String newState;

	@Editable(order=50)
	@NotNull
	@OmitName
	public FixType getFixType() {
		return fixType;
	}

	public void setFixType(FixType fixType) {
		this.fixType = fixType;
	}

	@Editable(order=100)
	@ChoiceProvider("getStateChoices")
	@DependsOn(property="fixType", value="CHANGE_TO_ANOTHER_STATE")
	@OmitName
	@NotEmpty
	public String getNewState() {
		return newState;
	}

	public void setNewState(String newState) {
		this.newState = newState;
	}
		
	@SuppressWarnings("unused")
	private static List<String> getStateChoices() {
		GlobalIssueSetting issueSetting = CodiBase.getInstance(SettingService.class).getIssueSetting();
		return new ArrayList<>(issueSetting.getStateSpecMap().keySet());
	}
	
}
