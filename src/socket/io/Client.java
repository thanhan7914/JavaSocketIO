package socket.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

public class Client extends Thread {
	private static int count = 0;
	private Server _server = null;
	private int _clientId;
	private Socket _socket;
    private DataInputStream _inputStream;
    private DataOutputStream _outputStream;
    private boolean _waitSend = false;
    private boolean _isAlive = true;
    private boolean _isBlock = false;
    private EventHandler<byte[]> _eventDataComming = null;
    private EventHandler<byte[]> _eventDisconnect = null;
    private String _path = "/";
    //const
    private final byte ACK = 0b0110;
    private final byte ENQ = 0b0101;
    private final byte ETB = 0b00010111;
    private final byte GET_ID = 0b0;
    private final byte SET_PATH = 0b1; 
	
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
		
		byte[] rdata = ping(new byte[] {ACK, ENQ, GET_ID});
		this._clientId = rdata[0] | rdata[1] << 8 | rdata[2] << 16 | rdata[3] << 24;
	}
	
	public void run() {
		while(_isAlive) {
			try {
				while(_isBlock) Thread.sleep(10);
				int length = _inputStream.readInt();

				if(length > 0) {
				    byte[] buf = new byte[length];
				    _inputStream.readFully(buf);
				    if(buf[0] == ACK && buf[1] == ENQ)
				    	pong(buf);
				    else
				    {
					    if(_eventDataComming != null)
					    	_eventDataComming.handle(this, "data", buf);
					    if(_server != null)
					    	_server.DataRecieve(this, buf);
				    }
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
			_eventDisconnect.handle(this, "disconnect", null);

		try {
			close();
		}
		catch(IOException ioe) {}
	}
	
	public int getClientId() {
		return this._clientId;
	}
	
	public String getPath() {
		return this._path;
	}
	
	public Client join(String room) {
		if(room == "") return this;
		room = room.trim();
		room = room.replaceAll("//", "/");
		if(room.startsWith("/")) room = room.substring(1);
		if(!room.endsWith("/")) room += "/";
		
		this._path = this._path + room;
		try {
			byte[] pdata = _path.getBytes();
			byte[] header = new byte[] {ACK, ENQ, SET_PATH};
			byte[] data = new byte[pdata.length + 3];
			System.arraycopy(header, 0, data, 0, 3);
			System.arraycopy(pdata, 0, data, 3, pdata.length);
			
			ping(data);
		}
		catch(IOException ioe) {}
		return this;
	}
	
	public byte[] ping(byte[] data) throws IOException {
		_isBlock = true;
		emit(data);
		
		int length = _inputStream.readInt();

		if(length > 0) {
		    byte[] buf = new byte[length];
		    _inputStream.readFully(buf);
		    _isBlock = false;
		    
		    return buf;
		}
		
		throw new IOException("ping failed.");
	}
	
	private void pong(byte[] data) throws Exception {
		switch(data[2]) {
		case GET_ID:
		{
			byte[] rdata= new byte[4];
			rdata[0] = (byte) (_clientId & 0xff);
			rdata[1] = (byte) ((_clientId << 8) & 0xff);
			rdata[2] = (byte) ((_clientId << 16) & 0xff);
			rdata[3] = (byte) ((_clientId << 24) & 0xff);
			
			emit(rdata);
		}
		break;
		
		case SET_PATH:
		{
			byte[] pdata = Arrays.copyOfRange(data, 3, data.length);
			String path = new String(pdata, "UTF-8");
			this._path = path;
			
			emit(new byte[] {ETB, ACK});
		}
		}
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
