package io.codibase.server.model.support.role;

import java.util.Collection;

import io.codibase.server.CodiBase;
import io.codibase.server.service.SettingService;
import io.codibase.server.annotation.Editable;

@Editable(order=100, name="All")
public class AllIssueFields implements IssueFieldSet {

	private static final long serialVersionUID = 1L;

	@Override
	public Collection<String> getIncludeFields() {
		return CodiBase.getInstance(SettingService.class).getIssueSetting().getFieldNames();
	}

}
