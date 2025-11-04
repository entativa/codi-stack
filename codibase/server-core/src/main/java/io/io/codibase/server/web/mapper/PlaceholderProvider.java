package io.codibase.server.web.mapper;

public interface PlaceholderProvider {

	String getPlaceholder(String mountSegment);
	
	String getOptionalPlaceholder(String mountSegment);
	
}
