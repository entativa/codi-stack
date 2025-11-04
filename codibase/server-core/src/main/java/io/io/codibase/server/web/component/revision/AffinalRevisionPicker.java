package io.codibase.server.web.component.revision;

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

public abstract class AffinalRevisionPicker extends Panel {

	private Long projectId;
	
	private String revision;
	
	public AffinalRevisionPicker(String id, Long repoId, String revision) {
		super(id);
		
		this.projectId = repoId;
		this.revision = revision;
	}
	
	private void newRevisionPicker(@Nullable AjaxRequestTarget target) {
		RevisionPicker revisionPicker = new RevisionPicker("revisionPicker", new LoadableDetachableModel<Project>() {

			@Override
			protected Project load() {
				return getProject();
			}
			
		}, revision) {

			@Override
			protected void onSelect(AjaxRequestTarget target, String revision) {
				AffinalRevisionPicker.this.onSelect(target, getProject(), revision);
			}

		};
		if (target != null) {
			replace(revisionPicker);
			target.add(revisionPicker);
		} else {
			add(revisionPicker);
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
				revision = project.getDefaultBranch();
				newRevisionPicker(target);
				AffinalRevisionPicker.this.onSelect(target, project, revision);
			}

			@Override
			protected Project getCurrent() {
				return CodiBase.getInstance(ProjectService.class).load(projectId);
			}
			
		});
		newRevisionPicker(null);
		
		setOutputMarkupId(true);
	}
	
	protected abstract void onSelect(AjaxRequestTarget target, Project project, String revision);
	
}
