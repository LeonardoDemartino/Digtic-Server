package com.digtic.server;

public class ConnectionData {	
	int toPlayer;	
	byte[] dataBytes;
	
	int a,b,c,d,e;
	
	ConnectionData(int toPlayer, byte[] data){
		this.toPlayer=toPlayer;
		dataBytes=data;
	}
}
