package socket.io;

public interface EventHandler<T> {
	public void handle(Object sender, T obj);
}