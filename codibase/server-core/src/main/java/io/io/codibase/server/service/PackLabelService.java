package io.codibase.server.service;

import io.codibase.server.model.Pack;
import io.codibase.server.model.PackLabel;

import java.util.Collection;

public interface PackLabelService extends EntityLabelService<PackLabel> {

	void create(PackLabel packLabel);
	
	void populateLabels(Collection<Pack> packs);
	
}
