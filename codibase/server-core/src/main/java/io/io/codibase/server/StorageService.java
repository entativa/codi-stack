package io.codibase.server;

import io.codibase.server.annotation.NoDBAccess;

import java.io.File;

public interface StorageService {

	File initLfsDir(Long projectId);

	File initArtifactsDir(Long projectId, Long buildNumber);

	@NoDBAccess
	File initPacksDir(Long projectId);

}
