package io.codibase.server.plugin.imports.gitlab;

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
public class IssueLabelMapping implements Serializable {

	private static final long serialVersionUID = 1L;

	private String gitLabIssueLabel;
	
	private String oneDevIssueField;

	@Editable(order=100, name="GitLab Issue Label")
	@NotEmpty
	public String getGitLabIssueLabel() {
		return gitLabIssueLabel;
	}

	public void setGitLabIssueLabel(String gitLabIssueLabel) {
		this.gitLabIssueLabel = gitLabIssueLabel;
	}

	@Editable(order=200, name="CodiBase Issue Field", description="Specify a custom field of Enum type")
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
			}
		}
		return choices;
	}
	
}
