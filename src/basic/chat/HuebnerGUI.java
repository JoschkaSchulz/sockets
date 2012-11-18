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
	
	/*
	 * Variablen
	 */
	private HuebnerChat chat;
	private String outputBuffer;
	
	private JPanel panel;
	private JScrollPane scrollPane;
	private JTextField textField;
	private JTextPane textArea;
	private JList<String> playerList;
	
	private DefaultListModel<String> listModel;
	
	/*
	 * Static Attribute für die Chatfarben
	 */
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
	
	/**
	 * Standardkonstrucktor
	 * Definiert die Eigenschaften des Fensters.
	 */
	public HuebnerGUI() {
		this.setUI();
		
		this.outputBuffer = "";
		
		setTitle("Rechnernetze Chat");
	    setSize(600, 500);
	    setLocationRelativeTo(null);
    	setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	/**
	 * Startet das Fenster
	 * 
	 * @param ui Die GUI die geöffnet werden soll
	 */
	public void start(final HuebnerGUI ui) {
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	HuebnerGUI gui = ui;
            	gui.setVisible(true);
            }
        });
	}
	
	/**
	 * Setzt die HuebnerChat Klasse
	 * 
	 * @param chat die Chat Klasse die für die Verbindung zuständig ist
	 */
	public void setHuebnerChat(HuebnerChat chat) {
		this.chat =  chat;
	}
	
	/**
	 * Methode um den OutputBuffer zu lesen
	 * 
	 * @return
	 */
	public String getOutputBuffer() {
		String output = this.outputBuffer;
		this.appendText(this.outputBuffer+"\n", SET_ADMIN);	//Ausgabe auf der Konsole wenn der Buffer abgefragt wird in Blau
		this.outputBuffer = "";
		return output;
	}
	
	/**
	 * Füllt den Output Buffer
	 * 
	 * @param input Das was in den OutputBuffer geschrieben werden soll
	 */
	public void fillOutputBuffer(String input) {
		this.outputBuffer = input;
	}
	
	/**
	 * Fragt ab ob etwas im OutputBuffer steht
	 * 
	 * @return true wenn etwas im Buffer steht, sonst false
	 */
	public boolean isOutputBufferfilled() {
		if(this.outputBuffer.length() > 0) return true;
		else return false;
	}
	
	/**
	 * Erstellt die GUI und einen KeyListener für die Eingabe der Enter Taste
	 */
	public void setUI() {
		panel = new JPanel(new BorderLayout());
		
		scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		panel.add(scrollPane, BorderLayout.CENTER);
		
		textField = new JTextField();
		
		//Fügt einen KeyListener hinzu der die Return-Taste abfragt(10), dann wird das Textfeld in den OutputBuffer geschrieben
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
	
	/**
	 * Füllt die Liste der Anwesenden im Chat mit Benutzern
	 * 
	 * @param players Liste der Benutzter
	 */
	public void setPlayers(HashMap<String, String> players) {
		listModel.clear();
		for (Map.Entry<String, String> entry : players.entrySet()) {
			listModel.addElement(entry.getValue());
		}
		playerList.setModel(listModel);
	}
	
	/**
	 * Fügt dem Chatfenster Text hinzu
	 * 
	 * @param text Text der hinzugefügt werden soll
	 * @param set Formatierung des Chats
	 */
	public void appendText(String text, AttributeSet set) {
		try {
			this.textArea.getDocument().insertString(this.textArea.getDocument().getLength(), text, set);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		
		this.setScrolbarMax();
	}
	
	/**
	 * Setzt die Scrollleiste nach ganz unten
	 */
	public void setScrolbarMax() {
		scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
	}
}
