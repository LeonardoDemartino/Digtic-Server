package com.digtic.server;

public class Core {
	public static void main(String argv[]) throws Exception
	{
		Server server=new Server();
		server.start();
		new SocketsManager(server).start();
	}
}	
