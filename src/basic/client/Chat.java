package basic.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import java.util.LinkedList;
import java.util.Random;

public class Chat implements Runnable {
	private static Random random = new Random();
	public class Ping {
		Date date = new Date();
		long id = Math.abs(random.nextLong());
	}

	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	private BufferedReader sysin;
	private PrintWriter sysout;
	
	private int myId;
	private String myName;
	private LinkedList<String> names = new LinkedList<String>();
	private LinkedList<Ping> pings = new LinkedList<Ping>();
	
	public static void main(String args[]) {
		new Chat();
	}

	public Chat() {
		try {
			sysin = new BufferedReader(new InputStreamReader(System.in));
			sysout = new PrintWriter(new OutputStreamWriter(System.out), true);

			sysout.println("Name?");
			myName = sysin.readLine().replace(' ', '_');
			
			sysout.println("Server IP?");

			socket = new Socket(sysin.readLine(), 4444);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
			
			new Thread(this).start();
			
			while (true) {
				String message = in.readLine();
				if (message.startsWith("PubMsg")) {
					String[] split = message.split(" ", 3);
					int sender = Integer.parseInt(split[1]);
					sysout.println(names.get(sender) + ": " + split[2]);
				} else if (message.startsWith("UniMsg")) {
					String[] split = message.split(" ", 3);
					int sender = Integer.parseInt(split[1]);
					System.err.println(names.get(sender) + ": " + split[2]);
				} else if (message.startsWith("SrvNewPlayer")) {
					int i = Integer.parseInt(message.split(" ", 2)[1]);
					names.add("Guest" + i);
					sysout.println("* " + names.get(i) + " ist gekommen");
				} else if (message.startsWith("SrvQuit")) {
					int i = Integer.parseInt(message.split(" ", 2)[1]);
					sysout.println("* " + names.get(i) + " ist gegangen");
				} else if (message.startsWith("PubName")) {
					String[] split = message.split(" ", 3);
					int i = Integer.parseInt(message.split(" ")[1]);
					String x = names.get(i); 
					names.set(i, split[2]);
					sysout.println("* " + x + " heiﬂt nun " + split[2]);
				} else if (message.startsWith("PubPing") || message.startsWith("UniPing")) {
					String[] split = message.split(" ", 2);
					out.println("UniPong " + split[1]);
				} else if (message.startsWith("UniPong")) {
					String[] split = message.split(" ", 3);
					long id = Long.parseLong(split[2]);
					Ping ping = null;
					for (Ping aPing : pings)
						if (aPing.id == id)
							ping = aPing;
					if (ping != null) {
						sysout.println("* Antwort von " + names.get(Integer.parseInt(split[1])) + " nach " + (new Date().getTime() - ping.date.getTime()) + " msek.");
						pings.remove(ping);
					}
				} else if (message.equals("SrvRebuildLog")) {
					out.println("PubName " + myName);
				} else if (message.startsWith("SrvWelcome")) {
					String[] split = message.split(" ", 2);
					names.add(myName);
					out.println("PubName " + myName);
					myId = Integer.parseInt(split[1]); 
				}
			}
		} catch (Exception e) {
			if (out != null) {
				System.out.println("-- connection reset --");
				e.printStackTrace();
			} else {
				System.out.println("-- connection failed --");
				e.printStackTrace();
			}
		}
	}

	public void run() {
		while (true) {
			try {
				String message = sysin.readLine();
				if (message.startsWith("/quit")) {
					try {
						socket.close();
					} catch (Exception e) {
					}
					System.exit(0);
				} else if (message.startsWith("/msg ")) {
					String[] split = message.split(" ", 3);
					if (names.contains(split[1]))
						out.println("UniMsg " + names.indexOf(split[1]) + " " + split[2]);
					else
						sysout.println("* " + split[1] + " gibt es nicht.");
				} else if (message.startsWith("/nick ")) {
					String[] split = message.split(" ", 2);
					myName = split[1].replace(' ', '_');
					out.println("PubName " + myName);
				} else if (message.startsWith("/names")) {
					for (String name : names)
						sysout.print(name + " ");
					sysout.println();
				} else if (message.startsWith("/ping")) {
					if (message.equals("/ping")) {
						for (int i = 0; i < names.size(); i++) {
							if (i == myId)
								continue;
							Ping ping = new Ping();
							pings.add(ping);
							out.println("UniPing " + i + " " + ping.id);
						}
					} else {
						String[] split = message.split(" ", 2);
						if (names.contains(split[1])) {
							Ping ping = new Ping();
							pings.add(ping);
							out.println("UniPing " + names.indexOf(split[1]) + " " + ping.id);
						}
					}
				} else if (message.startsWith("//")) {
					out.println("PubMsg " + message.substring(1));
				} else if (message.startsWith("/")) {
					sysout.println("* unbekannter Befehl: " + message);
				} else {
					out.println("PubMsg " + message);
					sysout.println(myName + ": " + message);
				}
			} catch (Exception e) {

			}
		}
	}
	
}
