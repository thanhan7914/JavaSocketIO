package socket.io;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server extends Thread {
	private boolean _isListen = true;
	private EventHandler<byte[]> _eventDataComming = null;
	private EventHandler<Client> _eventConnection = null;
	private ServerSocket _serverSocket = null;
	private ArrayList<Room> _rooms;
	private ArrayList<Client> _clients;

	public Server(int port) throws IOException {
		_serverSocket = new ServerSocket(port);
		_clients = new ArrayList<Client>();
		_rooms = new ArrayList<Room>();
	}
	
	public void listen() {
		start();
	}
	
	public void run() {
		while(_isListen) {
			try {
				Socket socket = _serverSocket.accept();
				Client client = new Client(this, socket);
				_clients.add(client);
				Thread t = new Thread(client);
				if(_eventConnection != null)
					_eventConnection.handle(this, "connection", client);

				t.start();
			}
			catch(IOException e) {}		
		}
	}
	
	protected void DataRecieve(Client client, byte[] data) {
		if(_eventDataComming != null)
			_eventDataComming.handle(client, "data", data);
		
		for(Room room:_rooms)
			if(client.getPath().startsWith(room.getPath()))
				room.DataRecieve(client, data);
	}
	
	//client call emit
	protected void emit(Client except, byte[] data) throws IOException {
		for(Client client:_clients)
			if(client.getClientId() != except.getClientId() && client.getPath().startsWith(except.getPath()))
				client.emitAsync(data);
	}
	
	protected ArrayList<Client> getAllClient() {
		return this._clients;
	}

	public void onConnection(EventHandler<Client> eventConnection) {
		this._eventConnection = eventConnection;
	}
	
	public void onData(EventHandler<byte[]> eventDataComming) {
		this._eventDataComming = eventDataComming;
	}
	
	public void emit(byte[] data) throws IOException {
		for(Client client:_clients)
			client.emitAsync(data);
	}
	
	public void removeClient() {
		for(int i = _clients.size() - 1; i >= 0; i--)
			if(_clients.get(i).isClose())
				_clients.remove(i);
	}
	
	public Room of(String path) {
		Room room = new Room(this, path);
		_rooms.add(room);
		return room;
	}
	
	public int getLocalPort() {
		return _serverSocket.getLocalPort();
	}
}