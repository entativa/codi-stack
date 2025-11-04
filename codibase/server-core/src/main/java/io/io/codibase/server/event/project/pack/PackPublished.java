package io.codibase.server.event.project.pack;

import io.codibase.server.model.Pack;

public class PackPublished extends PackEvent {

	private static final long serialVersionUID = 1L;

	public PackPublished(Pack pack) {
		super(pack.getUser(), pack.getPublishDate(), pack);
	}
	
	@Override
	public String getActivity() {
		return "published";
	}

}
