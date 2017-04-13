package socket.io;

public interface EventDataComing {
	public void onDataComing(Client client, byte[] data);
}
