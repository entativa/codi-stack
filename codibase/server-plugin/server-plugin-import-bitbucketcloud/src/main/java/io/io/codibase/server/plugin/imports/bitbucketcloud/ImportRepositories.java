package io.codibase.server.plugin.imports.bitbucketcloud;

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
import io.codibase.server.util.EditContext;
import io.codibase.server.validation.Validatable;
import io.codibase.server.web.editable.BeanEditor;

@Editable
@ClassValidating
public class ImportRepositories extends ImportWorkspace implements Validatable {

	private static final long serialVersionUID = 1L;

	private String parentCodiBaseProject;

	private boolean all;

	private boolean includeForks;

	private List<String> bitbucketRepositories;

	@Editable(order=200, name="Parent CodiBase Project", description = "Optionally specify a CodiBase project " +
			"to be used as parent of imported repositories. Leave empty to import as root projects")
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

	@Editable(order=300, name="Import All Repositories")
	public boolean isAll() {
		return all;
	}

	public void setAll(boolean all) {
		this.all = all;
	}

	@Editable(order=400, description="Whether or not to import forked Bitbucket repositories")
	@DependsOn(property="all")
	public boolean isIncludeForks() {
		return includeForks;
	}

	public void setIncludeForks(boolean includeForks) {
		this.includeForks = includeForks;
	}

	@Editable(order=500, name="Bitbucket Repositories to Import")
	@ChoiceProvider("getBitbucketRepositoryChoices")
	@DependsOn(property="all", value="false")
	@Size(min=1, message="At least one repository should be selected")
	public List<String> getBitbucketRepositories() {
		return bitbucketRepositories;
	}

	public void setBitbucketRepositories(List<String> bitbucketRepositories) {
		this.bitbucketRepositories = bitbucketRepositories;
	}

	@SuppressWarnings("unused")
	private static List<String> getBitbucketRepositoryChoices() {
		BeanEditor editor = ComponentContext.get().getComponent().findParent(BeanEditor.class);
		ImportRepositories repositories = (ImportRepositories) editor.getModelObject();
		String workspace = (String) EditContext.get().getInputValue("workspace");
		return repositories.server.listRepositories(workspace, true);
	}

	public Collection<String> getImportRepositories() {
		if (isAll())
			return server.listRepositories(getWorkspace(), isIncludeForks());
		else
			return getBitbucketRepositories();
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
