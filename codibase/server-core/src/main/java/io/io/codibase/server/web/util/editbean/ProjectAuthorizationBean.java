package io.codibase.server.web.util.editbean;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.ProjectChoice;
import io.codibase.server.annotation.RoleChoice;

@Editable
public class ProjectAuthorizationBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String projectPath;
	
	private List<String> roleNames;

	@Editable(order=100, name="Project")
	@ProjectChoice
	@NotEmpty
	public String getProjectPath() {
		return projectPath;
	}

	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
	}

	@Editable(order=200, name="Role")
	@RoleChoice
	@Size(min=1, message="At least one role is required")
	public List<String> getRoleNames() {
		return roleNames;	
	}

	public void setRoleNames(List<String> roleNames) {
		this.roleNames = roleNames;
	}
	
}
