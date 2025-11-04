package io.codibase.server.util;

public class FilenameUtils extends org.apache.commons.io.FilenameUtils {

	public static String sanitizeFileName(String fileName) {
		return fileName.replace("..", "_").replace('/', '_').replace('\\', '_');
	}
	
}