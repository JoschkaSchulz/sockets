package basic.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class StillePost {
	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	private BufferedReader sysin;
	private PrintWriter sysout;
	
	public static void main(String args[]) {
		new StillePost();
	}

	public StillePost() {
		try {
			sysin = new BufferedReader(new InputStreamReader(System.in));
			sysout = new PrintWriter(new OutputStreamWriter(System.out), true);

			sysout.println("Das Spiel lautet Stille Post.");
			sysout.println("Wenn Sie am Zug sind, erhalten Sie eine unvollständige Nachricht.");
			sysout.println("Versuchen Sie, den vollständigen Inhalt zu erraten, der Ihrer Meinung nach Sinn ergibt,");
			sysout.println("und geben Sie diesen weiter.");
			sysout.println();

			sysout.println("Server IP?");

			socket = new Socket(sysin.readLine(), 4444);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
			
			String text = null; // part of the game state!!
			while (true) {
				String message = in.readLine();
				if (message.equals("SrvYourTurn")) {
					sysout.println();
					sysout.println("Sie sind am Zug!");
					sysout.println(obfuscate(text));
					text = sysin.readLine();
					out.println("GamText " + text);
					out.println("SrvNextTurn");
					sysout.println();
					sysout.println();
				} else if (message.startsWith("GamText")) {
					text = message.split(" ", 2)[1];
				} else if (message.startsWith("SrvNextTurn")) {
					sysout.println("Spieler " + message.split(" ", 2)[1] + " ist am Zug.");
				} else if (message.startsWith("SrvNewPlayer")) {
					sysout.println("Neuer Spieler: " + message.split(" ", 2)[1]);
				} else if (message.startsWith("SrvWelcome")) {
					sysout.println("Ihre Spielernummer: " + message.split(" ", 2)[1]);
				}
			}
		} catch (Exception e) {
			if (out != null)
				System.out.println("-- connection reset --");
			else {
				System.out.println("-- connection failed --");
				e.printStackTrace();
			}
		}
	}

	private String obfuscate(String message) {
		if (message == null) {
			return "Denken Sie sich eine Nachricht aus:";
		}
		char[] c = message.toCharArray();
		for (int i = 0; i < c.length - 1; i++) {
			if (Math.random() >= 0.5d) {
				c[i] = ' ';
			}
		}
		return new String(c);
	}
	
}
