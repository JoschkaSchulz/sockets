package basic.chat;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map.Entry;

class HuebnerSender implements Runnable {
	private final HuebnerChat huebnerChat;
	private final String target;
	private final String message;
	
	public HuebnerSender(HuebnerChat huebnerChat, String message) {
		this.huebnerChat = huebnerChat;
		this.target = null;
		this.message = message;
	}
	public HuebnerSender(HuebnerChat huebnerChat, String message, String target) {
		this.huebnerChat = huebnerChat;
		this.target = target;
		this.message = message;
	}
	
	public void run() {
		for (Entry<String, String> pair : this.huebnerChat.players.entrySet()) {
			if (target != null && target != pair.getValue()) continue;
			if (pair.getValue().equals(this.huebnerChat.myName)) continue;
			try {
				DatagramSocket clientSocket = new DatagramSocket();
				InetAddress IPAddress = InetAddress.getByName(pair.getKey());
				byte[] sendData = (this.huebnerChat.myName + ": " + message).getBytes();
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 50001);
				clientSocket.send(sendPacket);
			} catch (Exception e) {
				this.huebnerChat.sysout.print("* Nachricht konnte nicht an " + pair.getValue() + " zugestellt werden.");
				this.huebnerChat.getGUI().appendText("* Nachricht konnte nicht an " + pair.getValue() + " zugestellt werden.", HuebnerGUI.SET_ERROR);
			}
		}
	}
}