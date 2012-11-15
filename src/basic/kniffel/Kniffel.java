package basic.kniffel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;
import java.util.regex.Pattern;

public class Kniffel {
	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	private BufferedReader sysin;
	private PrintWriter sysout;
	
	private LinkedList<int[]> spieler = new LinkedList<int[]>();
	private LinkedList<String> namen = new LinkedList<String>();
	private String meinName;
	private IKniffelFeld[] felder = new IKniffelFeld[] {
			new AugenFeld(1),
			new AugenFeld(2),
			new AugenFeld(3),
			new AugenFeld(4),
			new AugenFeld(5),
			new AugenFeld(6),
			null,
			new PaschFeld(2, 1, 0, 0, 0, "Paar"),
			new PaschFeld(2, 2, 0, 0, 0, "Zwei Paare"),
			new PaschFeld(3, 1, 0, 0, 0, "Dreierpasch"),
			new PaschFeld(4, 1, 0, 0, 0, "Viererpasch"),
			new PaschFeld(3, 1, 2, 1, 25, "Full House"),
			new StrassenFeld(4, 30, "Kleine"),
			new StrassenFeld(5, 40, "Große"),
			new PaschFeld(5, 1, 0, 0, 50, "Yahtzee!"),
			new KniffelFeld()
	};
	private char[] alphabet = new char[] {'1', '2', '3', '4', '5', '6',
			' ', 'P', 'Z', 'D', 'V', 'F', 'K', 'G', 'Y', 'C'};

	private int moves;

	int player = -1;
	int onTurn = 0;
	int[] dice = null;

	public static void main(String args[]) {
		new Kniffel();
	}

	public Kniffel() {
		try {
			sysin = new BufferedReader(new InputStreamReader(System.in));
			sysout = new PrintWriter(new OutputStreamWriter(System.out), true);

			sysout.println("Das Spiel lautet Kniffel.");
			sysout.println();

			sysout.println("Ihr Name?");
			
			meinName = sysin.readLine();

			sysout.println("Server IP?");

			socket = new Socket(sysin.readLine(), 4444);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
			
			int maxMoves = 0;
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
						for (int i = 0; i < spieler.size(); i++)
							spieler.set(i, newSpieler());
						moves = 0;
						maxMoves = 0;
					}
					do {
						dice = rollDice(5);
						
						String diceText = "";
						for (int i : dice)
							diceText += " " + i;
						out.println("GamRoll" + diceText);
						int position;
						int rolls = 1;
						String text;
						drawBoard(dice);
						sysout.println("Benennen Sie die zu behaltenden Würfel oder schreiben Sie den Anfangsbuchstaben der Kategorie!");
						sysout.println("Sie dürfen noch " + (3 - rolls) + "-mal würfeln.");
						do {
							text = sysin.readLine().toUpperCase();
							position = getPosition(text);
							if (rolls < 3 && Pattern.matches("[QWERT]{0,5}", text)) {
								dice = rollDice(dice, new boolean[] {text.contains("Q"),
										text.contains("W"), text.contains("E"),
										text.contains("R"), text.contains("T")});
								rolls++;
								diceText = "";
								for (int i : dice)
									diceText += " " + i;
								out.println("GamRoll" + diceText);
								drawBoard(dice);
								sysout.println("Benennen Sie die zu behaltenden Würfel oder schreiben Sie den Anfangsbuchstaben der Kategorie!");
								sysout.println("Sie dürfen noch " + (3 - rolls) + "-mal würfeln.");
								continue;
							}
						} while (position == -1 || spieler.get(player)[position] != -1);
						spieler.get(player)[position] = felder[position].eval(dice);
						moves++;
						out.println("GamMove " + moves + " " + position + " " + spieler.get(player)[position]);
					} while (moves < maxMoves);
					out.println("SrvNextTurn");
				} else if (message.startsWith("GamRoll")) {
					String[] split = message.split(" ");
					int[] dice = new int[split.length - 1];
					for (int i = 1; i < split.length; i++) {
						dice[i - 1] = Integer.parseInt(split[i]);
					}
					drawBoard(dice);
					sysout.println(namen.get(onTurn) + " hat gewürfelt.");
				} else if (message.startsWith("GamMove")) {
					int feld = Integer.parseInt(message.split(" ")[2]);
					int wert = Integer.parseInt(message.split(" ")[3]);
					int zug = Integer.parseInt(message.split(" ")[1]);
					spieler.get(onTurn)[feld] = wert;
					maxMoves = Math.max(maxMoves, zug);
					sysout.println(namen.get(onTurn) + " trägt " + wert + " Punkte in " + felder[feld].getName());
				} else if (message.equals("GamOver")) {
					sysout.println("Spiel vorbei!");
				} else if (message.equals("GamReset")) {
					for (int i = 0; i < spieler.size(); i++)
						spieler.set(i, newSpieler());
					moves = 0;
					maxMoves = 0;
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
					spieler.add(newSpieler());
					namen.add("Spieler " + i);
					sysout.println("Neuer Spieler: " + namen.get(i));
					// out.println("UniName " + i + " " + meinName);
				} else if (message.equals("SrvRebuildLog")) {
					out.println("PubName " + meinName);
				} else if (message.startsWith("SrvWelcome")) {
					player = Integer.parseInt(message.split(" ", 2)[1]);
					spieler.add(newSpieler());
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

	private int[] rollDice(int num) {
		int[] dice = new int[num];
		return rollDice(dice, null);
	}
	private int[] rollDice(int[] dice, boolean[] keep) {
		for (int x = 0; x < dice.length; x++)
			if (keep == null || keep[x] == false)
				dice[x] = (int) Math.floor(Math.random() * 6) + 1;
		return dice;
	}
	private void drawBoard(int[] dice) {
		int sum = 0;
		int zeile = 0;
		int value = 0;
		boolean thrown = false;
		int[] einSpieler;
		if (spieler.size() > onTurn)
			einSpieler = spieler.get(onTurn);
		else
			einSpieler = newSpieler();
		sysout.println("Würfel: Q W E R T      Spieler: " + namen.get(onTurn));
		sysout.print("Würfel: ");
		for (int i : dice)
			sysout.print(i + " ");
		sysout.println();
		for (IKniffelFeld feld : felder) {
			thrown = true;
			value = einSpieler[zeile];
			if (value == -1)
				thrown = false;
			if (feld == null) {
				if (sum >= 63) {
					einSpieler[zeile] = 35;
					sysout.println(pad("Bonus", 32) + "|  " + 35);
					sum += 35;
				} else
					sysout.println(pad("Bonus", 32) + "|noch " + (63 - sum));
			} else {
				if (thrown && value >= 0)
					sum += value;
				if (thrown)
					sysout.println(pad(feld.getName() + " (" + feld.getDesc() + ")", 32) + "|  " + value);
				else if ((value = feld.eval(dice)) > 0)
					sysout.println(pad(feld.getName() + " (" + feld.getDesc() + ")", 32) + "| (" + value + ")");
				else
					sysout.println(pad(feld.getName() + " (" + feld.getDesc() + ")", 32) + "|  ");
			}
			zeile++;
		}
		sysout.println(pad("Summe", 32) + "|  " + sum);
		/*sysout.println();
		for (int[] einSpieler : spieler) {
			for (int i : einSpieler) {
				sysout.print(i + " ");
			}
			sysout.println();
		}*/
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
		return (moves >= felder.length - 1);
	}
	private int[] newSpieler() {
		int[] spieler = new int[felder.length];
		for (int i = 0; i < spieler.length; i++)
			spieler[i] = -1;
		return spieler;
	}
}
