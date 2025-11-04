package io.codibase.server.plugin.imports.youtrack;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Size;

import io.codibase.server.annotation.ChoiceProvider;
import io.codibase.server.annotation.ClassValidating;
import io.codibase.server.annotation.DependsOn;
import io.codibase.server.annotation.Editable;
import io.codibase.server.security.SecurityUtils;
import io.codibase.server.security.permission.CreateChildren;
import io.codibase.server.util.ComponentContext;
import io.codibase.server.validation.Validatable;
import io.codibase.server.web.editable.BeanEditor;

@Editable
@ClassValidating
public class ImportProjects implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;
	
	ImportServer server;

	private String parentCodiBaseProject;

	private boolean all;

	private List<String> youTrackProjects;
	
	private boolean populateTagMappings = true;

	@Editable(order=200, name="Parent CodiBase Project", description = "Optionally specify a CodiBase project " +
			"to be used as parent of imported projects. Leave empty to import as root projects")
	@ChoiceProvider("getParentCodiBaseProjectChoices")
	public String getParentCodiBaseProject() {
		return parentCodiBaseProject;
	}

	public void setParentCodiBaseProject(String parentCodiBaseProject) {
		this.parentCodiBaseProject = parentCodiBaseProject;
	}

	@SuppressWarnings("unused")
	private static List<String> getParentCodiBaseProjectChoices() {
		return SecurityUtils.getAuthorizedProjects(new CreateChildren()).stream()
				.map(it->it.getPath()).sorted().collect(Collectors.toList());
	}

	@Editable(order=300, name="Import All Projects")
	public boolean isAll() {
		return all;
	}

	public void setAll(boolean all) {
		this.all = all;
	}

	@Editable(order=500, name="YouTrack Projects to Import")
	@ChoiceProvider("getYouTrackProjectChoices")
	@DependsOn(property="all", value="false")
	@Size(min=1, message="At least one project should be selected")
	public List<String> getYouTrackProjects() {
		return youTrackProjects;
	}

	public void setYouTrackProjects(List<String> youTrackProjects) {
		this.youTrackProjects = youTrackProjects;
	}

	@SuppressWarnings("unused")
	private static List<String> getYouTrackProjectChoices() {
		BeanEditor editor = ComponentContext.get().getComponent().findParent(BeanEditor.class);
		ImportProjects projects = (ImportProjects) editor.getModelObject();
		return projects.server.listProjects();
	}

	@Editable(order=600, description = "Whether or not to pre-populate tag mappings in next page. " +
			"You may want to disable this if there are too many tags to display")
	public boolean isPopulateTagMappings() {
		return populateTagMappings;
	}

	public void setPopulateTagMappings(boolean populateTagMappings) {
		this.populateTagMappings = populateTagMappings;
	}
	
	public Collection<String> getImportProjects() {
		if (isAll())
			return server.listProjects();
		else
			return getYouTrackProjects();
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		if (parentCodiBaseProject == null && !SecurityUtils.canCreateRootProjects()) {
			context.disableDefaultConstraintViolation();
			var errorMessage = "No permission to import as root projects, please specify parent project";
			context.buildConstraintViolationWithTemplate(errorMessage)
					.addPropertyNode("parentCodiBaseProject")
					.addConstraintViolation();
			return false;
		} else {
			return true;
		}
	}

}
