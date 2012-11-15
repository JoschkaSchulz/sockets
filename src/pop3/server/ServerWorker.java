package pop3.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

public class ServerWorker extends Thread {
	public UserPool userPool;

	public ServerWorker(String string) {
		super(string);
	}

	@Override
	public void run() {
		while (true) {
			for (User user : userPool.users) {
				for (Account account : user.accounts) {
					String s = "Not yet connected!";
					try {
						System.out.println("Connecting to " + account.username + "@" + account.hostname);
						Socket socket = new Socket(account.hostname, account.port);
						BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("CP1252")));
						PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("CP1252")), true);
						
						//recive the first massage
						s = in.readLine();
						System.out.println("Connecting " + s);
						
						//Send the username
						out.println("USER " + account.username);
						s = in.readLine();
						System.out.println("USER " + s);
						assert(s.startsWith("+OK"));
						
						//Send the user password
						out.println("PASS " + account.password);
						s = in.readLine();
						System.out.println("PASS " + s);
						assert(s.startsWith("+OK"));
						
						//Get a List of all Emails
						out.println("LIST");
						s = in.readLine();
						System.out.println("LIST " + s);
						assert(s.startsWith("+OK"));
						
						
						//read list until dot
						LinkedList<String> ids = new LinkedList<String>();
						while (true) {
							s = in.readLine();
							System.out.println("LIST " + s);
							if (s.equals(".")) break;
							ids.add(s.split(" ", 2)[0]);
						}
						
						//for every id get the mail
						for (String id : ids) {
							LinkedList<String> mail = new LinkedList<String>();
							out.println("RETR " + id);
							s = in.readLine();
							System.out.println("RETR " + id + " " + s);
							assert(s.startsWith("+OK"));
							
							//Read the entrie mail
							while (true) {
								s = in.readLine();
								System.out.println("RETR " + s);
								if (s.equals(".")) break;
								if (s.startsWith("."))
									mail.add(s.substring(1));
								else
									mail.add(s);
							}
							
							//one user incomming only
							synchronized(user) {
								user.incoming.putSample(join(mail, "\n"));
							}
							out.println("DELE " + id);
							s = in.readLine();
							System.out.println("DELE " + id + " " + s);
							assert(s.startsWith("+OK"));
						}
						out.println("QUIT");
						s = in.readLine();
						System.out.println("QUIT " + s);
						assert(s.startsWith("+OK"));
					//} catch (InterruptedException e) {
					//	return;
					} catch (Throwable t) {
						System.err.println(s);
						t.printStackTrace();
					}
				}
				if (Thread.interrupted()) return;
			}
			if (Thread.interrupted()) return;
			System.out.println("Got all the mails. Now sleeping.");
			
			//wait 30 seconds
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				return;
			}
		}
	}
	
	static String join(Collection<?> s, String delimiter) {
	     StringBuilder builder = new StringBuilder();
	     Iterator<?> iter = s.iterator();
	     while (iter.hasNext()) {
	         builder.append(iter.next());
	         if (!iter.hasNext()) {
	           break;                  
	         }
	         builder.append(delimiter);
	     }
	     return builder.toString();
	 }
}
