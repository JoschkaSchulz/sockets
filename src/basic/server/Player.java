package basic.server;
import java.net.Socket;


public class Player extends ClientWorker implements IPlayer {
	private BasicPlayerPool playerPool;
	
	public Player(Socket socket, BasicPlayerPool playerPool) {
		super(socket);
		this.playerPool = playerPool;
		playerPool.newPlayer(this);
	}
	protected void recvMessage(String rawPacket) {
		playerPool.commit(this, rawPacket);
		// System.out.println("Incoming Packet : " + rawPacket);
	}
	public String toString() {
		return socket.getInetAddress().getHostName();
	}
}
