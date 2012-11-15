package basic.server;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * SrvThankYou			Sie sind nicht mehr am Zug
 * SrvYourTurn			Sie sind am Zug
 * SrvNextTurn player	Spieler ist am Zug
 * SrvAbortTurn			Zug wurde vorzeitig abgebrochen
 * SrvBeQuiet			Sie sind nicht am Zug, Gam-Nachricht ignoriert
 * 
 * SrvNextTurn			Spieler am Zug möchte Zug abgeben
 * SrvQuit				Spieler möchte gehen
 * 
 * SrvResetLog			Spieler am Zug möchte Log aufräumen
 * SrvRebuildLog		Server möchte alle wichtigen Informationen wiederholt haben, die im Log stehen müssen
 * 
 * SrvMinPlayers num	Beeinflusst Zugwechsel sofern Spieler am Zug
 * SrvMaxPlayers num	Beeinflusst Zugwechsel sofern Spieler am Zug
 * SrvLock				Verhindert Verstellen der Anzahl der Spieler
 * 
 * SrvNewPlayer player	Neuer Spieler
 * SrvQuit player		Spieler ist gegangen
 * SrvWelcome player	Ihre Spielernummer
 * 
 * SrvIllegal			Unbekannter Befehl
 * 
 * Gam*			Spieler am Zug sendet an alle anderen
 * Mod*			Spieler sendet an Spieler am Zug
 * Uni* player	Spieler sendet an Spieler
 * Pub*			Spieler sendet an alle anderen
 * All*			Spieler sendet an alle
 * Slf*			Spieler sendet an sich selbst
 * 
 * 
 * @author U705426
 *
 */
public class PlayerPool extends BasicPlayerPool {
	public ArrayList<IPlayer> players = new ArrayList<IPlayer>();
	LogPlayer logPlayer = new LogPlayer(this);
	int onTurn = 0;
	int minPlayers = 0;
	int maxPlayers = 0;
	boolean lock;

	Timer timer = new Timer();

	public PlayerPool() {
		resetLog();
		timer.schedule(new TimerTask() {
			public void run() {
				if (onTurn < players.size())
					send(players.get(onTurn), "");
			}
		}, 0, 5000);
	}

	public String originate(IPlayer player, String message) {
		String[] msg = message.split(" ", 2);
		msg[0] += " " + players.indexOf(player);
		if (msg.length == 2)
			return msg[0] + " " + msg[1];
		else
			return msg[0];
	}
	public String originate(int id, String message) {
		String[] msg = message.split(" ", 2);
		msg[0] += " " + id;
		if (msg.length == 2)
			return msg[0] + " " + msg[1];
		else
			return msg[0];
	}
	public IPlayer originate(String message) {
		String[] msg = message.split(" ", 3);
		IPlayer player = null;
		try {
			player = players.get(Integer.parseInt(msg[1]));
		} catch (Exception e) {
			
		}
		return player;
	}
	public void newPlayer(IPlayer player) {
		synchronized(logPlayer.getLog()) {
			for (String message : logPlayer.getLog()) {
				send(player, message);
			}
		}
		
		/*for (IPlayer aPlayer : players) {
			send(player, originate(aPlayer, "SrvNewPlayer"));
		}*/
		players.add(player);
		int id = players.indexOf(player);
		publish(player, originate(id, "SrvNewPlayer"));
		//send(player, originate(onTurn, "SrvNextTurn"));
		send(player, originate(id, "SrvWelcome"));
		if (onTurn == id)
			send(player, "SrvYourTurn");
	}
	public void resetLog() {
		logPlayer.resetLog();
		send(logPlayer, originate(minPlayers, "SrvMinPlayers"));
		send(logPlayer, originate(maxPlayers, "SrvMaxPlayers"));
		if (lock)
			send(logPlayer, "SrvLock");
		for (IPlayer aPlayer : players)
			send(logPlayer, originate(aPlayer, "SrvNewPlayer"));
		send(logPlayer, originate(onTurn, "SrvNextTurn"));
	}
	void nextTurn() {
		send(players.get(onTurn), "SrvThankYou");
		int skip = onTurn;
		do {
			onTurn++;
			if ((maxPlayers != 0 && onTurn >= maxPlayers) || onTurn >= Math.max(minPlayers, players.size()))
				onTurn = 0;
			if (onTurn >= players.size() || players.get(onTurn).isOnline()) break;
		} while (onTurn != skip);
		if (onTurn < players.size() && !players.get(onTurn).isOnline())
			onTurn = players.size();
		publish(null, originate(onTurn, "SrvNextTurn"));
		if (onTurn != players.size())
			send(players.get(onTurn), "SrvYourTurn");
	}
	public void commit(IPlayer player, String message) {
		if (message.length() > 0)
			System.out.println("S « " + players.indexOf(player) + ": " + message);
		if (message.startsWith("Gam")) {
			/**
			 * send to all but sender
			 * incoming: GamMove payload
			 * outgoing: GamMove payload
			 */
			if (player.equals(players.get(onTurn))) {
				publish(player, message);
			} else {
				send(player, "SrvBeQuiet");
			}
		} else if (message.startsWith("Uni")) {
			/**
			 * send to recipient
			 * incoming: UniTalk recipient payload
			 * outgoing: UniTalk sender payload
			 */
			String[] split = message.split(" ", 3);
			send(originate(message), originate(player, split[0] + (split.length > 2 ? " " + split[2] : "")));
		} else if (message.startsWith("Mod")) {
			/**
			 * send to player on turn as a Moderator
			 * incoming: ModTalk payload
			 * outgoing: ModTalk sender payload
			 */
			send(players.get(onTurn), originate(player, message));
		} else if (message.startsWith("Pub")) {
			/**
			 * send to all but sender
			 * incoming: PubTalk payload
			 * outgoing: PubTalk sender payload
			 */
			publish(player, originate(player, message));
		} else if (message.startsWith("All")) {
			/**
			 * send to all
			 * incoming: PubTalk payload
			 * outgoing: PubTalk sender payload
			 */
			publish(null, originate(player, message));
		} else if (message.startsWith("Slf")) { 
			/**
			 * send to yourself
			 * incoming: SlfTalk payload
			 * outgoing: SlfTalk payload
			 */
			send(player, message);
		} else if (message.startsWith("SrvMinPlayers")) {
			if (!lock && player.equals(players.get(onTurn))) {
				minPlayers = Integer.parseInt(message.split(" ", 2)[1]);
				publish(null, originate(minPlayers, "SrvMinPlayers"));
			} else {
				send(player, "SrvBeQuiet");
			}
		} else if (message.startsWith("SrvMaxPlayers")) {
			if (!lock && player.equals(players.get(onTurn))) {
				maxPlayers = Integer.parseInt(message.split(" ", 2)[1]);
				publish(null, originate(maxPlayers, "SrvMaxPlayers"));
			} else {
				send(player, "SrvBeQuiet");
			}
		} else if (message.equals("SrvLock")) {
			if (!lock && player.equals(players.get(onTurn))) {
				lock = true;
				publish(null, "SrvLock");
			} else {
				send(player, "SrvBeQuiet");
			}
		} else if (message.equals("SrvResetLog")) {
			if (player.equals(players.get(onTurn))) {
				resetLog();
				publish(null, "SrvRebuildLog");
			} else {
				send(player, "SrvBeQuiet");
			}
		} else if (message.equals("SrvNextTurn")) {
			/**
			 * give turn to next player
			 * incoming: SrvNextTurn
			 * outgoing: SrvThankYou
			 * outgoing: SrvQuit someone
			 * outgoing: SrvNextTurn someone
			 * outgoing: SrvYourTurn
			 */
			if (player.equals(players.get(onTurn))) {
				nextTurn();
			} else {
				send(player, "SrvBeQuiet");
			}
		} else if (message.equals("SrvQuit")) {
			/**
			 * quit, and possibly give turn to next player
			 * incoming: SrvQuit
			 * outgoing: SrvQuit someone
			 * 
			 * if it is your turn:
			 * outgoing: SrvAbortTurn
			 * outgoing: see what happens if message.equals("SrvNextTurn")
			 */
			if (player.equals(players.get(onTurn))) {
				publish(player, "SrvAbortTurn");
				publish(player, originate(player, "SrvQuit"));
				nextTurn();
			} else {
				publish(player, originate(player, "SrvQuit"));
			}
			player.quit();
		} else if (message.equals("SrvNextDice")) {
			send(player, "SrvNextDice " + logPlayer.dice.size());
		} else {
			send(player, "SrvIllegal");
		}
	}
	public void publish(IPlayer player, String message) {
		for (IPlayer aPlayer : players) {
			if (!aPlayer.equals(player))
				send(aPlayer, message);
		}
		send(logPlayer, message);
	}
	public void send(IPlayer player, String message) {
		if (player.isOnline() || player.equals(logPlayer)) {
			if (message.length() > 0)
				System.out.println("S » " + (player.equals(logPlayer) ? "L" : (players.indexOf(player) == -1 ? "N" : players.indexOf(player))) + ": " + message);
			player.storMessage(message);
		}
	}
}
