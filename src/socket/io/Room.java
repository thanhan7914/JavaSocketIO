package socket.io;

import java.io.IOException;
import java.util.ArrayList;

public class Room {
	private EventDataComing _eventDataComing = null;
	private String _path;
	private Server _server;
	
	protected Room(Server server, String path) {
		this._path = Room.to(path);
		this._server = server;
	}
	
	protected static String to(String path) {
		path = path.trim();
		if(path.equals("") || path.equals("/")) return "/";
		path = path.trim();
		path = path.replaceAll("//", "/");
		if(!path.startsWith("/")) path = "/" + path;
		if(!path.endsWith("/")) path += "/";
		return path;
	}
	
	protected void dataRecieve(Client client, byte[] data) {
		if(_eventDataComing != null)
			_eventDataComing.onDataComing(client, data);
	}
	
	public void onDataComing(EventDataComing eventDataComing) {
		this._eventDataComing = eventDataComing;
	}
	
	public String getPath() {
		return this._path;
	}
	
	public boolean has(Client client) {
		return client.getPath().equals(_path);
	}
	
	public ArrayList<Client> getAllClient() {
		ArrayList<Client> clients = new ArrayList<Client>();
		
		for(Client client:_server.getAllClient())
			if(client.getPath().equals(_path))
				clients.add(client);
		
		return clients;
	}
	
	public void emit(byte[] data) throws IOException {
		for(Client client:_server.getAllClient())
			if(client.getPath().equals(_path))
				client.emitAsync(data);
	}
	
	public void broadcastEmit(Client clt, byte[] data) throws IOException {
		for(Client client:_server.getAllClient())
			if(client.getClientId() != clt.getClientId() && client.getPath().equals(_path))
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
	
	public void broadcastEmit(Client client, String data) throws IOException {
		broadcastEmit(client, Util.stringToByteArrayWithUtf8(data));
	}
	
	public void broadcastEmit(Client client, float... floats) throws IOException {
		broadcastEmit(client, Util.toByteArray(floats));
	}
	
	public void broadcastEmit(Client client, int... integers) throws IOException {
		broadcastEmit(client, Util.toByteArray(integers));
	}
	
	public void broadcastEmit(Client client, long... longs) throws IOException {
		broadcastEmit(client, Util.toByteArray(longs));
	}
	
	public void broadcastEmit(Client client, double... doubles) throws IOException {
		broadcastEmit(client, Util.toByteArray(doubles));
	}
	
	public void broadcastEmit(Client client, boolean b) throws IOException {
		broadcastEmit(client, Util.toByteArray(b));
	}
}
