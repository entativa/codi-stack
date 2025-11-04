package io.codibase.server.web.component.markdown;

import java.io.Serializable;

public interface ContentVersionSupport extends Serializable {
	
	long getVersion();
	
}
