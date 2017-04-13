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
    private EventDataComing _eventDataComing = null;
    private EventDisconnect _eventDisconnect = null;
    private String _path = "/";
    //const
    private final byte ACK = 0b0110;
    private final byte ENQ = 0b0101;
    private final byte ETB = 0b00010111;
    private final byte GET_ID = 0b0;
    private final byte SET_PATH = 0b1;
    //temp
    private byte[] _tmp;
	
	public Client(Server server, Socket socket) throws IOException {
		super();
		this._clientId = count++;
		this._server = server;
		this._socket = socket;
		this._inputStream = new DataInputStream(_socket.getInputStream());
		this._outputStream = new DataOutputStream(_socket.getOutputStream());
	}
	
	public Client(String host, int port) throws IOException, InterruptedException {
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
				int length = _inputStream.readInt();

				if(length > 0) {
				    byte[] buf = new byte[length];
				    _inputStream.readFully(buf);

				    if(_isBlock)
				    {
				    	_tmp = buf.clone();
				    	_isBlock = false;
				    }
				    else if(buf[0] == ACK && buf[1] == ENQ)
				    	pong(buf);
				    else
				    {
					    if(_eventDataComing != null)
					    	_eventDataComing.onDataComing(this, buf);
					    if(_server != null)
					    	_server.dataRecieve(this, buf);
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
			_eventDisconnect.onDisconnect(this);
		if(_server != null)
			_server.clientDisconnect(this);

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
	
	public Client join(String room) throws IOException, InterruptedException {
		room = room.trim();
		if(room.equals("") || room.equals("/")) return this;
		room = room.trim();
		room = room.replaceAll("//", "/");
		if(!room.startsWith("/")) room = "/" + room;
		if(!room.endsWith("/")) room += "/";
		if(room.equals(_path)) return this;
		
		this._path = room;
		
		byte[] pdata = _path.getBytes();
		byte[] header = new byte[] {ACK, ENQ, SET_PATH};
		byte[] data = new byte[pdata.length + 3];
		System.arraycopy(header, 0, data, 0, 3);
		System.arraycopy(pdata, 0, data, 3, pdata.length);
		
		ping(data);		
		return this;
	}
	
	public byte[] ping(byte[] data) throws IOException, InterruptedException {
		_isBlock = true;
		emit(data);
		while(_isBlock) Thread.sleep(10);
		return _tmp;
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
			String from = this._path;
			this._path = path;
			
			emit(new byte[] {ETB, ACK});
			if(_server != null)
				_server.clientJoinRoom(this, from, getPath());
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
	
	public void broadcastEmit(byte[] data) throws IOException {
		if(_server != null)
			_server.emit(this, data);
	}
	
	public void broadcastEmit(String data) throws IOException {
		broadcastEmit(Util.stringToByteArrayWithUtf8(data));
	}
	
	public void broadcastEmit(float... floats) throws IOException {
		broadcastEmit(Util.toByteArray(floats));
	}
	
	public void broadcastEmit(int... integers) throws IOException {
		broadcastEmit(Util.toByteArray(integers));
	}
	
	public void broadcastEmit(long... longs) throws IOException {
		broadcastEmit(Util.toByteArray(longs));
	}
	
	public void broadcastEmit(double... doubles) throws IOException {
		broadcastEmit(Util.toByteArray(doubles));
	}
	
	public void broadcastEmit(boolean b) throws IOException {
		broadcastEmit(Util.toByteArray(b));
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
	
	public void emitAsync(String value) throws IOException {
		emitAsync(Util.stringToByteArrayWithUtf8(value));
	}
	
	public void emitAsync(float... floats) throws IOException {
		emitAsync(Util.toByteArray(floats));
	}
	
	public void emitAsync(int... integers) throws IOException {
		emitAsync(Util.toByteArray(integers));
	}
	
	public void emitAsync(long... longs) throws IOException {
		emitAsync(Util.toByteArray(longs));
	}
	
	public void emitAsync(double... doubles) throws IOException {
		emitAsync(Util.toByteArray(doubles));
	}
	
	public void emitAsync(boolean b) throws IOException {
		emitAsync(Util.toByteArray(b));
	}
	
	public void onDataComing(EventDataComing eventDataComing) {
		this._eventDataComing = eventDataComing;
	}
	
	public void onDisconnect(EventDisconnect eventDisconnect) {
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
