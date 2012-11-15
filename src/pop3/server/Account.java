/**
 * Account
 * 
 * Diese Klasse dient lediglich zum Speichern aller 
 * Daten eines Accounts
 */
package pop3.server;

public class Account {
	public String hostname;
	public int port;
	public String username;
	public String password;
	public Account(String hostname, int port, String username, String password) {
		this.hostname = hostname;
		this.port = port;
		this.username = username;
		this.password = password;
	}
}