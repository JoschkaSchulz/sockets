package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.LinkedList;

public class ServerConnection {
	public Socket socket;
	public BufferedReader in;
	public PrintWriter out;
	public Thread reader;
	public Thread writer;
	public Thread waker;

	public String hostname = "localhost";
	public int port = 50000;

	public int playerId = -1;
	public int gameId = -1;

	public LinkedList<String> inList = new LinkedList<String>();
	public LinkedList<String> outList = new LinkedList<String>();

	public long linesSent = 0;
	public long linesRecv = 0;
	public long charsSent = 0;
	public long charsRecv = 0;

	public boolean isOnline = false;
	
	public LinkedList<ILogger> loggers = new LinkedList<ILogger>();

	public ServerConnection() {}

	public void init() {
		try {
			socket = new Socket(hostname, port);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
			out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")), true);
			reader = new Thread("ServerConnectionReader") {
				@Override
				public void run() {
					read();
				}
			};
			writer = new Thread("ServerConnectionWriter") {
				@Override
				public void run() {
					write();
				}
			};
			waker = new Thread("WakeUpThread") {
				@Override
				public void run() {
				}
			};
			reader.start();
			writer.start();
			isOnline = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void disposeAll() {
		isOnline = false;
		if (reader != null && reader.isAlive()) {
			reader.interrupt();
			reader = null;
		}
		if (writer != null && writer.isAlive()) {
			writer.interrupt();
			writer = null;
		}
		if (in != null) {
			try {
				in.close();
			} catch (Exception e) {}
			socket = null;
		}
		if (out != null) {
			try {
				out.close();
			} catch (Exception e) {}
			out = null;
		}
		if (socket != null && !socket.isClosed()) {
			try {
				socket.close();
			} catch (Exception e) {}
			in = null;
		}
	}

	public int[] getConnectionInfo() {
		return new int[] { gameId, playerId };
	}

	public void setConnectionInfo(int[] info) {
		push("SrvSetGame " + gameId);
		push("SrvSetPlayer " + playerId);
	}

	public String pull() {
		synchronized (inList) {
			if (inList.size() > 0)
				return inList.removeFirst();
		}
		return null;
	}

	public void push(String message) {
		charsSent += message.length() + 1;
		linesSent++;
		synchronized (outList) {
			outList.add(message);
			outList.notifyAll();
		}
		if (message.length() == 0) return;
		for (ILogger l : loggers)
			l.addLine(false, message);
		System.err.println(message);
	}
	
	public void ping() {
		push("");
	}
	
	public void listen() {
		synchronized (inList) {
			while (true) {
				if (inList.size() > 0)
					return;
				try {
					inList.wait();
				} catch (Exception e) {
					return;
				}
			}
		}
	}

	public String toString() {
		return ((isOnline) ? "Online" : "Offline") + " to " + hostname + ":" + port + " \n"
				+ "Sent: " + linesSent + " (" + charsSent + " chars) \n"
				+ "Recv: " + linesRecv + " (" + charsRecv + " chars)";
	}

	private void read() {
		try {
			while (true) {
				String message = in.readLine();
				charsRecv += message.length() + 1;
				linesRecv++;
				if (message.length() == 0) continue;
				synchronized (inList) {
					inList.add(message);
					inList.notifyAll();
				}
				for (ILogger l : loggers)
					l.addLine(true, message);
				System.err.println(message);
				if (Thread.interrupted())
					return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		isOnline = false;
	}

	private void write() {
		try {
			synchronized (outList) {
				while (true) {
					if (outList.size() == 0)
						outList.wait();
					while (outList.size() > 0)
						out.println(outList.removeFirst());
					if (Thread.interrupted())
						return;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		isOnline = false;
	}
}
