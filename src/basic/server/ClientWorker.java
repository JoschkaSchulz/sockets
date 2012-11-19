package basic.server;
import java.io.*;
import java.net.*;

/**
 * 
 */
public class ClientWorker implements Runnable {
	protected Socket socket;
	protected BufferedReader in;
	protected PrintWriter out;
	protected Thread runner;

	/**
	 * 
	 * @param socket
	 */
	public ClientWorker(Socket socket) {
		this.socket = socket;

		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

			runner = new Thread(this);
			runner.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Empf‰ngt Packete vom Socket
	 */
	public void run() {
		try {
			while (true) {
				String rawPacket = in.readLine();

				if (rawPacket != null) {
					recvMessage(rawPacket);
				} else {
					System.out.println("Connection reset from " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
					socket.close();
					socket = null;
					runner = null;
					recvMessage("SrvQuit");
					break;
				}
			}
		} catch (IOException e) {
			//java.net.SocketException: socket closed
			//e.printStackTrace();
		}
	}

	/**
	 * Schaut ob der Socket aktiv ist
	 * 
	 * @return true wenn der socket gesetrzt ist, sonst false
	 */
	public boolean isOnline() {
		return socket != null;
	}
	
	/**
	 * Schlieﬂt den socket sauber
	 */
	public void quit() {
		try {
			if (socket != null) {
				System.out.println("Connection closed to " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
				if (!socket.isClosed())
					socket.close();
			}
			socket = null;
			runner = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Gibt eine empfangene Nachricht aus
	 * 
	 * @param rawPacket Das erhaltene Packet
	 */
	protected void recvMessage(String rawPacket) {
		System.out.println("Incoming Packet : " + rawPacket);
		out.println("Incoming Packet : " + rawPacket);
	}
	
	/**
	 * Beendet die Verbindung falls der socket geschlossen wird
	 * 
	 * @param message 
	 */
	public void storMessage(String message) {
		if (socket != null) {
			out.println(message);
			if (socket.isClosed() || out.checkError()) {
				quit();
				recvMessage("SrvQuit");
			}
		}
	}
}
