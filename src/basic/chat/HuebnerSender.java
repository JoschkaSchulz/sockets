package basic.chat;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map.Entry;

/**
 * Der Sender wird dafür verwendet an die anderen Clienten Nachrichten zu senden
 */
class HuebnerSender implements Runnable {
	private final HuebnerChat huebnerChat;
	private final String target;
	private final String message;
	
	/**
	 * Konstrucktor
	 * 
	 * @param huebnerChat Der aktuelle Chatclient
	 * @param message Die Nachricht die gesendet werden soll
	 */
	public HuebnerSender(HuebnerChat huebnerChat, String message) {
		this.huebnerChat = huebnerChat;
		this.target = null;
		this.message = message;
	}
	
	/**
	 * Konstrucktor
	 * 
	 * @param huebnerChat Der aktuelle Chatclient
	 * @param message Die Nachricht die gesendet werden soll
	 * @param target Das Ziel an das gesendet werden soll
	 */
	public HuebnerSender(HuebnerChat huebnerChat, String message, String target) {
		this.huebnerChat = huebnerChat;
		this.target = target;
		this.message = message;
	}
	
	/**
	 * Sendet entweder an alle oder nur an das angegebee Ziel die 'message'
	 */
	public void run() {
		for (Entry<String, String> pair : this.huebnerChat.players.entrySet()) {
			if (target != null && target != pair.getValue()) continue;	//Wenn es an ein Ziel gesendet werden soll und dies nicht das Ziel ist weiter!
			if (pair.getValue().equals(this.huebnerChat.myName)) continue;	
			try {
				DatagramSocket clientSocket = new DatagramSocket();	
				InetAddress IPAddress = InetAddress.getByName(pair.getKey()); 
				byte[] sendData = (this.huebnerChat.myName + ": " + message).getBytes(); //Bereite das senden vor
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 50001); 
				clientSocket.send(sendPacket); //Sende das Datagram
			} catch (Exception e) {
				this.huebnerChat.sysout.print("* Nachricht konnte nicht an " + pair.getValue() + " zugestellt werden.");
				this.huebnerChat.getGUI().appendText("* Nachricht konnte nicht an " + pair.getValue() + " zugestellt werden.", HuebnerGUI.SET_ERROR);
			}
		}
	}
}