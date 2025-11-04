package io.codibase.server.web.editable.buildspec.param.spec;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.codibase.server.buildspec.param.spec.ParamSpec;
import io.codibase.server.annotation.Editable;

@Editable
public class ParamSpecBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private ParamSpec paramSpec;

	// change Named("paramSpec") also if change name of this property 
	@Editable(name="Type", order=100)
	@NotNull(message="may not be empty")
	public ParamSpec getParamSpec() {
		return paramSpec;
	}

	public void setParamSpec(ParamSpec paramSpec) {
		this.paramSpec = paramSpec;
	}

}
