package io.codibase.server.web.component.branch.picker;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.unbescape.html.HtmlEscape;

import io.codibase.server.model.Project;
import io.codibase.server.web.asset.icon.IconScope;
import io.codibase.server.web.component.floating.FloatingPanel;
import io.codibase.server.web.component.link.DropdownLink;
import io.codibase.server.web.component.svg.SpriteImage;

public abstract class BranchPicker extends DropdownLink {

	private final IModel<Project> projectModel;
	
	private String branch;
	
	public BranchPicker(String id, IModel<Project> projectModel, String branch) {
		super(id);
		
		this.projectModel = projectModel;
		this.branch = branch;
	}

	@Override
	protected Component newContent(String id, FloatingPanel dropdown) {
		return new BranchSelector(id, projectModel, branch) {

			@Override
			protected void onSelect(AjaxRequestTarget target, String branch) {
				dropdown.close();
				BranchPicker.this.branch = branch;
				target.add(BranchPicker.this);
				
				BranchPicker.this.onSelect(target, branch);
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
				+ "<span class='branch-picker'>"
				+ "  <svg class='icon'><use xlink:href='%s'/></svg>"
				+ "  <span>%s</span> "
				+ "  <svg class='rotate-90 icon'><use xlink:href='%s'/></svg>"
				+ "</span>", 
				SpriteImage.getVersionedHref(IconScope.class, "branch"),
				branch!=null?HtmlEscape.escapeHtml5(branch):"<i>choose</i>", 
				SpriteImage.getVersionedHref(IconScope.class, "arrow")));
	}

	@Override
	protected void onDetach() {
		projectModel.detach();
		super.onDetach();
	}

	protected abstract void onSelect(AjaxRequestTarget target, String branch);
	
}
