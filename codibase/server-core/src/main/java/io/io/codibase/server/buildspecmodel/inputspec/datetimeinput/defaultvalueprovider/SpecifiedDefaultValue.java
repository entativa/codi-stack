package io.codibase.server.buildspecmodel.inputspec.datetimeinput.defaultvalueprovider;

import java.util.Date;

import javax.validation.constraints.NotNull;

import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.OmitName;
import io.codibase.server.annotation.WithTime;

@Editable(order=100, name="Use specified default value")
public class SpecifiedDefaultValue implements DefaultValueProvider {

	private static final long serialVersionUID = 1L;

	private Date value;

	@Editable(name="Specified default value")
	@NotNull(message="may not be empty")
	@WithTime
	@OmitName
	public Date getValue() {
		return value;
	}

	public void setValue(Date value) {
		this.value = value;
	}

	@Override
	public Date getDefaultValue() {
		return getValue();
	}

}
