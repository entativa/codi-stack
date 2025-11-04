package io.codibase.server.terminal;

import org.eclipse.jetty.websocket.api.Session;

import io.codibase.agent.Message;

public class AgentShell implements Shell {

	private final Terminal terminal;
	
	private final Session agent;
	
	public AgentShell(Terminal terminal, Session agent, String jobToken) {
		this.terminal = terminal;
		this.agent = agent;
		new Message(io.codibase.agent.MessageTypes.SHELL_OPEN, terminal.getSessionId() + ":" + jobToken).sendBy(agent);
	}
	
	public Terminal getTerminal() {
		return terminal;
	}

	@Override
	public void sendInput(String input) {
		new Message(io.codibase.agent.MessageTypes.SHELL_INPUT, terminal.getSessionId() + ":" + input).sendBy(agent);
	}

	@Override
	public void resize(int rows, int cols) {
		new Message(io.codibase.agent.MessageTypes.SHELL_RESIZE, terminal.getSessionId() + ":" + rows + ":" + cols).sendBy(agent);
	}

	@Override
	public void exit() {
		terminal.sendError("Shell exited");
		new Message(io.codibase.agent.MessageTypes.SHELL_EXIT, terminal.getSessionId()).sendBy(agent);
	}

}
