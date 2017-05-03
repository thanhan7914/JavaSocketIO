package socket.io;

public interface PongEvent {
	public void reply(Client client, byte[] data);
}
