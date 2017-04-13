package example;
import java.io.IOException;
import socket.io.*;

public class MainServer {
	public static void main(String[] args) throws IOException {
		Server listener = new Server(6969);
		
		listener.listen();
		System.out.println("Server listen on port " + listener.getLocalPort());
		listener.onConnection(new EventConnection() {
			@Override
			public void onConnection(Server server, Client client) {
				System.out.println("new connection, id: " + client.getClientId());
				
				client.onDisconnect(new EventDisconnect() {
					@Override
					public void onDisconnect(Client sender) {
						System.out.println("clientId: " + client.getClientId() + " disconnect.");
					}
				});
				
				client.onDataComing(new EventDataComing () {
					@Override
					public void onDataComing(Client sender, byte[] data) {
						System.out.println("clientId: " + client.getClientId() + " recieve data: " + Util.byteArrayToString(data, "UTF-8"));
					}
				});
			}
		});
		
		listener.onDataComing(new EventDataComing() {
			@Override
			public void onDataComing(Client client, byte[] data) {
				try {
					System.out.println("recieve data: " + new String(data, "UTF-8"));
//					listener.emit(data);
					client.broadcastEmit(data);
				}
				catch(IOException e) {}
			}
		});
		
		listener.of("game").onDataComing(new EventDataComing() {
			@Override
			public void onDataComing(Client client, byte[] data) {
				System.out.println("Room game recieve data from clientId: " + client.getClientId() + " - data: " + Util.byteArrayToString(data, "UTF-8"));
			}
		});
		
		listener.onClientJoinRoom(new RoomEvent() {
			@Override
			public void onClientJoinRoom(Client client, String from, String to) {
				try {
					client.broadcastEmit("client id " + client.getClientId() + " join room " + to);
					
					listener.of(from).emit("client id " + client.getClientId() + " leave room.");
				}
				catch(IOException ioe) {}
			}
		});
	}
}
