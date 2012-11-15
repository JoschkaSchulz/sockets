/**
 * ConnectionPool
 * 
 * Diese Klasse h‰lt alle Verbindungen (Connections und kontrolliert ob 
 * eine der Verbindungen ein Befehl an den Server geschickt hat. Anschlieﬂend
 * wird der Befehl behandelt und die Verbindung bekommt die Antwort.
 */

package pop3.server;

import java.util.ArrayList;

public class ConnectionPool {
	public UserPool userPool = new UserPool();
	public ArrayList<Connection> connections = new ArrayList<Connection>();
	public void newConnection(Connection connection) {
		connections.add(connection);
	}
	public void recv(Connection connection, String message) {
		int state = 0;
		boolean processed = false;
		
		//Checks the recived massage
		if (connection.isCommandMode) {
			String[] res = message.split(" ", 2);
			connection.data.put("Command", (res.length >= 1) ? res[0] : "");
			connection.data.put("Param", (res.length >= 2) ? res[1] : "");
			connection.data.put("Chunk", "");
			state = 0;
		} else if (message.equals(".")) {
			connection.isCommandMode = true;
			state = 1;
		} else {
			String chunk = connection.data.get("Chunk");
			connection.data.put("Chunk", chunk.length() > 0 ? chunk + "\n" + message : message);
			state = 2;
		}
		
		//Get the command and param
		String command = connection.data.get("Command");
		String param = connection.data.get("Param");
		
		//Checks wich command was recived
		if (connection.data.containsKey("Login"))
			state += 4;
		if (state == 0 && command.equals("USER")) {
			connection.data.put("User", param);
			state = 3;
			//connection.sendMessage("+OK name is a valid mailbox");
			//connection.sendMessage("-ERR never heard of mailbox name");
			processed = true;
		}
		if (state == 0 && command.equals("PASS")) {
			connection.data.put("Password", param);
			state = 3;
			processed = true;
			//connection.sendMessage("+OK maildrop locked and ready");
			//connection.sendMessage("-ERR invalid password");
		}
		if (state == 3 && connection.data.containsKey("User") && connection.data.containsKey("Password")) {
			// validate login
			User user = userPool.getUser(connection.data.get("User"));
			if (user == null || !user.password.equals(connection.data.get("Password"))) {
				connection.data.remove("User");
				connection.data.remove("Password");
				connection.sendMessage("-ERR invalid name or password");
			} else {
				if (user.connection == null) {
					if (command.equals("USER")) {
						connection.sendMessage("+OK maildrop opened");
					} else {
						connection.sendMessage("+OK maildrop opened");
					}
					connection.data.put("Login", "true");
					connection.user = user;
					user.connection = connection;
					synchronized(user) {
						user.maildrop.putMails(user.incoming);
						user.incoming = new Maildrop();
					}
					connection.maildrop = user.maildrop;
				} else {
					connection.data.remove("User");
					connection.data.remove("Password");
					connection.sendMessage("-ERR already connected to this maildrop - please try again later");
				}
			}
			//connection.data.remove("User");
			//connection.data.remove("Pass");
			//connection.sendMessage("-ERR invalid name or password");
		} else if (state == 3) {
			if (command.equals("USER")) {
				connection.sendMessage("+OK name set");
			} else {
				connection.sendMessage("+OK password set");
			}
		}
		
		//Is the user logged in (state 4) and is the command equals "Stat"
		if (state == 4 && command.equals("STAT")) {
			connection.sendMessage("+OK " + connection.maildrop.mails.size() + " " + connection.maildrop.getSize());
			processed = true;
		}
		
		//Is the user logged in (state 4) and is the command equals "List"
		if (state == 4 && command.equals("LIST")) {
			if (param.length() > 0) {
				int mail = Integer.parseInt(param);
				if (connection.maildrop.mails.size() > mail) {
					connection.sendMessage("+OK " + mail + " " + connection.maildrop.mails.get(mail).length());
				} else {
					connection.sendMessage("-ERR no such message, only " + connection.maildrop.mails.size() + " messages in maildrop");
				}
			} else {
				connection.sendMessage("+OK " + connection.maildrop.mails.size() + " messages (" + connection.maildrop.getSize() + " octets)");
				int id = 0;
				for (String mail : connection.maildrop.mails) {
					connection.sendMessage(id + " " + mail.length());
					id++;
				}
				connection.sendMessage(".");
			}
			processed = true;
		}
		
		//Is the user logged in (state 4) and is the command equals "Retr"
		if (state == 4 && command.equals("RETR")) {
			if (param.length() > 0) {
				int mail = Integer.parseInt(param);
				if (connection.maildrop.mails.size() > mail) {
					String msg = connection.maildrop.mails.get(mail);
					connection.sendMessage("+OK " + msg.length() + " octets");
					String[] msgs = msg.split("\n");
					for (String line : msgs)
						if (line.startsWith("."))
							connection.sendMessage("." + line);
						else
							connection.sendMessage(line);
					//connection.sendMessage(msg); // TODO REPLACE DOT WITH DOUBLEDOT
					connection.sendMessage(".");
				} else {
					connection.sendMessage("-ERR no such message, only " + connection.maildrop.mails.size() + " messages in maildrop");
				}
			} else {
				connection.sendMessage("-ERR no param given");
			}
			processed = true;
		}
		
		//Is the user logged in (state 4) and is the command equals "Dele"
		if (state == 4 && command.equals("DELE")) {
			if (param.length() > 0) {
				int mail = Integer.parseInt(param);
				if (connection.maildrop.mails.size() > mail) {
					if (!connection.maildrop.marked.contains(mail)) {
						connection.maildrop.marked.add(mail);
						connection.sendMessage("+OK message " + mail + " deleted");
					} else {
						connection.sendMessage("-ERR message " + mail + " already deleted");
					}
				} else {
					connection.sendMessage("-ERR no such message, only " + connection.maildrop.mails.size() + " messages in maildrop");
				}
			} else {
				connection.sendMessage("-ERR no param given");
			}
			processed = true;
		}
		
		//Is the user logged in (state 4) and is the command equals "Noop"
		if (state == 4 && command.equals("NOOP")) {
			connection.sendMessage("+OK");
			processed = true;
		}
		
		//Is the user logged in (state 4) and is the command equals "reset"
		if (state == 4 && command.equals("RSET")) {
			connection.maildrop.marked.clear();
			connection.sendMessage("+OK maildrop has " + connection.maildrop.mails.size() + " messages (" + connection.maildrop.getSize() + " octets)");
			processed = true;
		}
		
		//Is the user logged in (state 4) and is the command equals "uidl"
		if (state == 4 && command.equals("UIDL")) {
			if (param.length() > 0) {
				int mail = Integer.parseInt(param);
				if (connection.maildrop.mails.size() > mail) {
					connection.sendMessage("+OK " + mail + " " + connection.maildrop.uidls.get(mail));
				} else {
					connection.sendMessage("-ERR no such message, only " + connection.maildrop.mails.size() + " messages in maildrop");
				}
			} else {
				connection.sendMessage("+OK " + connection.maildrop.mails.size() + " messages (" + connection.maildrop.getSize() + " octets)");
				int id = 0;
				for (int uidl : connection.maildrop.uidls) {
					connection.sendMessage(id + " " + uidl);
					id++;
				}
				connection.sendMessage(".");
			}
			processed = true;
		}
		
		//Is the user logged in (state 4) and is the command equals "quit"
		if (state == 4 && command.equals("QUIT")) {
			connection.sendMessage("+OK terminating session");
			Maildrop recent = new Maildrop();
			Maildrop old = connection.maildrop;
			int id = 0;
			for (String mail : old.mails) {
				if (!old.marked.contains(id))
					recent.putMail(mail, old.uidls.get(id));
				id++;
			}
			connection.maildrop = recent;
			connection.user.maildrop = recent;
			connection.user.connection = null;
			connection.user = null;
			connection.quit();
		}
		
		//if the user isn't logged in and he don't use the "USER" or "PASS" command
		if (state == 0 && processed == false) {
			connection.sendMessage("-ERR command not accepted");
		}
		
		//if the user used a unkown command
		if (state == 4 && processed == false) {
			connection.sendMessage("-ERR unknown command");
		}
	}
}
