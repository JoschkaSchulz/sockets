package basic.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;

public class Backgammon {
	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	private BufferedReader sysin;
	private PrintWriter sysout;
	
	private LinkedList<String> namen = new LinkedList<String>();
	private String meinName;
	/**
	 *     0 = store black
	 * 1 - 6 = home black
	 * 7 -12 = outer
	 * 13-18 = outer
	 * 19-24 = home white
	 *    25 = store white
	 *    26 = out black
	 *    27 = out white
	 *    
	 *    positive = black
	 *    
	 *    start:
	 *     1 =-2
	 *     6 = 6
	 *     8 = 3
	 *    12 =-5
	 *    13 = 5
	 *    17 =-3
	 *    19 =-5
	 *    24 = 2
	 */
	private int[] felder = new int[28];
	private char[] alphabet = new char[26];

	int player = -1;
	int onTurn = 0;
	int[] dice = null;

	public static void main(String args[]) {
		new Backgammon();
	}

	public Backgammon() {
		try {
			init();
			sysin = new BufferedReader(new InputStreamReader(System.in));
			sysout = new PrintWriter(new OutputStreamWriter(System.out), true);

			sysout.println("Das Spiel lautet Backgammon.");
			sysout.println();

			sysout.println("Ihr Name?");
			
			meinName = sysin.readLine();

			sysout.println("Server IP?");

			socket = new Socket(sysin.readLine(), 4444);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

			boolean isLocked = false;

			while (true) {
				String message = in.readLine();
				if (message.equals("SrvYourTurn")) {
					if (getWinner()) {
						sysout.println("Spiel vorbei!");
						out.println("GamOver");
						sysout.println("Drücken Se die Eingabetaste, um eine neue Runde zu beginnen");
						sysin.readLine();
						out.println("GamReset");
						out.println("SrvResetLog");
						init();
					}
					if (!isLocked) {
						// out.println("SrvMinPlayers 2");
						out.println("SrvMaxPlayers 2");
						out.println("SrvLock");
						isLocked = true;
					}
					dice = rollDice();
					out.println("GamRoll " + dice[0] + " " + dice[1]);
					int[] moves = new int[4];
					moves[0] = dice[0];
					moves[1] = dice[1];
					if (moves[0] == moves[1]) {
						moves[2] = moves[0];
						moves[3] = moves[1];
					}
					do {
						int pos1 = -1;
						int pos2 = -1;
						int span = 0;
						boolean farbe = player == 1;
						String text;
						drawBoard(dice);
						if (felder[26 + (farbe ? 1 : 0)] != 0) {
							pos1 = 26 + player;
						}
						if (pos1 < 26)
							sysout.println("Benennen Sie zwei Felder für einen Bewegungszug!");
						else
							sysout.println("Benennen Sie ein Feld zum Platzieren des Objekts!");
						do {
							text = sysin.readLine().toUpperCase();
							if (pos1 < 26) {
								pos1 = getPosition(Character.toString(text.charAt(0)));
								pos2 = getPosition(Character.toString(text.charAt(1)));
							} else {
								pos2 = getPosition(Character.toString(text.charAt(0)));
							}
							if (pos1 < 26) {
								span = pos2 - pos1;
								if (farbe)
									span = -span;
							} else {
								span = pos2;
								if (farbe)
									span = 25 - span;
								if (span == 0)
									continue;
							}
						} while (pos1 == -1 || pos2 == -1 || !testMove(moves, span) || !validSource(farbe, pos1) || !validTarget(farbe, pos2));
						moves = useMove(moves, span);
						take(farbe, pos1);
						place(farbe, pos2);
						out.println("GamMove " + pos1 + " " + pos2);
					} while (moves[0] > 0 || moves[1] > 0 || moves[2] > 0 || moves[3] > 0);
					out.println("SrvNextTurn");
				} else if (message.startsWith("GamRoll")) {
					String[] split = message.split(" ", 3);
					int[] dice = new int[split.length - 1];
					for (int i = 1; i < split.length; i++) {
						dice[i - 1] = Integer.parseInt(split[i]);
					}
					drawBoard(dice);
					sysout.println(namen.get(onTurn) + " hat gewürfelt.");
				} else if (message.startsWith("GamMove")) {
					int pos1 = Integer.parseInt(message.split(" ")[1]);
					int pos2 = Integer.parseInt(message.split(" ")[2]);
					boolean farbe = onTurn == 1;
					take(farbe, pos1);
					place(farbe, pos2);
					/*int wert = Integer.parseInt(message.split(" ")[3]);
					int zug = Integer.parseInt(message.split(" ")[1]);*/
					/*spieler.get(onTurn)[feld] = wert;
					maxMoves = Math.max(maxMoves, zug);*/
					//sysout.println(namen.get(onTurn) + " trägt " + wert + " Punkte in " + felder[feld].getName());
				} else if (message.equals("GamOver")) {
					sysout.println("Spiel vorbei!");
				} else if (message.equals("GamReset")) {
					init();
				} else if (message.startsWith("UniName") || message.startsWith("PubName")) {
					String[] split = message.split(" ", 3);
					int i = Integer.parseInt(message.split(" ")[1]);
					String x = namen.get(i); 
					namen.set(i, split[2]);
					sysout.println(x + " heißt jetzt " + split[2]);
				} else if (message.startsWith("SrvNextTurn")) {
					onTurn = Integer.parseInt(message.split(" ", 2)[1]);
					if (onTurn == player)
						sysout.println("Sie sind am Zug!");
					else
						sysout.println(((namen.size() > onTurn) ? namen.get(onTurn) : "Spieler " + onTurn) + " ist am Zug.");
				} else if (message.startsWith("SrvNewPlayer")) {
					int i = Integer.parseInt(message.split(" ", 2)[1]);
					namen.add("Spieler " + i);
					sysout.println("Neuer Spieler: " + namen.get(i));
				} else if (message.equals("SrvRebuildLog")) {
					out.println("PubName " + meinName);
				} else if (message.startsWith("SrvLock")) {
					isLocked = true;
				} else if (message.startsWith("SrvWelcome")) {
					player = Integer.parseInt(message.split(" ", 2)[1]);
					namen.add(meinName);
					out.println("PubName " + meinName);
					sysout.println("Ihre Position: " + player);
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
	
	private void init() {
		for (int i = 0; i < 26; i++) {
			alphabet[i] = (char) (65 + i);
		}
		felder[1] = -2;
		felder[6] = 6;
		felder[8] = 3;
		felder[12] = -5;
		felder[13] = 5;
		felder[17] = -3;
		felder[19] = -5;
		felder[24] = 2;
	}

	private int[] rollDice() {
		int[] dice = new int[2];
		for (int x = 0; x < dice.length; x++)
			dice[x] = (int) Math.floor(Math.random() * 6) + 1;
		return dice;
	}
	private boolean testMove(int[] moves, int span) {
		for (int x = 0; x < moves.length; x++) {
			if (moves[x] == span) {
				return true;
			}
		}
		return false;
	}
	private int[] useMove(int[] moves, int span) {
		for (int x = 0; x < moves.length; x++) {
			if (moves[x] == span) {
				moves[x] = 0;
				return moves;
			}
		}
		return null;
	}
	private boolean validSource(boolean type, int position) { // true = white
		return !type && felder[position] > 0 || type && felder[position] < 0;
	}
	private boolean validTarget(boolean type, int position) { // true = white
		return !type && felder[position] > -2 || type && felder[position] < 2;
	}
	private boolean take(boolean type, int position) { // true = white
		if (validSource(type, position)) {
			felder[position] -= type ? -1 : 1;
			return true;
		}
		return false;
	}
	private boolean place(boolean type, int position) { // true = white
		if (validTarget(type, position)) {
			felder[position] += type ? -1 : 1;
			if (felder[position] == 0) {
				felder[position] = type ? -1 : 1;
				felder[26 + (!type ? 1 : 0)] += !type ? -1 : 1; 
			}
			return true;
		}
		return false;
	}
	private String drawBoardInfo(int feld, boolean orientation, int type) {
		switch (type) {
		case 0:
			return Character.toString(alphabet[feld]);
		case 1:
			return orientation ? "^" : "v"; 
		case 2:
			return felder[feld] == 0 ? " " : felder[feld] < 0 ? "W" : "B"; 
		case 3:
			return felder[feld] == 0 ? " " : Integer.toString(Math.min(Math.abs(felder[feld]), 9)); 
		}
		return null;
	}
	private void drawBoard(int[] dice) {
		for (int y = 0; y < 4; y++) {
			for (int x = 13; x < 26; x++) {
				sysout.print(drawBoardInfo(x, false, y));
				if (x == 18 || x == 24)
					sysout.print(' ');
			}
			sysout.println();
		}
		sysout.println();
		for (int y = 3; y >= 0; y--) {
			for (int x = 12; x >= 0; x--) {
				sysout.print(drawBoardInfo(x, true, y));
				if (x == 7 || x == 1)
					sysout.print(' ');
			}
			sysout.println();
		}
		sysout.println("Wartet: Bx" + felder[26] + ", Wx" + Math.round(felder[27]));
		sysout.println("Würfel: " + dice[0] + " " + dice[1]);
	}
	private String pad(String text, int len) {
		while (text.length() < len)
			text += " ";
		return text;
	}
	private int getPosition(String target) {
		target = target.toUpperCase();
		if (target.equals(" ")) return -1;
		if (target.length() != 1) return -1;
		for (int i = 0; i < alphabet.length; i++)
			if (target.charAt(0) == alphabet[i])
				return i;
		return -1;
	}
	private boolean getWinner() {
		return (felder[0] >= 16 || felder[25] <= -16);
	}
	private int[] newSpieler() {
		int[] spieler = new int[felder.length];
		for (int i = 0; i < spieler.length; i++)
			spieler[i] = -1;
		return spieler;
	}
}
