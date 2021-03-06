package example;
import java.io.IOException;
import java.util.Scanner;

import socket.io.*;

public class TestClient {
	
	public static void main(String[] args) throws Exception {
		Client client = new Client("localhost", 6969);
		client.onDataComing(new EventDataComing () {
			
			@Override
			public void onDataComing(Client sender, byte[] data) {
				try {
					//get data
					System.out.println("Client recieve data " + new String(data, "UTF-8"));
				}
				catch(IOException e) {}
			}
		});
		
		System.out.println("connected!");
		client.emit("Console: ok test");
		
		Scanner sc=new Scanner(System.in);
		while(true) {
			System.out.print("Message: ");
			String msg = sc.nextLine();
			
			if(msg.trim().equals("quit")) break;
			client.emit("Console  " + client.getClientId() + ": " + msg);
		}
		
		client.close();
		sc.close();
	}
}
