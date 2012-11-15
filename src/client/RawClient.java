package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class RawClient extends BasicGame {

	private BufferedReader sysin;
	private PrintWriter sysout;
	
	public static void main(String args[]) {
		new RawClient();
	}

	public RawClient() {
		try {
			sysin = new BufferedReader(new InputStreamReader(System.in));
			sysout = new PrintWriter(new OutputStreamWriter(System.out), true);

			sysout.println("Server IP?");

			filter = new BasicConnectionFilter(new ServerConnection());
			filter.serverConnection.hostname = sysin.readLine();
			filter.serverConnection.init();
			
			init();
			run();
		} catch (Exception e) {
			
		}
	}
	
	public void onAny() {
		System.out.println(filter.message);
	}

	public void run() {
		while (true) {
			try {
				String message = sysin.readLine();
				if (message.startsWith("/quit")) {
					setSrvQuit();
					filter.serverConnection.disposeAll();
					System.exit(0);
				} else {
					filter.push(message);
				}
			} catch (Exception e) {

			}
		}
	}
	
}
