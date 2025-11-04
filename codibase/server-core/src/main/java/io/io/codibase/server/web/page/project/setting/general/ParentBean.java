package io.codibase.server.web.page.project.setting.general;

import java.io.Serializable;

import org.jspecify.annotations.Nullable;

import io.codibase.server.annotation.ProjectPath;
import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.ParentChoice;

@Editable
public class ParentBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String parentPath;

	@Editable(name="Parent Project", placeholder="No parent", description="Settings and permissions "
			+ "of parent project will be inherited by this project")
	@ProjectPath
	@ParentChoice
	public String getParentPath() {
		return parentPath;
	}

	public void setParentPath(@Nullable String parentPath) {
		this.parentPath = parentPath;
	}
	
}
