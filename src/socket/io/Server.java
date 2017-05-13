package socket.io;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server extends Thread {
	private boolean _isListen = true;
	private EventDataComing _eventDataComing = null;
	private EventConnection _eventConnection = null;
	private RoomEvent _eventClientJoinRoom = null;
	private EventDisconnect _eventDisconnect = null;
	private PongEvent _pong = null;
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
	
	@Override
	public void run() {
		while(_isListen) {
			try {
				Socket socket = _serverSocket.accept();
				Client client = new Client(this, socket);
				_clients.add(client);
				Thread t = new Thread(client);
				if(_eventConnection != null)
					_eventConnection.onConnection(this, client);

				t.start();
			}
			catch(IOException e) {}		
		}
	}
	
	protected void clientDisconnect(Client client) {
		if(_eventDisconnect != null)
			_eventDisconnect.onDisconnect(client);
	}
	
	protected void clientJoinRoom(Client client,  String from, String to) {
		if(_eventClientJoinRoom != null)
			_eventClientJoinRoom.onClientJoinRoom(client, from, to);
	}
	
	protected void dataRecieve(Client client, byte[] data) {
		if(_eventDataComing != null)
			_eventDataComing.onDataComing(client, data);
		
		for(Room room:_rooms)
			if(client.getPath().equals(room.getPath()))
				room.dataRecieve(client, data);
	}
	
	protected void pingReply(Client client, byte[] data) {
		if(_pong != null)
			_pong.reply(client, data);
		else
			try {
				client.emit(0);
			}catch(Exception exc){}
	}
	
	protected void emit(Client except, byte[] data) throws IOException {
		for(Client client:_clients)
			if(client.getClientId() != except.getClientId())// && client.getPath().equals(except.getPath())
				client.emitAsync(data);
	}
	
	public ArrayList<Client> getAllClient() {
		return this._clients;
	}
	
	public void onDataComing(EventDataComing eventDataComing) {
		this._eventDataComing = eventDataComing;
	}
	
	public void onConnection(EventConnection eventConnection) {
		this._eventConnection = eventConnection;
	}
	
	public void onClientJoinRoom(RoomEvent eventClientJoinRoom) {
		this._eventClientJoinRoom = eventClientJoinRoom;
	}
	
	public void onDisconnect(EventDisconnect eventDisconnect) {
		this._eventDisconnect = eventDisconnect;
	}
	
	public void onPing(PongEvent pong) {
		this._pong = pong;
	}
	
	public void emit(byte[] data) throws IOException {
		for(Client client:_clients)
			client.emitAsync(data);
	}
	
	public void emit(String value) throws IOException {
		emit(Util.stringToByteArrayWithUtf8(value));
	}
	
	public void emit(float... floats) throws IOException {
		emit(Util.toByteArray(floats));
	}
	
	public void emit(int... integers) throws IOException {
		emit(Util.toByteArray(integers));
	}
	
	public void emit(long... longs) throws IOException {
		emit(Util.toByteArray(longs));
	}
	
	public void emit(double... doubles) throws IOException {
		emit(Util.toByteArray(doubles));
	}
	
	public void emit(boolean b) throws IOException {
		emit(Util.toByteArray(b));
	}
	
	public void removeClient() {
		for(int i = _clients.size() - 1; i >= 0; i--)
			if(_clients.get(i).isClose())
				_clients.remove(i);
	}
	
	public Room of(String path) {
		for(Room r:_rooms)
			if(r.getPath().equals(Room.to(path)))
				return r;
		//create new room
		Room room = new Room(this, path);
		_rooms.add(room);
		return room;
	}
	
	public Client find(int clientId) {
		for(Client client:_clients)
			if(client.getClientId() == clientId)
				return client;

		return null;
	}
	
	public int getLocalPort() {
		return _serverSocket.getLocalPort();
	}
	
	public void close() throws IOException {
		this.interrupt();
		_rooms.clear();
		_clients.clear();
	}
}