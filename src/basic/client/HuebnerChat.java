package basic.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import basic.server.IPlayer;

public class HuebnerChat implements Runnable {
	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	private BufferedReader sysin;
	private PrintWriter sysout;
	
	private String myName;
	HashMap<String, String> players = new HashMap<String, String>();

	private class HuebnerSender implements Runnable {
		public void run() {
			// TODO Auto-generated method stub
			
		}
	}
	
	public static void main(String args[]) {
		new HuebnerChat();
	}
	
	public static <T, E> T getKeyByValue(HashMap<T, E> map, E value) {
	    for (Entry<T, E> entry : map.entrySet()) {
	        if (value.equals(entry.getValue())) {
	            return entry.getKey();
	        }
	    }
	    return null;
	}

	public HuebnerChat() {
		try {
			sysin = new BufferedReader(new InputStreamReader(System.in));
			sysout = new PrintWriter(new OutputStreamWriter(System.out), true);

			sysout.println("Name?");
			myName = sysin.readLine().replace(' ', '_');
			
			sysout.println("Server IP?");

			socket = new Socket(sysin.readLine(), 4444);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
			
			out.println("NEW " + myName);
			
			new Thread(this).start();
			
			while (true) {
				String message = in.readLine();
				if (message.equals("OK")) {
					sysout.println("* Verbindung hergestellt.");
					out.println("INFO");
				} else if (message.startsWith("LIST ")) {
					String[] split = message.split(" ");
					int i = 2;
					while (i < split.length) {
						players.put(split[i], split[i + 1]);
						i += 2;
					}
				} else if (message.startsWith("ERROR")) {
					String[] split = message.split(" ", 2);
					sysout.println("* Fehler: " + ((split.length > 0) ? split[1] : "Der Server ist nicht bereit"));
					System.exit(0);
				} else if (message.startsWith("BYE")) {
					sysout.println("* Verbindung zum Server getrennt");
				} else {
					sysout.println("*** " + message);
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
					out.println("BYE");
					try {
						socket.close();
					} catch (Exception e) {
					}
					System.exit(0);
				} else if (message.startsWith("/msg ")) {
					String[] split = message.split(" ", 3);
					split[1] = split[1].replace(' ', '_');
					if (players.containsValue(split[1]))
						out.println("UniMsg " + getKeyByValue(players, split[1]) + " " + split[2]);
					else
						sysout.println("* " + split[1] + " gibt es nicht.");
				} else if (message.startsWith("/nick ")) {
					String[] split = message.split(" ", 2);
					myName = split[1].replace(' ', '_');
					out.println("NEW " + myName);
				} else if (message.startsWith("/names")) {
					for (Entry<String, String> pair : players.entrySet()) {
				        sysout.println(pair.getValue());
					}
					sysout.println();
				} else if (message.startsWith("//")) {
					out.println(message.substring(1));
				} else if (message.startsWith("/")) {
					sysout.println("* Unbekannter Befehl: " + message);
				} else {
					out.println(message);
					sysout.println(myName + ": " + message);
				}
			} catch (Exception e) {

			}
		}
	}
	
}
