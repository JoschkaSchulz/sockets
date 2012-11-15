package client;

public interface IFilter {

	public abstract String pull();

	public abstract String decode(String message);

	public abstract void push(String message);

}