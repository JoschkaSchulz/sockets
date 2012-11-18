package basic.chat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class HuebnerChat implements Runnable {
	
	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	private BufferedReader sysin;
	PrintWriter sysout;
	
	private HuebnerGUI gui;
	
	private Timer timer = new Timer();;
	String myName;
	HashMap<String, String> players = new HashMap<String, String>();

	public HuebnerGUI getGUI() {
		return this.gui;
	}
	
	public static void main(String args[]) {
		HuebnerGUI gui = new HuebnerGUI();
		gui.start(gui);
		new HuebnerChat(gui);
	}
	
	public static <T, E> T getKeyByValue(HashMap<T, E> map, E value) {
	    for (Entry<T, E> entry : map.entrySet()) {
	        if (value.equals(entry.getValue())) {
	            return entry.getKey();
	        }
	    }
	    return null;
	}

	public HuebnerChat(HuebnerGUI gui) {
		this.gui = gui;
		this.gui.setHuebnerChat(this);
		
		try {
			sysin = new BufferedReader(new InputStreamReader(System.in));
			sysout = new PrintWriter(new OutputStreamWriter(System.out), true);

			//Eingabe des Namens
			sysout.println("Name?");
			gui.appendText("Wie lautet ihr Name?\n", HuebnerGUI.SET_ORANGE);
			while(!gui.isOutputBufferfilled()) {
				Thread.sleep(50);
			}
			myName = gui.getOutputBuffer();
			
			
			//Eingabe der Server IP
			sysout.println("Server IP?");
			gui.appendText("Wie lautet die Server IP?\n", HuebnerGUI.SET_ORANGE);
			String ip = "";
			while(!gui.isOutputBufferfilled()) {
				Thread.sleep(50);
			}
			ip = gui.getOutputBuffer();
			
			//Verbindung zum Server herstellen
			socket = new Socket(ip, 50000);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
			
			out.println("NEW " + myName);
			
			new Thread(this).start();
			new Thread(new HuebnerReceiver(this)).start();
			timer.schedule(new TimerTask() {
				public void run() {
					out.println("INFO");
				}
			}, 5000, 5000);
			while (true) {
				String message = in.readLine();
				if (message.equals("OK")) {
					sysout.println("* Verbindung hergestellt.");
					gui.appendText("* Verbindung hergestellt.\n", HuebnerGUI.SET_ORANGE);
					out.println("INFO");
				} else if (message.startsWith("LIST ")) {
					String[] split = message.split(" ");
					int i = 2;
					while (i < split.length) {
						players.put(split[i], split[i + 1]);
						i += 2;
					}
					gui.setPlayers(players);
				} else if (message.startsWith("ERROR")) {
					String[] split = message.split(" ", 2);
					sysout.println("* Fehler: " + ((split.length > 0) ? split[1] : "Der Server ist nicht bereit"));
					gui.appendText("* Fehler: " + ((split.length > 0) ? split[1]+"\n" : "Der Server ist nicht bereit\n"), HuebnerGUI.SET_ORANGE);
					System.exit(0);
				} else if (message.startsWith("BYE")) {
					sysout.println("* Verbindung zum Server getrennt");
					gui.appendText("* Verbindung zum Server getrennt\n", HuebnerGUI.SET_ORANGE);
				} else {
					sysout.println("*** " + message);
					gui.appendText("*** " + message, HuebnerGUI.SET_ORANGE);
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
		System.exit(0);
	}

	public void run() {
		while (true) {
			try {
				//String message = sysin.readLine();	//Konsolen Version
				String message = "";
				while(!gui.isOutputBufferfilled()) {
					Thread.sleep(50);
				}
				message = gui.getOutputBuffer();
				System.out.println("DEBUG:"+message);
				
				if (message.startsWith("/quit")) {
					out.println("BYE");
					try {
						socket.close();
					} catch (Exception e) {
					}
					System.exit(0);
				} else if (message.startsWith("/msg ")) {
					String[] split = message.split(" ", 3);
					if (players.containsValue(split[1])) {
						new Thread(new HuebnerSender(this, split[2], split[1])).start();
						sysout.println(myName.replace('_', ' ') + ": " + split[2]);
						gui.appendText(myName.replace('_', ' ') + ": \n" + split[2]+"\n", HuebnerGUI.SET_ORANGE);
					} else {
						sysout.println("* " + split[1] + " gibt es nicht.");
						gui.appendText("* " + split[1] + " gibt es nicht.\n", HuebnerGUI.SET_ORANGE);
					}
				} else if (message.startsWith("/nick ")) {
					String[] split = message.split(" ", 2);
					myName = split[1].replace(' ', '_');
					out.println("NEW " + myName);
				} else if (message.startsWith("/names")) {
					for (Entry<String, String> pair : players.entrySet()) {
				        sysout.println(pair.getValue().replace('_', ' '));
				        gui.appendText(pair.getValue().replace('_', ' ')+"\n", HuebnerGUI.SET_ORANGE);
					}
				} else if (message.startsWith("//")) {
					new Thread(new HuebnerSender(this, message.substring(1))).start();
					sysout.println(myName.replace('_', ' ') + ": " + message.substring(1));
					gui.appendText(myName.replace('_', ' ') + ": " + message.substring(1)+"\n", HuebnerGUI.SET_ORANGE);
				} else if (message.startsWith("/")) {
					sysout.println("* Unbekannter Befehl: " + message);
					gui.appendText("* Unbekannter Befehl: " + message+"\n", HuebnerGUI.SET_ORANGE);
				} else {
					new Thread(new HuebnerSender(this, message+"\n")).start();
					sysout.println(myName.replace('_', ' ') + ": " + message);
				}
			} catch (Exception e) {

			}
		}
	}
	
}
