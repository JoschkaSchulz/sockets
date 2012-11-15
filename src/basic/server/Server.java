package basic.server;
import java.io.*;
import java.net.*;

public class Server extends Thread {
	private ServerSocket serverSocket;
	private Socket socket;
	private BasicPlayerPool playerPool;

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

	public Server(int port) {
		playerPool = new HuebnerPlayerPool();
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
				new Player(socket, playerPool);
			}
		} catch (IOException e) {
			// java.net.SocketException: socket closed
			// e.printStackTrace();
		}
	}
}
