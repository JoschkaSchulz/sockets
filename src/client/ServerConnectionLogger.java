package client;

import java.awt.BorderLayout;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class ServerConnectionLogger extends JFrame implements ILogger {

	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JLabel infoLabel = null;
	private JScrollPane jScrollPane = null;
	private JTextArea jTextArea = null;
	private StringBuilder stringBuilder = new StringBuilder();  //  @jve:decl-index=0:
	public ServerConnection serverConnection;

	/**
	 * This is the default constructor
	 */
	public ServerConnectionLogger() {
		super();
		initialize();
		new Timer().schedule(new TimerTask() {
			public void run() {
				if (serverConnection != null)
					infoLabel.setText(serverConnection.toString());
			}
		}, 0, 5000);
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(380, 335);
		this.setContentPane(getJContentPane());
		this.setTitle("ServerConnection Logger");
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			infoLabel = new JLabel();
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(infoLabel, BorderLayout.NORTH);
			jContentPane.add(getJScrollPane(), BorderLayout.CENTER);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getJTextArea());
		}
		return jScrollPane;
	}

	/**
	 * This method initializes jTextArea	
	 * 	
	 * @return javax.swing.JTextArea	
	 */
	private JTextArea getJTextArea() {
		if (jTextArea == null) {
			jTextArea = new JTextArea();
		}
		return jTextArea;
	}
	
	/* (non-Javadoc)
	 * @see client.ILogger#addLine(boolean, java.lang.String)
	 */
	public void addLine(boolean reading, String message) {
		stringBuilder.append((reading ? "Server: " : "Client: ") + message + "\n");
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				infoLabel.setText(serverConnection.toString());
				jTextArea.setText(stringBuilder.toString());
			}
		});
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
