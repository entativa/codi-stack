package io.codibase.server.event.project.pack;

import java.util.Date;

import io.codibase.server.CodiBase;
import io.codibase.server.service.PackService;
import io.codibase.server.event.project.ProjectEvent;
import io.codibase.server.model.Pack;
import io.codibase.server.model.User;
import io.codibase.server.web.UrlService;

public abstract class PackEvent extends ProjectEvent {

	private static final long serialVersionUID = 1L;
	
	private final Long packId;
	
	public PackEvent(User user, Date date, Pack pack) {
		super(user, date, pack.getProject());
		packId = pack.getId();
	}

	public Pack getPack() {
		return CodiBase.getInstance(PackService.class).load(packId);
	}

	@Override
	public String getUrl() {
		return CodiBase.getInstance(UrlService.class).urlFor(getPack(), true);
	}
	
}
