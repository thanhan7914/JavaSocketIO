package socket.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Client extends Thread {
	private static int count = 0;
	private Server _server = null;
	private int _clientId;
	private Socket _socket;
    private DataInputStream _inputStream;
    private DataOutputStream _outputStream;
    private boolean _waitSend = false;
    private boolean _isAlive = true;
    private EventHandler<byte[]> _eventDataComming = null;
    private EventHandler<byte[]> _eventDisconnect = null;
	
	public Client(Server server, Socket socket) throws IOException {
		super();
		this._clientId = count++;
		this._server = server;
		this._socket = socket;
		this._inputStream = new DataInputStream(_socket.getInputStream());
		this._outputStream = new DataOutputStream(_socket.getOutputStream());
	}
	
	public Client(String host, int port) throws IOException {
		this._clientId = count++;
		this._socket = new Socket(host, port);
		this._inputStream = new DataInputStream(_socket.getInputStream());
		this._outputStream = new DataOutputStream(_socket.getOutputStream());
		this.start();
	}
	
	public void run() {
		while(_isAlive) {
			try {
				int length = _inputStream.readInt();

				if(length > 0) {
				    byte[] buf = new byte[length];
				    _inputStream.readFully(buf);
				    if(_eventDataComming != null)
				    	_eventDataComming.handle(this, buf);
				    if(_server != null)
				    	_server.DataRecieve(this, buf);
				}
			}
			catch(IOException ioe) {
				break;
			}
			catch(Exception exc) {
				break;
			}
		}
		
		if(_eventDisconnect != null)
			_eventDisconnect.handle(this, null);

		try {
			close();
		}
		catch(IOException ioe) {}
	}
	
	public int getClientId() {
		return this._clientId;
	}
	
	public void emit(byte[] data) throws IOException {
		_waitSend = true;
		_outputStream.writeInt(data.length);
		_outputStream.write(data);
		_outputStream.flush();
		_waitSend = false;
	}
	
	public void emit(String data) throws IOException {
		emit(data.getBytes("UTF-8"));
	}
	
	public void broadcastEmit(byte[] data) throws IOException {
		if(_server != null)
			_server.emit(this, data);
	}
	
	public void broadcastEmit(String data) throws IOException {
		if(_server != null)
			_server.emit(this, data.getBytes("UTF-8"));
	}
	
	public void emitAsync(byte[] data) {
		(new Thread() {
			public void run() {
				try {
					while(_waitSend);
					emit(data);
				}
				catch(Exception e) {}
			}
		}).start();
	}
	
	public void onData(EventHandler<byte[]> eventDataComming) {
		this._eventDataComming = eventDataComming;
	}
	
	public void onDisconnect(EventHandler<byte[]> eventDisconnect) {
		this._eventDisconnect = eventDisconnect;
	}
	
	public void close() throws IOException {
		this._isAlive = false;
		this.interrupt();
		_inputStream.close();
		_outputStream.close();
		_socket.close();
		if(_server != null)
			_server.removeClient();
	}
	
	public boolean isClose() {
		return !_isAlive;
	}
}
