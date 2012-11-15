package client;

import java.util.LinkedList;

public class BasicGame {
	public int playerId = -1;
	public int gameId = -1;

	public int onTurn = 0;
	public int minPlayers = 0;
	public int maxPlayers = 0;
	public boolean lock = false;
	public boolean yourTurn = false;
	
	public LinkedList<BasicPlayer> players = new LinkedList<BasicPlayer>();
	
	public BasicConnectionFilter filter;

	public Thread reader;
	
	public void init() {
		reader = new Thread("Reader") {
			public void run() {
				doRead();
			}
		};
		reader.start();
	}
	
	public void doRead() {
		while (true) {
			filter.serverConnection.listen();
			do {
				if (filter.pull() == null) break;
				read();
			} while (true);
		}
	}
	
	public void read() {
		try {
			if (filter.type.equals("Srv")) {
				if (filter.action.equals("MinPlayers")) {
					minPlayers = filter.asInt(0);
					onSrvMinPlayers(minPlayers);
				}
				else if (filter.action.equals("MaxPlayers")) {
					maxPlayers = filter.asInt(0);
					onSrvMaxPlayers(maxPlayers);
				}
				else if (filter.action.equals("NextTurn")) {
					onTurn = filter.asInt(0);
					onSrvNextTurn(onTurn);
				}
				else if (filter.action.equals("NewPlayer")) {
					BasicPlayer player = newPlayer();
					player.id = filter.asInt(0);
					player.name = newPlayerName(player.id);
					players.add(player);
					onSrvNewPlayer(player.id);
				}
				else if (filter.action.equals("Quit")) {
					players.get(filter.asInt(0)).isOnline = false;
					onSrvQuit(filter.asInt(0));
				}
				else if (filter.action.equals("Welcome")) {
					BasicPlayer player = newPlayer();
					player.id = filter.asInt(0);
					player.name = newPlayerName(player.id);
					players.add(player);
					playerId = player.id;
					onSrvWelcome(playerId);
				}
				else if (filter.action.equals("Lock")) {
					lock = true;
					onSrvLock();
				}
				else if (filter.action.equals("RebuildLog")) {
					onSrvRebuildLog();
				}
				else if (filter.action.equals("YourTurn")) {
					yourTurn = true;
					onSrvYourTurn();
				}
				else if (filter.action.equals("ThankYou")) {
					yourTurn = false;
					onSrvThankYou();
				}
				else if (filter.action.equals("AbortTurn")) {
					yourTurn = false;
					onSrvAbortTurn();
				}
			} else if (filter.type.equals("Pub") || filter.type.equals("All") || filter.type.equals("Mod") || filter.type.equals("Uni") || filter.type.equals("Slf")) {
				if (filter.action.equals("Name")) {
					String old = players.get(filter.sender).name;
					players.get(filter.sender).name = filter.parameter;
					onAnyName(filter.sender, filter.parameter, old);
				}
				if (filter.action.equals("Chat"))
					onAnyChat(filter.sender, filter.parameter);
				onAny();
			} else if (filter.type.equals("Gam")) {
				onGam();
			}
		} catch (Exception e) {
			System.err.println("illegal pattern " + e.toString());
			e.printStackTrace();
		}
	}

	public BasicPlayer newPlayer() {
		return new BasicPlayer();
	}
	public String newPlayerName(int id) {
		return "Spieler " + id;
	}
	
	public void onSrvMinPlayers(int i) {}

	public void setSrvMinPlayers(int i) {
		if (yourTurn)
			filter.push("SrvMinPlayers " + i);
	}

	public void onSrvMaxPlayers(int i) {}

	public void setSrvMaxPlayers(int i) {
		if (yourTurn)
			filter.push("SrvMaxPlayers " + i);
	}

	public void onSrvLock() {}

	public void setSrvLock() {
		if (yourTurn)
			filter.push("SrvLock");
	}

	public void onSrvQuit(int id) {}

	public void setSrvQuit() {
		filter.push("SrvQuit");
	}

	public void onSrvRebuildLog() {
		filter.push("PubName " + players.get(playerId).name);
	}

	public void onSrvYourTurn() {}

	public void onSrvThankYou() {}

	public void onSrvAbortTurn() {}

	public void onSrvNextTurn(int id) {}

	public void setSrvNextTurn() {
		filter.push("SrvNextTurn");
	}

	public void onSrvNewPlayer(int id) {}

	public void onSrvWelcome(int id) {}

	public void onAnyName(int id, String name, String oldName) {}

	public void setAnyName(String name) {
		players.get(playerId).name = name;
		// onAnyName(playerId, name);
		filter.push("PubName " + name);
	}
	
	public void onAnyChat(int id, String message) {}

	public void setAnyChat(String message) {
		// onAnyChat(playerId, message);
		filter.push("PubChat " + message);
	}
	
	public void onAny() {}

	public void onGam() {}

}