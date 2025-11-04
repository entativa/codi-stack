package io.codibase.server.plugin.imports.youtrack;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotEmpty;

import io.codibase.server.CodiBase;
import io.codibase.server.service.SettingService;
import io.codibase.server.model.support.administration.GlobalIssueSetting;
import io.codibase.server.buildspecmodel.inputspec.InputSpec;
import io.codibase.server.model.support.issue.field.spec.FieldSpec;
import io.codibase.server.annotation.ChoiceProvider;
import io.codibase.server.annotation.Editable;

@Editable
public class IssueFieldMapping implements Serializable {

	private static final long serialVersionUID = 1L;

	private String youTrackIssueField;
	
	private String oneDevIssueField;

	@Editable(order=100, name="YouTrack Issue Field")
	@NotEmpty
	public String getYouTrackIssueField() {
		return youTrackIssueField;
	}

	public void setYouTrackIssueField(String youTrackIssueField) {
		this.youTrackIssueField = youTrackIssueField;
	}

	@Editable(order=200, name="CodiBase Issue Field")
	@ChoiceProvider("getCodiBaseIssueFieldChoices")
	@NotEmpty
	public String getCodiBaseIssueField() {
		return oneDevIssueField;
	}

	public void setCodiBaseIssueField(String oneDevIssueField) {
		this.oneDevIssueField = oneDevIssueField;
	}

	@SuppressWarnings("unused")
	private static List<String> getCodiBaseIssueFieldChoices() {
		List<String> choices = new ArrayList<>();
		GlobalIssueSetting issueSetting = CodiBase.getInstance(SettingService.class).getIssueSetting();
		for (FieldSpec field: issueSetting.getFieldSpecs()) {
			if (field.getType().equals(InputSpec.ENUMERATION)) {
				for (String value: field.getPossibleValues()) 
					choices.add(field.getName() + "::" + value);
			} else {
				choices.add(field.getName());
			}
		}
		return choices;
	}
	
}
