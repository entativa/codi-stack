package io.codibase.server.web.component.issue.workflowreconcile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.google.common.base.Preconditions;

import io.codibase.server.CodiBase;
import io.codibase.server.annotation.ChoiceProvider;
import io.codibase.server.annotation.DependsOn;
import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.OmitName;
import io.codibase.server.service.SettingService;
import io.codibase.server.model.support.administration.GlobalIssueSetting;
import io.codibase.server.model.support.issue.field.spec.FieldSpec;
import io.codibase.server.model.support.issue.field.spec.choicefield.ChoiceField;
import io.codibase.server.util.ComponentContext;
import io.codibase.server.web.component.issue.workflowreconcile.WorkflowReconcilePanel.UndefinedFieldValueContainer;

@Editable
public class UndefinedFieldValueResolution implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum FixType {CHANGE_TO_ANOTHER_VALUE, DELETE_THIS_VALUE}
	
	private FixType fixType = FixType.CHANGE_TO_ANOTHER_VALUE;
	
	private String newValue;
	
	@Editable(order=50)
	@OmitName
	@NotNull
	public FixType getFixType() {
		return fixType;
	}

	public void setFixType(FixType fixType) {
		this.fixType = fixType;
	}

	@Editable(order=100)
	@ChoiceProvider("getValueChoices")
	@DependsOn(property="fixType", value="CHANGE_TO_ANOTHER_VALUE")
	@OmitName
	@NotEmpty
	public String getNewValue() {
		return newValue;
	}

	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}

	@SuppressWarnings("unused")
	private static List<String> getValueChoices() {
		UndefinedFieldValueContainer container = ComponentContext.get().getComponent()
				.findParent(UndefinedFieldValueContainer.class); 
		GlobalIssueSetting issueSetting = CodiBase.getInstance(SettingService.class).getIssueSetting();
		FieldSpec fieldSpec = Preconditions.checkNotNull(issueSetting.getFieldSpec(container.getFieldName()));
		ComponentContext.push(new ComponentContext(container));
		try {
			return new ArrayList<>(((ChoiceField)fieldSpec).getChoiceProvider().getChoices(true).keySet());
		} finally {
			ComponentContext.pop();
		}
	}

}
