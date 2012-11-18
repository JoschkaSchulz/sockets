/*
 * Chat für Rechnernetze
 * 
 * Dieses Chatprogramm bietet einen die Möglichkeit sich mit den Chat-Server zu verbinden.
 */

/*
 * Package angabe
 */
package basic.chat;

/*
 * Imports
 */
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

/*
 * HuebnerChat
 * 
 * Klasse zum erstellen eines Chatclienten mit GUI
 */
public class HuebnerChat implements Runnable {
	
	/*
	 * Variablen
	 */
	private Socket 			socket;		//socket für die Verbindung
	private BufferedReader 	in;			//Erhaltene Daten vom Server
	private PrintWriter 	out;		//Ausgehende Daten zum Server
	private BufferedReader 	sysin;		//Konsoleneingabe
	PrintWriter 			sysout;		//Konsolenausgabe
	
	private HuebnerGUI gui;				//GUI
	
	private Timer timer = new Timer();									//5 Sekunden Timer um Infos vom Server zu holen
	String myName;														//Eigener Name
	String ip;															//Server IP
	HashMap<String, String> players = new HashMap<String, String>();	//Alle Clienten an die gesendet werden soll

	/**
	 * Gibt die GUI zurück, damit es möglich ist auf die Eingaben zu zu greifen
	 * und um Text dort auszugeben.
	 * 
	 * @return Die GUI
	 */
	public HuebnerGUI getGUI() {	
		return this.gui;
	}
	
	/**
	 * Startet den Clienten
	 * 
	 * @param args Werden nicht verwendet
	 */
	public static void main(String args[]) {
		HuebnerGUI gui = new HuebnerGUI();
		gui.start(gui);
		new HuebnerChat(gui);
	}
	
	/**
	 * Holt aus einer Hashmap einen Key durch angabe des Value
	 * 
	 * @param map	Die Map welche durchsucht werden soll.
	 * @param value Das Value nachdem gesucht werden soll
	 * @return der erste key der gefunden wird, wird zurück gegeben, ansonsten null.
	 */
	public static <T, E> T getKeyByValue(HashMap<T, E> map, E value) {
	    for (Entry<T, E> entry : map.entrySet()) {
	        if (value.equals(entry.getValue())) {
	            return entry.getKey();
	        }
	    }
	    return null;
	}

	/**
	 * Eingabe des Namens des Benutzers
	 */
	public void enterName() {
		try {
			//Eingabe des Namens
			sysout.println("Name?");
			gui.appendText("Wie lautet ihr Name?\n", HuebnerGUI.SET_ORANGE);
			while(!gui.isOutputBufferfilled()) {
				Thread.sleep(50);
			}
			myName = gui.getOutputBuffer();
		}catch(Exception e) {}
	}
	
	/**
	 * Eingabe der IP des Servers
	 */
	public void enterIP() {
		try {
			//Eingabe der Server IP
			sysout.println("Server IP?");
			gui.appendText("Wie lautet die Server IP?\n", HuebnerGUI.SET_ORANGE);
			ip = "";
			while(!gui.isOutputBufferfilled()) {
				Thread.sleep(50);
			}
			ip = gui.getOutputBuffer();
		}catch(Exception e) {}
	}
	
	/**
	 * Diese Methode kontrolliert alles das was vom Server reinkommt und verwaltet dies,
	 * z.B. wird die Liste in einzelne Benutzter aufgeteilt und gespeichert.
	 */
	public void handleServerConnection() {
		try {
			while (true) {
				String message = in.readLine();
			//Wenn OK vom Server kommt, gebe aus das die Verbindung hergestellt wurde und sende ein "INFO"
				if (message.equals("OK")) {	
					sysout.println("* Verbindung hergestellt.");
					gui.appendText("* Verbindung hergestellt.\n", HuebnerGUI.SET_ORANGE);
					out.println("INFO");
			//Wenn der eingehende String mit LIST anfängt, zerteile die Liste und speichere sie
				} else if (message.startsWith("LIST ")) {	
					String[] split = message.split(" ");
					int i = 2;
					while (i < split.length) {
						players.put(split[i], split[i + 1]);
						i += 2;
					}
					gui.setPlayers(players);
			//Wenn ERROR kommt gebe aus, das der Server nicht ebreit ist und beende das Program
				} else if (message.startsWith("ERROR")) {	
					String[] split = message.split(" ", 2);
					sysout.println("* Fehler: " + ((split.length > 0) ? split[1] : "Der Server ist nicht bereit"));
					gui.appendText("* Fehler: " + ((split.length > 0) ? split[1]+"\n" : "Der Server ist nicht bereit\n"), HuebnerGUI.SET_ORANGE);
					System.exit(0);
			//Wenn ein BYE kommt gebe den Text aus der die Verbindung zum Server getrennt ist
				} else if (message.startsWith("BYE")) {	
					sysout.println("* Verbindung zum Server getrennt");
					gui.appendText("* Verbindung zum Server getrennt\n", HuebnerGUI.SET_ORANGE);
			//ansonsten gebe mit angeführten '***' aus, was für eine Nachricht vom Server kommt
				} else {	
					sysout.println("*** " + message);
					gui.appendText("*** " + message, HuebnerGUI.SET_ORANGE);
				}
			}
		}catch(Exception e) {}
	}
	
	/**
	 * Standardkonstrucktor
	 * 
	 * @param gui Die GUI muss mit übergeben werden muss.
	 */
	public HuebnerChat(HuebnerGUI gui) {
		this.gui = gui;
		this.gui.setHuebnerChat(this);
		
		try {
			sysin = new BufferedReader(new InputStreamReader(System.in));
			sysout = new PrintWriter(new OutputStreamWriter(System.out), true);

			this.enterName();	//Fragt den Namen ab
			
			this.enterIP();	//Fragt die Server IP ab
			
			//Verbindung zum Server herstellen
			socket = new Socket(ip, 50000);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
			
			//Sende den neuen Benutzer an den Server
			out.println("NEW " + myName);
			
			//Starte einen Timer/Thread der alle 5 Sekunden nach aktuellen Informationen fragt
			new Thread(this).start();
			new Thread(new HuebnerReceiver(this)).start();
			timer.schedule(new TimerTask() {
				public void run() {
					out.println("INFO");
				}
			}, 5000, 5000);
			
			this.handleServerConnection(); //Verwaltet die verbindung zum Server
			
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

	/**
	 * Führt das Runnable aus um die ganzen Benutzter Eingaben zu verwalten
	 */
	public void run() {
		while (true) {
			try {
			//Wartet auf eine Eingabe von der GUI
				String message = "";
				while(!gui.isOutputBufferfilled()) {
					Thread.sleep(50);
				}
				message = gui.getOutputBuffer();
				System.out.println("DEBUG:"+message);
				
			//Bei dem Befehl '/quit' sende BYE an den Server und beende die Verbindung und das Program
				if (message.startsWith("/quit")) {
					out.println("BYE");
					try {
						socket.close();
					} catch (Exception e) {
					}
					System.exit(0);
			//Bei dem Befehl '/msg <Name> <Nachricht>' wird die Nachricht an einem Bestimmten geschickt 
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
			//Bei dem Befehl '/nick <Name>' ist es möglich den Namen zu wechseln
				} else if (message.startsWith("/nick ")) {
					String[] split = message.split(" ", 2);
					myName = split[1].replace(' ', '_');
					out.println("NEW " + myName);
			//Bei dem Befehl '/names' werden alle verfügbaren Namen der Benutzter ausgegeben
				} else if (message.startsWith("/names")) {
					for (Entry<String, String> pair : players.entrySet()) {
				        sysout.println(pair.getValue().replace('_', ' '));
				        gui.appendText(pair.getValue().replace('_', ' ')+"\n", HuebnerGUI.SET_ORANGE);
					}
			//Bei dem Befehl '//' wird / gesendet
				} else if (message.startsWith("//")) {
					new Thread(new HuebnerSender(this, message.substring(1))).start();
					sysout.println(myName.replace('_', ' ') + ": " + message.substring(1));
					gui.appendText(myName.replace('_', ' ') + ": " + message.substring(1)+"\n", HuebnerGUI.SET_ORANGE);
			//Bei dem Befehl '/' wird ausgegben, dass der Befehl unbekannt ist (dies gilt für alle nicht vorher genannten Befehle)
				} else if (message.startsWith("/")) {
					sysout.println("* Unbekannter Befehl: " + message);
					gui.appendText("* Unbekannter Befehl: " + message+"\n", HuebnerGUI.SET_ORANGE);
			//Ansonsten wird einfach die Nachricht an alle im Chat gesendet
				} else {
					new Thread(new HuebnerSender(this, message+"\n")).start();
					sysout.println(myName.replace('_', ' ') + ": " + message);
				}
			} catch (Exception e) {

			}
		}
	}
	
}
