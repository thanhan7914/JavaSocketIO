# JavaSocketIO
### Usage:
1. Server

   Create server listen port 6969
   
	 ```Server listener = new Server(6969);```
   
   Listen:
   
   `listener.listen();`
   
   handler connection
   ~~~~
    listener.onConnection(new EventHandler<Client>() {
			@Override
			public void handle(Object sender, String name, Client client) {
				System.out.println("new connection, id: " + client.getClientId());
				
				client.onDisconnect(new EventHandler<byte[]>() {
					@Override
					public void handle(Object sender, byte[] data) {
						System.out.println("clientId: " + client.getClientId() + " disconnect.");
					}
				});
			}
		});
    ~~~~
    data receive
    ~~~~
		listener.onData(new EventHandler<byte[]>() {
			
			@Override
			public void handle(Object sender, String name, byte[] data) {
				try {
					Client client = (Client)sender;
					System.out.println("recieve data: " + new String(data, "UTF-8"));
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
		client.onData(new EventHandler<byte[]>() {
			
			@Override
			public void handle(Object sender, String name, byte[] data) {
				try {
					//get data
					System.out.println("Client recieve data " + new String(data, "UTF-8"));
				}
				catch(IOException e) {}
			}
		});
    ~~~~
    Send data
    
    `client.emit("Client: ok test");`
    
    Send data and waiting for response
    
    `byte[] client.ping(byte[] data)`
