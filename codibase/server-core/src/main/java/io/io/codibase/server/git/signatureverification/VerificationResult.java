package io.codibase.server.git.signatureverification;

import org.apache.wicket.Component;
import org.eclipse.jgit.revwalk.RevObject;

public interface VerificationResult {
	
	Component renderDetail(String componentId, RevObject revObject);
	
}
