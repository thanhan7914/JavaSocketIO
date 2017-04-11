package example;
import java.io.IOException;
import socket.io.*;

public class MainServer {
	public static void main(String[] args) throws IOException {
		Server listener = new Server(6969);
		
		listener.listen();
		System.out.println("Server listen on port " + listener.getLocalPort());
		listener.onConnection(new EventHandler<Client>() {
			@Override
			public void handle(Object sender, String name, Client client) {
				System.out.println("new connection, id: " + client.getClientId());
				
				client.onDisconnect(new EventHandler<byte[]>() {
					@Override
					public void handle(Object sender, String name, byte[] data) {
						System.out.println("clientId: " + client.getClientId() + " disconnect.");
					}
				});
				
				client.onData(new EventHandler<byte[]> () {
					@Override
					public void handle(Object sender, String name, byte[] data) {
						try {
							System.out.println("clientId: " + client.getClientId() + " recieve data: " + new String(data, "UTF-8"));
						}catch(Exception exc){}
					}
				});
			}
		});
		
		listener.onData(new EventHandler<byte[]>() {
			@Override
			public void handle(Object sender, String name, byte[] data) {
				try {
					Client client = (Client)sender;
					System.out.println("recieve data: " + new String(data, "UTF-8"));
//					listener.emit(data);
					client.broadcastEmit(data);
				}
				catch(IOException e) {}
			}
		});
		
		listener.of("game").onData(new EventHandler<byte[]> () {
			@Override
			public void handle(Object sender, String name, byte[] data) {
				try {
					Client client = (Client)sender;
					
					System.out.println("Room game recieve data from clientId: " + client.getClientId() + " - data: " + new String(data, "UTF-8"));
				}catch(Exception exc){}
			}
		});
	}
}
