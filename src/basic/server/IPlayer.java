package basic.server;

/**
 *Interface für einen Benutzter
 */
public interface IPlayer {

	/**
	 * 
	 * @param message
	 */
	void storMessage(String message);

	/**
	 * Schaut ob der Benutzter noch Online ist
	 * 
	 * @return true wenn der Socket Online ist, sonst false
	 */
	boolean isOnline();

	/**
	 * Beendet die Verbindung sauber
	 */
	void quit();

}