package io.codibase.server.replica;

import io.codibase.commons.loader.ExtensionPoint;

@ExtensionPoint
public interface BuildStorageSyncer {
	
	void sync(Long projectId, Long buildNumber, String activeServer);
	
}
