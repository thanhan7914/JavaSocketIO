package socket.io;

public interface EventHandler<T> {
	public void handle(Object sender, String name, T obj);
}