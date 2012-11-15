package client;

import javax.swing.SwingUtilities;

public class Controller {

	private ServerConnectionLogger logger;
	private ServerConnection connection;
	private BasicConnectionFilter filter;
	private Object pause = new Object();

	public Controller() {
		connection = new ServerConnection();
		filter = new BasicConnectionFilter(connection);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				logger = new ServerConnectionLogger();
				logger.setVisible(true);
				logger.serverConnection = connection;
				filter.loggers.add(logger);
				connection.hostname = "localhost";
				connection.port = 4444;
				connection.init();
				synchronized(pause) {
					pause.notify();
				}
			}
		});
		synchronized(pause) {
			try {
				if (!connection.isOnline) pause.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		synchronized(connection.inList) {
			try {
				while (true) {
					connection.inList.wait();
					while (connection.inList.size() > 0)
						filter.pull();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new Controller();
	}

}
