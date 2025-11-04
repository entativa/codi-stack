package io.codibase.server.web.component.branch.picker;

import io.codibase.server.CodiBase;
import io.codibase.server.service.ProjectService;
import io.codibase.server.model.Project;
import io.codibase.server.persistence.dao.Dao;
import io.codibase.server.security.SecurityUtils;
import io.codibase.server.security.permission.ReadCode;
import io.codibase.server.web.component.project.ProjectPicker;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import org.jspecify.annotations.Nullable;
import java.util.List;

public abstract class AffinalBranchPicker extends Panel {

	private Long projectId;
	
	private String branch;
	
	public AffinalBranchPicker(String id, Long projectId, String branch) {
		super(id);
		
		this.projectId = projectId;
		this.branch = branch;
	}
	
	private void newBranchPicker(@Nullable AjaxRequestTarget target) {
		BranchPicker branchPicker = new BranchPicker("branchPicker", new LoadableDetachableModel<Project>() {

			@Override
			protected Project load() {
				return getProject();
			}
			
		}, branch) {

			@Override
			protected void onSelect(AjaxRequestTarget target, String branch) {
				AffinalBranchPicker.this.onSelect(target, getProject(), branch);
			}

		};
		if (target != null) {
			replace(branchPicker);
			target.add(branchPicker);
		} else {
			add(branchPicker);
		}
	}
	
	private Project getProject() {
		return CodiBase.getInstance(Dao.class).load(Project.class, projectId);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ProjectPicker("projectPicker", new LoadableDetachableModel<List<Project>>() {

			@Override
			protected List<Project> load() {
				Project project = CodiBase.getInstance(Dao.class).load(Project.class, projectId);
				List<Project> affinals = project.getForkRoot().getForkDescendants();
				affinals.add(0, project.getForkRoot());
				affinals.retainAll(SecurityUtils.getAuthorizedProjects(new ReadCode()));
				
				return affinals;
			}
			
		}) {

			@Override
			protected void onSelect(AjaxRequestTarget target, Project project) {
				projectId = project.getId();
				branch = project.getDefaultBranch();
				newBranchPicker(target);
				AffinalBranchPicker.this.onSelect(target, project, branch);
			}

			@Override
			protected Project getCurrent() {
				return CodiBase.getInstance(ProjectService.class).load(projectId);
			}
			
		});
		newBranchPicker(null);
		
		setOutputMarkupId(true);
	}
	
	protected abstract void onSelect(AjaxRequestTarget target, Project project, String branch);
	
}
