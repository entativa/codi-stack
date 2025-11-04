package io.codibase.server.model.support;

import javax.persistence.MappedSuperclass;

import io.codibase.server.model.AbstractEntity;
import io.codibase.server.model.Project;

@MappedSuperclass
public abstract class ProjectBelonging extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public abstract Project getProject();
		
}
