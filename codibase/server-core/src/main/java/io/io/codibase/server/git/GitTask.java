package io.codibase.server.git;

import io.codibase.commons.utils.command.Commandline;

import java.io.IOException;

public interface GitTask<T> {

	T call(Commandline git) throws IOException;
	
}
