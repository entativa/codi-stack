package io.codibase.server.service;

import io.codibase.server.model.Pack;
import io.codibase.server.model.PackBlob;
import io.codibase.server.model.PackBlobReference;

public interface PackBlobReferenceService extends EntityService<PackBlobReference> {

	void create(PackBlobReference blobReference);
	
	void createIfNotExist(Pack pack, PackBlob packBlob);
		
}
