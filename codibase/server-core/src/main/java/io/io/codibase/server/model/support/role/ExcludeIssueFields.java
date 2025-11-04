package io.codibase.server.model.support.role;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.validation.constraints.Size;

import io.codibase.server.CodiBase;
import io.codibase.server.service.SettingService;
import io.codibase.server.annotation.ChoiceProvider;
import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.OmitName;

@Editable(order=200, name="All except")
public class ExcludeIssueFields implements IssueFieldSet {

	private static final long serialVersionUID = 1L;

	private List<String> excludeFields = new ArrayList<>();

	@Editable(name="Excluded Fields")
	@ChoiceProvider("getFieldChoices")
	@OmitName
	@Size(min=1, message = "At least one field needs to be specified")
	public List<String> getExcludeFields() {
		return excludeFields;
	}

	public void setExcludeFields(List<String> excludeFields) {
		this.excludeFields = excludeFields;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getFieldChoices() {
		return CodiBase.getInstance(SettingService.class).getIssueSetting().getFieldNames();
	}

	@Override
	public Collection<String> getIncludeFields() {
		Collection<String> fields = CodiBase.getInstance(SettingService.class).getIssueSetting().getFieldNames();
		fields.removeAll(excludeFields);
		return fields;
	}

}
