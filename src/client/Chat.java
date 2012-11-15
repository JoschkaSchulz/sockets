package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.LinkedList;
import java.util.Random;

public class Chat extends BasicGame {
	private static Random random = new Random();
	public class Ping {
		Date date = new Date();
		long id = Math.abs(random.nextLong());
	}

	private BufferedReader sysin;
	private PrintWriter sysout;
	
	private String myName = "";
	
	private LinkedList<Ping> pings = new LinkedList<Ping>();
	
	public static void main(String args[]) {
		new Chat();
	}

	public Chat() {
		try {
			sysin = new BufferedReader(new InputStreamReader(System.in));
			sysout = new PrintWriter(new OutputStreamWriter(System.out), true);

			sysout.println("Name?");
			myName = sysin.readLine().replace(' ', '_');
			
			sysout.println("Server IP?");

			filter = new BasicConnectionFilter(new ServerConnection());
			filter.serverConnection.hostname = sysin.readLine();
			filter.serverConnection.init();
			
			init();
			run();
		} catch (Exception e) {
			
		}
	}
	
	public void onAny() {
		if (filter.action.equals("Msg")) {
			if (filter.type == "Uni")
				System.err.println(players.get(filter.sender).name + ": " + filter.parameter);
			else
				sysout.println(players.get(filter.sender).name + ": " + filter.parameter);
		} else if (filter.action.equals("Ping")) {
			filter.push("UniPong " + filter.sender + " " + filter.parameter);
		} else if (filter.action.equals("Pong")) {
			long id = Long.parseLong(filter.arguments[0]);
			Ping ping = null;
			for (Ping aPing : pings)
				if (aPing.id == id)
					ping = aPing;
			if (ping != null) {
				sysout.println("* Antwort von " + players.get(filter.sender).name + " nach " + (new Date().getTime() - ping.date.getTime()) + " msek.");
				pings.remove(ping);
			}
		}
	}

	public void onSrvNewPlayer(int id) {
		sysout.println("* " + players.get(id).name + " ist gekommen");
	}
	
	public void onSrvQuit(int id) {
		sysout.println("* " + players.get(id).name + " ist gegangen");
	}
	
	public void onAnyName(int id, String name, String old) {
		sysout.println("* " + old + " heiﬂt nun " + name);
	}

	public void onSrvWelcome(int id) {
		setAnyName(myName);
	}

	public void run() {
		while (true) {
			try {
				String message = sysin.readLine();
				if (message.startsWith("/quit")) {
					setSrvQuit();
					filter.serverConnection.disposeAll();
					System.exit(0);
				} else if (message.startsWith("/msg ")) {
					String[] split = message.split(" ", 3);
					boolean success = false;
					for (BasicPlayer player : players)
						if (player.name == split[1]) {
							filter.push("UniMsg " + player.id + " " + split[2]);
							success = true;
						}
					if (!success)
						sysout.println("* " + split[1] + " gibt es nicht.");
				} else if (message.startsWith("/nick ")) {
					String[] split = message.split(" ", 2);
					myName = split[1].replace(' ', '_');
					setAnyName(myName);
				} else if (message.startsWith("/names")) {
					for (BasicPlayer player : players)
						sysout.print(player.name + " ");
					sysout.println();
				} else if (message.startsWith("/ping")) {
					if (message.equals("/ping")) {
						for (BasicPlayer player : players) {
							if (player.id == playerId)
								continue;
							Ping ping = new Ping();
							pings.add(ping);
							filter.push("UniPing " + player.id + " " + ping.id);
						}
					} else {
						String[] split = message.split(" ", 2);
						for (BasicPlayer player : players) {
							if (player.name == split[1]) {
								Ping ping = new Ping();
								pings.add(ping);
								filter.push("UniPing " + player.id + " " + ping.id);
							}
						}
					}
				} else if (message.startsWith("//")) {
					filter.push("PubMsg " + message.substring(1));
				} else if (message.startsWith("/")) {
					sysout.println("* unbekannter Befehl: " + message);
				} else {
					filter.push("PubMsg " + message);
					sysout.println(myName + ": " + message);
				}
			} catch (Exception e) {

			}
		}
	}
	
}
