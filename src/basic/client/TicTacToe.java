package basic.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class TicTacToe {
	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	private BufferedReader sysin;
	private PrintWriter sysout;
	
	private int[] board = new int[9];
	private char[] alphabet = new char[] {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I'};
	
	private static final String[][] pieces = new String[3][3];
	
	{
		pieces[0][0] = pieces[0][1] = pieces[0][2] = "   ";
		pieces[1][0] = "\\ /"; pieces[2][0] = " _ ";
		pieces[1][1] = " X ";  pieces[2][1] = "| |";
		pieces[1][2] = "/ \\"; pieces[2][2] = " ¯ ";
	}

	public static void main(String args[]) {
		new TicTacToe();
	}

	public TicTacToe() {
		try {
			sysin = new BufferedReader(new InputStreamReader(System.in));
			sysout = new PrintWriter(new OutputStreamWriter(System.out), true);

			sysout.println("Das Spiel lautet Tic Tac Toe.");
			sysout.println();

			sysout.println("Server IP?");

			socket = new Socket(sysin.readLine(), 4444);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
			
			int player = -1;
			int onTurn = 0;
			int moves = 0;
			boolean isGameOver = false;
			boolean isLocked = false;
			sysout.println();
			drawBoard();
			while (true) {
				String message = in.readLine();
				if (message.equals("SrvYourTurn")) {
					if (isGameOver) {
						sysout.println("Drücken Se die Eingabetaste, um eine neue Runde zu beginnen");
						sysin.readLine();
						isGameOver = false;
						out.println("GamReset");
						out.println("SrvResetLog");
						for (int i = 0; i < 9; i++)
							board[i] = 0;
						moves = 0;
						sysout.println();
						drawBoard();
					}
					if (!isLocked) {
						out.println("SrvMinPlayers 2");
						out.println("SrvMaxPlayers 2");
						out.println("SrvLock");
						isLocked = true;
					}
					sysout.println("Sie sind am Zug! Bitte Feld benennen, um ein " + (player == 0 ? 'X' : 'O') + " zu platzieren");
					String text = "";
					while (getPosition(text) == -1 || board[getPosition(text)] != 0) {
						text = sysin.readLine();
					}
					board[getPosition(text)] = player + 1;
					sysout.println();
					drawBoard();
					moves++;
					out.println("GamMove " + text);
					if (getWinner() >= 0) {
						sysout.println("Spieler " + (getWinner() == 0 ? 'X' : 'O') + " gewinnt!");
						out.println("GamOver");
						out.println("SrvNextTurn");
						isGameOver = true;
					} else if (moves >= 9) {
						sysout.println("Unentschieden!");
						out.println("GamOver");
						out.println("SrvNextTurn");
						isGameOver = true;
					} else {
						out.println("SrvNextTurn");
					}
				} else if (message.startsWith("GamMove")) {
					board[getPosition(message.split(" ", 2)[1])] = onTurn + 1;
					sysout.println();
					drawBoard();
					moves++;
					if (getWinner() >= 0)
						sysout.println("Spieler " + (getWinner() == 0 ? 'X' : 'O') + " gewinnt!");
					 else if (moves >= 9)
						sysout.println("Unentschieden!");
				} else if (message.startsWith("GamOver")) {
					isGameOver = true;
				} else if (message.startsWith("GamReset")) {
					isGameOver = false;
					for (int i = 0; i < 9; i++)
						board[i] = 0;
					moves = 0;
					sysout.println();
					drawBoard();
				} else if (message.startsWith("SrvNextTurn")) {
					onTurn = Integer.parseInt(message.split(" ", 2)[1]);
					sysout.println("Spieler " + (onTurn == 0 ? 'X' : 'O') + " ist am Zug.");
				} else if (message.startsWith("SrvNewPlayer")) {
					int i = Integer.parseInt(message.split(" ", 2)[1]);
					if (i < 2)
						sysout.println("Neuer Spieler: " + (i == 0 ? 'X' : 'O'));
					else
						sysout.println("Neuer Zuschauer: " + i);
				} else if (message.startsWith("SrvLock")) {
					isLocked = true;
				} else if (message.startsWith("SrvWelcome")) {
					player = Integer.parseInt(message.split(" ", 2)[1]);
					if (player < 2)
						sysout.println("Ihre Farbe: " + (player == 0 ? 'X' : 'O'));
					else
						sysout.println("Ihre Zuschauernummer: " + player);
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

	private void drawBoard() {
		for (int y = 0; y < 3; y++) {
			if (y > 0)
				sysout.println("-----+-----+-----");
			for (int y1 = 0; y1 < 3; y1++) {
				for (int x = 0; x < 3; x++) {
					if (x > 0)
						sysout.print("|");
					sysout.print((y1 == 0 ? alphabet[y * 3 + x] : ' ') + drawPiece(board[y * 3 + x], y1) + " ");
				}
				sysout.println();
			}
		}
	}
	private String drawPiece(int piece, int row) {
		return pieces[piece][row];
	}
	private int getPosition(String target) {
		target = target.toUpperCase();
		if (target.length() != 1) return -1;
		for (int i = 0; i < 9; i++)
			if (target.charAt(0) == alphabet[i])
				return i;
		return -1;
	}
	private int getWinner() {
		int[] solutions = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 0, 3, 6, 1, 4, 7, 2, 5, 8, 0, 4, 8, 2, 4, 6 };
		for (int i = 0; i < 22; i += 3)
			if (board[solutions[i]] > 0 &&
					board[solutions[i]] == board[solutions[i + 1]] &&
					board[solutions[i]] == board[solutions[i + 2]])
				return board[solutions[i]];
		return -1;
	}
}
