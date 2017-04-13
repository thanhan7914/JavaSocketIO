package example;

import java.io.IOException;

import socket.io.*;

public class ServerTest2 implements EventConnection, EventDataComing, EventDisconnect, RoomEvent {
	Server _server;
	int _port = 6969;
	
	public ServerTest2 () throws IOException {
		_server = new Server(_port);
		_server.listen();
		System.out.println("Server listen on port " + _server.getLocalPort());
		_server.onConnection(this);
		_server.onDataComing(this);
		_server.onClientJoinRoom(this);
		_server.onDisconnect(this);
	}
	
	public static void main(String[] args) throws IOException {
		new ServerTest2();
	}

	@Override
	public void onClientJoinRoom(Client client, String from, String to) {
		// TODO Auto-generated method stub
		try {
			client.broadcastEmit("client id " + client.getClientId() + " join room " + to);
			
			_server.of(from).emit("client id " + client.getClientId() + " leave room.");
		}
		catch(IOException ioe) {}
	}

	@Override
	public void onDisconnect(Client client) {
		// TODO Auto-generated method stub
		System.out.println("clientId: " + client.getClientId() + " disconnect.");
	}

	@Override
	public void onDataComing(Client client, byte[] data) {
		// TODO Auto-generated method stub
		try {
			client.broadcastEmit(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onConnection(Server server, Client client) {
		// TODO Auto-generated method stub
		System.out.println("new connection, id: " + client.getClientId());
	}

}
