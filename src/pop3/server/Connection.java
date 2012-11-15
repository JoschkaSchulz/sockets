/**
 * Connection
 * 
 * Stellt eine Verbindung da.
 */
package pop3.server;

import java.net.Socket;
import java.util.HashMap;

public class Connection extends ClientWorker implements IConnection {
		private ConnectionPool connectionPool;
		public HashMap<String, String> data = new HashMap<String, String>();
		public User user;
		public Maildrop maildrop;
		
		public Connection(Socket socket, ConnectionPool connectionPool) {
			super(socket);
			this.connectionPool = connectionPool;
			connectionPool.newConnection(this);
			sendMessage("+OK log in now!");
		}
		protected void recvMessage(String rawPacket) {
			System.out.println("<< " + rawPacket);
			connectionPool.recv(this, rawPacket);
			// System.out.println("Incoming Packet : " + rawPacket);
		}

}
