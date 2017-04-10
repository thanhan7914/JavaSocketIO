package example;
import java.io.IOException;
import socket.io.*;

public class TestClient {
	
	public static void main(String[] args) throws IOException {
		Client client = new Client("localhost", 6969);
		client.onData(new EventHandler<byte[]>() {
			
			@Override
			public void handle(Object sender, byte[] data) {
				try {
					//get data
					System.out.println("Client recieve data " + new String(data, "UTF-8"));
				}
				catch(IOException e) {}
			}
		});
		
		System.out.println("connected!");
		client.emit(("Client: ok test").getBytes());
	}
}
