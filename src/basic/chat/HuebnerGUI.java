package basic.chat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class HuebnerGUI extends JFrame {	
	private HuebnerChat chat;
	private String outputBuffer;
	
	private JPanel panel;
	private JScrollPane scrollPane;
	private JTextField textField;
	private JTextPane textArea;
	private JList<String> playerList;
	
	private DefaultListModel<String> listModel;
	
	public static SimpleAttributeSet SET_ORANGE = new SimpleAttributeSet();
	public static SimpleAttributeSet SET_BLACK = new SimpleAttributeSet();

	public static SimpleAttributeSet SET_ERROR = new SimpleAttributeSet();
	public static SimpleAttributeSet SET_ADMIN = new SimpleAttributeSet();
	
	static {
		StyleConstants.setForeground(SET_ORANGE, Color.ORANGE);
		StyleConstants.setForeground(SET_BLACK, Color.BLACK);

		StyleConstants.setForeground(SET_ADMIN, Color.BLUE);
		StyleConstants.setBold(SET_ADMIN, true);
		
		StyleConstants.setForeground(SET_ERROR, Color.RED);
		StyleConstants.setBold(SET_ERROR, true);
	}
	
	public HuebnerGUI() {
		this.setUI();
		
		this.outputBuffer = "";
		
		setTitle("Rechnernetze Chat");
	    setSize(600, 500);
	    setLocationRelativeTo(null);
    	setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	public void start(final HuebnerGUI ui) {
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	HuebnerGUI gui = ui;
            	gui.setVisible(true);
            }
        });
	}
	
	public void setHuebnerChat(HuebnerChat chat) {
		this.chat =  chat;
	}
	
	public String getOutputBuffer() {
		String output = this.outputBuffer;
		this.appendText(this.outputBuffer+"\n", SET_ADMIN);
		this.outputBuffer = "";
		return output;
	}
	
	public void fillOutputBuffer(String input) {
		this.outputBuffer = input;
	}
	
	public boolean isOutputBufferfilled() {
		if(this.outputBuffer.length() > 0) return true;
		else return false;
	}
	
	public void setUI() {
		panel = new JPanel(new BorderLayout());
		
		scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		panel.add(scrollPane, BorderLayout.CENTER);
		
		textField = new JTextField();
		textField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				if(arg0.getKeyCode() == 10) {
					//Wenn Enter gedrückt wurde!
					fillOutputBuffer(textField.getText()+"");
					textField.setText("");
				}
			}
		});
		panel.add(textField, BorderLayout.SOUTH);
		textField.setColumns(10);
		
		playerList = new JList<String>();
		listModel = new DefaultListModel<String>();
		listModel.addElement("Bitte einloggen!");
		playerList.setModel(listModel);
		panel.add(playerList, BorderLayout.EAST);
		
		textArea = new JTextPane();
		textArea.setAutoscrolls(true);
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);
		
		this.add(panel);
	}
	
	public void setPlayers(HashMap<String, String> players) {
		listModel.clear();
		for (Map.Entry<String, String> entry : players.entrySet()) {
			listModel.addElement(entry.getValue());
		}
		playerList.setModel(listModel);
	}
	
	public void appendText(String text, AttributeSet set) {
		try {
			this.textArea.getDocument().insertString(this.textArea.getDocument().getLength(), text, set);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		
		this.setScrolbarMax();
	}
	
	public void setScrolbarMax() {
		scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
	}
}
