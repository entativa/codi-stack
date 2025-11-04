package io.codibase.server.codequality;

import java.util.Map;

import org.jspecify.annotations.Nullable;

import io.codibase.commons.loader.ExtensionPoint;
import io.codibase.server.model.Build;

@ExtensionPoint
public interface LineCoverageContribution {

	Map<Integer, CoverageStatus> getLineCoverages(Build build, String blobPath, @Nullable String reportName); 
	
}
