package basic.server;
import java.net.Socket;

/**
 * Player ist ein Benutzter im Chat
 */
public class Player extends ClientWorker implements IPlayer {
	private BasicPlayerPool playerPool;
	
	/**
	 * 
	 * @param socket
	 * @param playerPool
	 */
	public Player(Socket socket, BasicPlayerPool playerPool) {
		super(socket);
		this.playerPool = playerPool;
		playerPool.newPlayer(this);
	}
	
	/**
	 * Empfängt eine Nachricht und gibt sie an den playerPool weiter
	 */
	protected void recvMessage(String rawPacket) {
		playerPool.commit(this, rawPacket);
		// System.out.println("Incoming Packet : " + rawPacket);
	}
	
	/**
	 * Gibt den HostName des Spielers aus
	 */
	public String toString() {
		return socket.getInetAddress().getHostName();
	}
}
