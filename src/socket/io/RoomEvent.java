package socket.io;

public interface RoomEvent {
	public void onClientJoinRoom(Client client, String from, String to);
}
