package pop3.server;

import java.util.ArrayList;

public class User {
	public Connection connection;
	public Maildrop incoming = new Maildrop();
	public Maildrop maildrop = new Maildrop();
	public String name;
	public String password;
	public ArrayList<Account> accounts = new ArrayList<Account>();
}
