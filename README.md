# JavaSocketIO
### Usage:
1. Server

   Create server listen port 6969
   
	 ```Server listener = new Server(6969);```
   
   Listen  (!important).
   
   `listener.listen();`
   
   handler connection
   ~~~~
    listener.onConnection(new EventConnection() {
			@Override
			public void onConnection(Server sender, Client client) {
				System.out.println("new connection, id: " + client.getClientId());
				
				client.onDisconnect(new EventDisconnect() {
					@Override
					public void onDisconnect(Client s) {
						System.out.println("clientId: " + client.getClientId() + " disconnect.");
					}
				});
			}
		});
    ~~~~
    waiting for data
    ~~~~
		listener.onDataComing(new EventDataComing() {
			
			@Override
			public void onDataComing(Client client, byte[] data) {
				try {
					System.out.println("recieve data: " + Util.byteArrayToString(data, "UTF-8"));
					client.broadcastEmit(data);
				}
				catch(IOException e) {}
			}
		});
    ~~~~~
    
2. Client
   
   Create a client
   
   	`Client client = new Client("localhost", 6969);`
    
    wait data
    ~~~~
		client.onDataComing(new EventDataComing() {
			
			@Override
			public void onDataComing(Client client, byte[] data) {
				try {
					//get data
					System.out.println("Client recieve data " + Util.byteArrayToString(data, "UTF-8"));
				}
				catch(IOException e) {}
			}
		});
    ~~~~
    Send data
    
    `client.emit("Client: test");`
    
    Send Async
    
    `client.emitAsync("Client: test");`
    
    Send data and waiting for response
    
    `byte[] client.ping(byte[] data)`
