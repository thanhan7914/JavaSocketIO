package socket.io;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server extends Thread{
	private boolean _isListen = true;
	private ArrayList<Client> _clients;
	private EventHandler<byte[]> _eventDataComming = null;
	private EventHandler<Client> _eventConnection = null;
	private ServerSocket _serverSocket = null;

	public Server(int port) throws IOException {
		_serverSocket = new ServerSocket(port);
		_clients = new ArrayList<Client>();
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
					_eventConnection.handle(this, client);

				t.start();
			}
			catch(IOException e) {}		
		}
	}
	
	public void DataRecieve(Client serviceSocket, byte[] data) {
		if(_eventDataComming != null)
			_eventDataComming.handle(serviceSocket, data);
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
	
	public void emit(Client except, byte[] data) throws IOException {
		for(Client client:_clients)
			if(client.getClientId() != except.getClientId())
				client.emitAsync(data);
	}
	
	public int getLocalPort() {
		return _serverSocket.getLocalPort();
	}
	
	public void removeClient() {
		for(int i = _clients.size() - 1; i >= 0; i--)
			if(_clients.get(i).isClose())
				_clients.remove(i);
	}
}