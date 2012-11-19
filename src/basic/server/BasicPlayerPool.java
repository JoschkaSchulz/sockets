package basic.server;

/**
 *BasicPlayerPool
 *
 *Verwaltet die Chat Benutzter
 */
public class BasicPlayerPool {
	public BasicPlayerPool() {
		super();
	}
	
	/**
	 * Fügt einen neuen Spieler hinzu(ohne Funktion)
	 * 
	 * @param player ohne Funktion
	 */
	public void newPlayer(IPlayer player) {
	}
	
	/**
	 * Gibt eine Nachricht und einen Spieler aus
	 * 
	 * @param player
	 * @param message
	 */
	public void commit(IPlayer player, String message) {
		if (message.length() > 0)
			System.out.println("S « " + player + ": " + message);
	}
	
	/**
	 * Sendet eine Nachricht an einen Spieler
	 * 
	 * @param player
	 * @param message
	 */
	public void send(IPlayer player, String message) {
		if (player.isOnline()) {
			if (message.length() > 0)
				System.out.println("S » " + player + ": " + message);
			player.storMessage(message);
		}
	}
	
	/**
	 * Ohne Funktion
	 * 
	 * @param player
	 */
	public void disconnect(IPlayer player) {
		
	}
}