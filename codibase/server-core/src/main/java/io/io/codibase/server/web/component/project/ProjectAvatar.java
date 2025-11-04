package io.codibase.server.web.component.project;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;

import io.codibase.server.CodiBase;
import io.codibase.server.web.avatar.AvatarService;

public class ProjectAvatar extends WebComponent {

	private String url;
	
	public ProjectAvatar(String id, Long projectId) {
		super(id);

		url = getAvatarService().getProjectAvatarUrl(projectId);
	}
	
	private AvatarService getAvatarService() {
		return CodiBase.getInstance(AvatarService.class);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		setOutputMarkupId(true);
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		
		tag.setName("img");
		tag.append("class", "avatar", " ");
		tag.put("src", url);
	}

}
