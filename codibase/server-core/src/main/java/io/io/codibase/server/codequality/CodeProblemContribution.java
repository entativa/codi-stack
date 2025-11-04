package io.codibase.server.codequality;

import java.util.List;

import org.jspecify.annotations.Nullable;

import io.codibase.commons.loader.ExtensionPoint;
import io.codibase.server.model.Build;

@ExtensionPoint
public interface CodeProblemContribution {

	List<CodeProblem> getCodeProblems(Build build, String blobPath, @Nullable String reportName); 
	
}
