package io.codibase.server.web.component.project;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.unbescape.html.HtmlEscape;

import io.codibase.server.model.Project;
import io.codibase.server.web.asset.icon.IconScope;
import io.codibase.server.web.component.floating.FloatingPanel;
import io.codibase.server.web.component.link.DropdownLink;
import io.codibase.server.web.component.project.selector.ProjectSelector;
import io.codibase.server.web.component.svg.SpriteImage;

public abstract class ProjectPicker extends DropdownLink {

	private final IModel<List<Project>> projectsModel; 
	
	public ProjectPicker(String id, IModel<List<Project>> projectsModel) {
		super(id);
	
		this.projectsModel = projectsModel;
	}

	@Override
	protected Component newContent(String id, FloatingPanel dropdown) {
		return new ProjectSelector(id, projectsModel) {

			@Override
			protected void onSelect(AjaxRequestTarget target, Project project) {
				dropdown.close();
				target.add(ProjectPicker.this);
				ProjectPicker.this.onSelect(target, project);
			}

			@Override
			protected Project getCurrent() {
				return ProjectPicker.this.getCurrent();
			}

		};
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		setEscapeModelStrings(false);
	}

	@Override
	public IModel<?> getBody() {
		return Model.of(String.format(""
				+ "<span class='project-picker'>"
				+ "  <svg class='icon'><use xlink:href='%s'/></svg>"
				+ "  <span>%s</span>"
				+ "  <svg class='icon rotate-90'><use xlink:href='%s'/></svg>"
				+ "</span>", 
				SpriteImage.getVersionedHref(IconScope.class, "project"),
				HtmlEscape.escapeHtml5(getCurrent().getPath()), 
				SpriteImage.getVersionedHref(IconScope.class, "arrow")));
	}

	@Override
	protected void onDetach() {
		projectsModel.detach();
		super.onDetach();
	}

	protected abstract void onSelect(AjaxRequestTarget target, Project project);
	
	protected abstract Project getCurrent();
	
}
