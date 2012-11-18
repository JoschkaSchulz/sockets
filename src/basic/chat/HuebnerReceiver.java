package basic.chat;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

final class HuebnerReceiver implements Runnable {
	private final HuebnerChat huebnerChat;

	HuebnerReceiver(HuebnerChat huebnerChat) {
		this.huebnerChat = huebnerChat;
	}

	public void run() {
		try {
			DatagramSocket clientSocket = new DatagramSocket(50001);
			byte[] receiveData = new byte[1024];
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			while (true) {
				clientSocket.receive(receivePacket);
				String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
				String[] split = message.split(": ", 2);
				this.huebnerChat.sysout.println(split[0].replace('_', ' ') + ": " + split[1]);
				this.huebnerChat.getGUI().appendText(split[0].replace('_', ' ') + ": " + split[1], HuebnerGUI.SET_ORANGE);
			}
			//clientSocket.close();
		} catch (Exception e) {
			
		}
	}
}