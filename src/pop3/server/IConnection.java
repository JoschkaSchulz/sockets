/**
 * IConnecton
 * 
 * Dieses Interface wird ben�tigt um eine Verbindung zu erstellen
 * welche mit unserern Server kommunizieren kann.
 */

package pop3.server;

public interface IConnection {

	void sendMessage(String message);

	boolean isOnline();

	void quit();

}