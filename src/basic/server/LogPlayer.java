package basic.server;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import client.BasicConnectionFilter;


public class LogPlayer implements IPlayer {
	private Random random = new Random();
	private PlayerPool playerPool;
	private LinkedList<String> messageLog = new LinkedList<String>();
	ArrayList<Integer> dice = new ArrayList<Integer>();
	private BasicConnectionFilter filter = new BasicConnectionFilter(null);
	
	public LogPlayer(PlayerPool playerPool) {
		this.playerPool = playerPool;
		// playerPool.newPlayer(this);
	}
	public boolean isOnline() {
		return false;
	}
	public void quit() {
	}
	public void storMessage(String message) {
		synchronized(messageLog) {
			messageLog.add(message);
		}
		filter.decode(message);
		if (filter.action.equals("DiceRoll")) {
			if (filter.sender >= 0) {
				if (filter.type.equals("Gam")) {
					int id = filter.asInt(0);
					while (dice.size() <= id) {
						dice.add(-1);
					}
					int rnd = random.nextInt(filter.asInt(1));
					if (dice.get(id) == -1) {
						dice.set(id, rnd);
					}
					if (dice.get(id) > -1) {
						playerPool.send(playerPool.players.get(filter.sender), "SrvDice " + id + " " + dice.get(id));
					}
				} else if (filter.type.equals("Pub")) {
					int rnd = random.nextInt(filter.asInt(0));
					playerPool.publish(null, "PubDice " + filter.sender + " " + rnd);
				}
			}
		}
		if (filter.action.equals("DiceReveal")) {
			if (filter.sender >= 0) {
				if (filter.type.equals("Gam")) {
					int id = filter.asInt(0);
					playerPool.publish(null, "SrvDice " + id + " " + dice.get(id));
				}
			}
		}
	}
	public LinkedList<String> getLog() {
		return messageLog;
	}
	public void resetLog() {
		messageLog = new LinkedList<String>();
	}
}
