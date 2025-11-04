package io.codibase.server.buildspec;

import io.codibase.server.annotation.Editable;

import java.io.Serializable;

@Editable
public interface NamedElement extends Serializable {

	String getName();
	
}
