package basic.server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * Klasse die die Chat Benutzter verwaltet
 */
public class HuebnerPlayerPool extends BasicPlayerPool {
	HashMap<IPlayer, String> players = new HashMap<IPlayer, String>();
	
	/*
	 * Standardkonstrucktor
	 * Führt den BasicPlayerPool Konstrucktor aus
	 */
	public HuebnerPlayerPool() {
		super();
	}
	
	/**
	 * Ohne Funktion wird einfach nur vom IPlayer Interface benötigt
	 */
	public void newPlayer(IPlayer player) {
	}
	
	/**
	 * Verwaltet die Befehle die vom Spieler kommen
	 */
	public void commit(IPlayer player, String message) {
	//Wenn die message länger 0 ist dann gebe die Nachricht aus
		if (message.length() > 0)
			System.out.println("S « " + player + ": " + message);
		String[] tokens = message.split(" ", 2);
	//Wenn die Nachricht mit 'NEW ' beginnt füge einen Spieler hinzu und sende ein OK zurück
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
	//Wenn die Nachricht mit 'INFO' beginnt dann schicke eine Liste der Benutzter zurück
		} else if (message.equals("INFO")) {
			StringBuilder sb = new StringBuilder("LIST ");
			synchronized(players) {
				sb.append(players.size());
				for (Entry<IPlayer, String> pair : players.entrySet()) {
			        sb.append(" ").append(pair.getKey().toString()).append(" ").append(pair.getValue());
				}
			}
			send(player, sb.toString());
	//Wenn die Nachricht mit 'BYE' beginnt beende die Server verbindung
		} else if (message.equals("BYE") || message.equals("SrvQuit")) {
			send(player, "BYE");
			player.quit();
			disconnecting(player);
		}
	}
	
	/**
	 * Sendet eine Nachricht zum Benutzter
	 * 
	 * @param player Der Spieler an dem gesendet werden soll
	 * @param message Die Nachricht die an den Spieler gesendet werden soll
	 */
	public void send(IPlayer player, String message) {
		if (player.isOnline()) {
			if (message.length() > 0)
				System.out.println("S » " + player + ": " + message);
			player.storMessage(message);
		}
	}
	
	/**
	 * Beendet die Verbindung von einem Benutzer
	 * 
	 * @param player Der Benutzter der disconnectet werden soll
	 */
	public void disconnecting(IPlayer player) {
		synchronized(players) {
			players.remove(player);
		}
	}
}