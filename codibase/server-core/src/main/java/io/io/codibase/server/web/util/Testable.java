package io.codibase.server.web.util;

import java.io.Serializable;

import io.codibase.commons.utils.TaskLogger;

public interface Testable<T extends Serializable> {
	
	void test(T data, TaskLogger logger);
	
}
