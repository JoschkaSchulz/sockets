/**
 * MailDrop
 * 
 * Der Maildrop hält die ganzen mails von einer Verbindung. Auch die
 * makierungen für die mails welche gelöscht werden sollen sind hier
 * enthalten.
 */

package pop3.server;

import java.util.ArrayList;

public class Maildrop {
	public ArrayList<String> mails = new ArrayList<String>();
	public ArrayList<Integer> uidls = new ArrayList<Integer>();
	public ArrayList<Integer> marked = new ArrayList<Integer>();
	public int getSize() {
		int size = 0;
		for (String mail : mails)
			size += mail.length();
		return size;
	}
	public int getSize(int id) {
		return mails.get(id).length();
	}
	
	public void putText(String text) {
		StringBuilder sb = new StringBuilder();
		sb.append("From: \"E-Mail Administrator\" <postmaster@localhost>\n");
		sb.append("To: joschka@localhost\n");
		sb.append("Subject: Hello World\n");
		sb.append("Date: Fre, 21 Dec 2012 13:37:00 +0200\n");
		sb.append("MIME-Version: 1.0\n");
		sb.append("Content-type: Text/plain; charset=US-ASCII\n\n");
		sb.append(text);
		putSample(sb.toString());
	}
	
	public void putSample(String mail) {
		mails.add(mail);
		uidls.add(mail.hashCode());
	}
	public void putMail(String mail, int uidl) {
		mails.add(mail);
		uidls.add(uidl);
	}
	public void putMails(Maildrop maildrop) {
		for (String mail : maildrop.mails)
			mails.add(mail);
		for (Integer uidl : maildrop.uidls)
			uidls.add(uidl);
	}
	
	public Maildrop() {
	}
}
