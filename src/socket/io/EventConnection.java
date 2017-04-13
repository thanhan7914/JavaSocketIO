package socket.io;

public interface EventConnection {
	public void onConnection(Server server, Client client);
}
