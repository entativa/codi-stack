package io.codibase.server.model.support.role;

import java.io.Serializable;
import java.util.Collection;

import io.codibase.server.annotation.Editable;

@Editable
public interface IssueFieldSet extends Serializable {

	Collection<String> getIncludeFields();
	
}
