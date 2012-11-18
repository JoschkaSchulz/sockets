package basic.chat;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Emfanger für den HuebnerChat
 * 
 * Wird verwendet um Packete von anderen Clienten zu empfangen
 *
 */
final class HuebnerReceiver implements Runnable {
	private final HuebnerChat huebnerChat;

	/**
	 * Standardkonstrucktor
	 * Setzt den Chat(die Verbindung)
	 * 
	 * @param huebnerChat Der aktuelle Chat des Clienten
	 */
	HuebnerReceiver(HuebnerChat huebnerChat) {
		this.huebnerChat = huebnerChat;
	}

	/**
	 * Horcht auf dem Port 50001 nach Paketen von anderen Clienten
	 */
	public void run() {
		try {
			DatagramSocket clientSocket = new DatagramSocket(50001);
			byte[] receiveData = new byte[1024];
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			while (true) {
				clientSocket.receive(receivePacket);
				String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
				String[] split = message.split(": ", 2);	//Spalte die nachricht in [0] Benutzer und [1] Nachricht
				this.huebnerChat.sysout.println(split[0].replace('_', ' ') + ": " + split[1]);
				this.huebnerChat.getGUI().appendText(split[0].replace('_', ' ') + ": " + split[1], HuebnerGUI.SET_ORANGE); //Gebe die Packete auf der GUI aus
			}
			//clientSocket.close();
		} catch (Exception e) {
			
		}
	}
}