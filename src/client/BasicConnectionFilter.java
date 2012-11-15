package client;

import java.util.LinkedList;

public class BasicConnectionFilter implements IFilter {
	public ServerConnection serverConnection;
	public String message;
	public String command;
	public String type;
	public String action;
	public String parameter;
	public String[] arguments;
	public int sender;

	public BasicConnectionFilter(ServerConnection serverConnection) {
		this.serverConnection = serverConnection;
	}
	
	public LinkedList<ILogger> loggers = new LinkedList<ILogger>();

	/* (non-Javadoc)
	 * @see client.IFilter#pull()
	 */
	public String pull() {
		message = command = type = action = parameter = "";
		sender = -1;
		arguments = new String[0];
		if (serverConnection != null)
			message = serverConnection.pull();
		else
			return null;
		return decode(message);
	}
	
	public String decode(String message) {
		command = type = action = parameter = "";
		sender = -1;
		arguments = new String[0];
		if (message == null)
			return null;
		for (ILogger l : loggers)
			l.addLine(true, message);
		String[] dummy = message.split(" ", 2);
		if (dummy.length == 0) {
			return message;
		}
		command = dummy[0];
		if (command.length() > 3) {
			type = command.substring(0, 3);
			action = command.substring(3);
		}
		if (type.equals("Uni") || type.equals("Mod") || type.equals("Pub") || type.equals("All")) {
			dummy = dummy[1].split(" ", 2);
			sender = Integer.parseInt(dummy[0]);
		}
		if (dummy.length > 1) {
			parameter = dummy[1];
			arguments = dummy[1].split(" ", -1);
		}
		return message;
	}

	/* (non-Javadoc)
	 * @see client.IFilter#push(java.lang.String)
	 */
	public void push(String message) {
		for (ILogger l : loggers)
			l.addLine(false, message);
		if (serverConnection != null)
			serverConnection.push(message);
		else
			return;
	}
	
	public int asInt(int i) {
		return Integer.parseInt(arguments[i]);
	}
	public String asString(int i) {
		return arguments[i];
	}
}
