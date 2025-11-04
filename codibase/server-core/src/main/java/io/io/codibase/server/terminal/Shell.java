package io.codibase.server.terminal;

public interface Shell {

	void sendInput(String input);
	
	void resize(int rows, int cols);
	
	void exit();

}
