package basic.server;

public interface IPlayer {

	void storMessage(String message);

	boolean isOnline();

	void quit();

}