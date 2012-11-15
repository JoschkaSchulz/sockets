package basic.server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class HuebnerPlayerPool extends BasicPlayerPool {
	HashMap<IPlayer, String> players = new HashMap<IPlayer, String>();
	public HuebnerPlayerPool() {
		super();
	}
	public void newPlayer(IPlayer player) {
	}
	public void commit(IPlayer player, String message) {
		if (message.length() > 0)
			System.out.println("S « " + player + ": " + message);
		String[] tokens = message.split(" ", 2);
		if (message.startsWith("NEW ")) {
			if (tokens[1].contains(" ") || tokens[1].length() < 1 || tokens[1].length() > 20) {
				send(player, "ERROR Your name can not be accepted.");
				player.quit();
				return;
			}
			send(player, "OK");
			synchronized(players) {
				players.put(player, tokens[1]);
			}
		} else if (message.equals("INFO")) {
			StringBuilder sb = new StringBuilder("LIST ");
			synchronized(players) {
				sb.append(players.size());
				for (Entry<IPlayer, String> pair : players.entrySet()) {
			        sb.append(" ").append(pair.getKey().toString()).append(" ").append(pair.getValue());
				}
			}
			send(player, sb.toString());
		} else if (message.equals("BYE") || message.equals("SrvQuit")) {
			send(player, "BYE");
			player.quit();
			disconnecting(player);
		}
	}
	public void send(IPlayer player, String message) {
		if (player.isOnline()) {
			if (message.length() > 0)
				System.out.println("S » " + player + ": " + message);
			player.storMessage(message);
		}
	}
	public void disconnecting(IPlayer player) {
		synchronized(players) {
			players.remove(player);
		}
	}
}