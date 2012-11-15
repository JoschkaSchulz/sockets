package basic.server;

public class BasicPlayerPool {
	public BasicPlayerPool() {
		super();
	}
	public void newPlayer(IPlayer player) {
	}
	public void commit(IPlayer player, String message) {
		if (message.length() > 0)
			System.out.println("S « " + player + ": " + message);
	}
	public void send(IPlayer player, String message) {
		if (player.isOnline()) {
			if (message.length() > 0)
				System.out.println("S » " + player + ": " + message);
			player.storMessage(message);
		}
	}
	public void disconnect(IPlayer player) {
		
	}
}