package pop3.server;

import java.util.ArrayList;

public class UserPool {
	public ArrayList<User> users = new ArrayList<User>();
	public User getUser(String name) {
		for (User user : users)
			if (user.name.equals(name))
				return user;
		return null;
	}
	public void makeUser(String name, String password) {
		User user = new User();
		user.name = name;
		user.password = password;
		users.add(user);
	}
	public UserPool() {
		makeUser("admin", "123456");
//		makeUser("user", "password");
//		makeUser("postmaster", "postmaster");
//		makeUser("root", "111111");
		users.get(0).accounts.add(new Account("localhost", 110, "joschka", "joschka"));
		users.get(0).incoming.putText("Hello World");
		users.get(0).incoming.putText("I am a very interesting mail. Read me!");
		users.get(0).incoming.putText("Funny stuff to be seen here.");
		users.get(0).incoming.putText("Buy Viagra online!! Enter your credit card information below.");
	}
}
