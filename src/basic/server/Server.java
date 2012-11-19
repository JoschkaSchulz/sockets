package basic.server;
import java.io.*;
import java.net.*;

/**
 * Der Server f�r den Chat
 */
public class Server extends Thread {
	/*
	 * Variablen
	 */
	private ServerSocket serverSocket;
	private Socket socket;
	private BasicPlayerPool playerPool;

	/**
	 * Die main startet den Server f�r die Chat clienten
	 * 
	 * @param args Ohne Funktion
	 */
	public static void main(String args[]) {
		int port = 50000;
		Server app = new Server(port);
		app.start();

		System.out.println("Listening on port " + port + " ...");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		PrintWriter out = new PrintWriter(new OutputStreamWriter(System.out), true);
		try {
			while (true) {
				if (in.readLine().equals("quit")) {
					app.close();
					break;
				}
			}
		} catch (IOException e) {
			out.println("-- eof received --");
		} finally {
			out.println("-- terminated --");
		}
		System.exit(0);
	}

	/**
	 * Erstellt einen neuen Server
	 * 
	 * @param port der Port auf dem der Server gestartet werden soll.
	 */
	public Server(int port) {
		playerPool = new HuebnerPlayerPool();
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Schlie�t eine Verbdinung
	 */
	public void close() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Startet das Lauschen auf neue Anfragen von Clienten.
	 */
	public void run() {
		try {
			while (true) {
				socket = serverSocket.accept();
				System.out.println("Incoming Connection from " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
				new Player(socket, playerPool);
			}
		} catch (IOException e) {
			// java.net.SocketException: socket closed
			// e.printStackTrace();
		}
	}
}
