package pop3.server;

import java.io.*;
import java.net.*;

public class Server extends Thread {
	private ServerSocket serverSocket;
	private Socket socket;
	private ConnectionPool connectionPool;
	private UserPool userPool;
	private ServerWorker serverWorker;

	public static void main(String args[]) {
		int port = 11000;
		Server server = new Server(port);
		server.serverWorker.userPool = server.userPool;
		server.serverWorker.start();
		server.start();

		System.out.println("Listening on port " + port + " ...");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		PrintWriter out = new PrintWriter(new OutputStreamWriter(System.out), true);
		try {
			while (true) {
				if (in.readLine().equals("quit")) {
					server.close();
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

	public Server(int port) {
		connectionPool = new ConnectionPool();
		userPool = new UserPool();
		connectionPool.userPool = userPool;
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		serverWorker = new ServerWorker("ServerWorker");
	}

	public void close() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			while (true) {
				socket = serverSocket.accept();
				System.out.println("Incoming Connection from " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
				new Connection(socket, connectionPool);
			}
		} catch (IOException e) {
			// java.net.SocketException: socket closed
			// e.printStackTrace();
		}
	}
}
