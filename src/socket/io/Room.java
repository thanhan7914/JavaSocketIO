package socket.io;

import java.io.IOException;

public class Room {
	private EventHandler<byte[]> _eventDataComming = null;
	private String _path;
	private Server _server;
	
	protected Room(Server server, String path) {
		this._path = to(path);
		this._server = server;
	}
	
	private String to(String path) {
		if(path == "") return "/";
		path = path.trim();
		path = path.replaceAll("//", "/");
		if(!path.startsWith("/")) path = "/" + path;
		if(!path.endsWith("/")) path += "/";
		return path;
	}
	
	protected void DataRecieve(Object sender, byte[] data) {
		if(_eventDataComming != null)
			_eventDataComming.handle(sender, "data", data);
	}
	
	public void onData(EventHandler<byte[]> eventDataComming) {
		this._eventDataComming = eventDataComming;
	}
	
	public String getPath() {
		return this._path;
	}
	
	public boolean has(Client client) {
		return client.getPath().startsWith(_path);
	}
	
	public void emit(byte[] data) throws IOException {
		for(Client client:_server.getAllClient())
			if(client.getPath().startsWith(_path))
				client.emitAsync(data);
	}
}
