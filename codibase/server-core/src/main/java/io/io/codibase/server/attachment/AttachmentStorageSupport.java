package io.codibase.server.attachment;

import io.codibase.server.model.Project;

public interface AttachmentStorageSupport {
	
	Project getAttachmentProject();
	
	String getAttachmentGroup();
	
}
