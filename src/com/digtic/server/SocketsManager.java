package com.digtic.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

public class SocketsManager extends Thread{
	
	Server server;
	String ip_local="", ip_external="";
	SocketsManager(Server server){
		this.server=server;
	}
	
	static String readUrl(String urlString) throws Exception {
	    BufferedReader reader = null;
	    try {
	        URL url = new URL(urlString);
	        reader = new BufferedReader(new InputStreamReader(url.openStream()));
	        StringBuffer buffer = new StringBuffer();
	        int read;
	        char[] chars = new char[1024];
	        while ((read = reader.read(chars)) != -1)
	            buffer.append(chars, 0, read); 

	        return buffer.toString();
	    }
	    catch (Exception e){
	    	System.out.println("Error getting: "+urlString+"     Retrying...");
	    	Thread.sleep(500);
	    	return readUrl(urlString);
	    } finally {
	        if (reader != null)
	            reader.close();
	    }
	}
	
	public void run(){
		try {
			ip_local=Inet4Address.getLocalHost().getHostAddress();
			ip_external=readUrl("http://checkip.amazonaws.com/");
		} catch (Exception e1) {
			e1.printStackTrace();
			return;
		}
		
		System.out.println("Running... Waiting for conection @ LAN "+ip_local+" / INET "+ip_external);
		int portNum = 8888;
		
		try (ServerSocket serverSocket = new ServerSocket(portNum)) { 
            while (true) {
            	if(server.connectionsCount+1<Server.MAX_PLAYERS){
            		Socket new_socket=serverSocket.accept();
            		new_socket.setTcpNoDelay(true);
            		for(int connID=0; connID<Server.MAX_PLAYERS; connID++){
            			if(server.players[connID]==null || !server.players[connID].isAlive()){
            				EntityPlayer newPlayer=new EntityPlayer(server, connID, 32, World.WORLD_HEIGHT-1, 32, server.game);
            				server.game.addObject(connID, newPlayer);
            				server.players[connID]=new ConnectionThread(new_socket, server, newPlayer, server.game);
            				server.players[connID].start();
            				
            				//My creation to other players
            				server.sendToOtherPlayersClose(EventsHandler.PLAYER_DATA, newPlayer.id, newPlayer.getEntityType(), newPlayer.pos[0],newPlayer.pos[1], newPlayer.pos[2],newPlayer.lookAt[0], newPlayer.lookAt[1], newPlayer.lookAt[2], newPlayer.getExtraData(), connID, newPlayer.pos[0], newPlayer.pos[2]);
            				
            				//Others creations to myself
            				for(int playerID=0; playerID<Server.MAX_PLAYERS; playerID++){
            		    		if(server.players[playerID]!=null && server.players[playerID].getSocket()!=null && server.players[playerID].myself.connID!=connID){
            		    			EntityPlayer thisOne=server.players[playerID].myself;
            		    			server.sendToMyselfClose(EventsHandler.PLAYER_DATA, thisOne.id, thisOne.getEntityType(), thisOne.pos[0],thisOne.pos[1], thisOne.pos[2],thisOne.lookAt[0], thisOne.lookAt[1], thisOne.lookAt[2], thisOne.getExtraData(), connID, newPlayer.pos[0], newPlayer.pos[2]);
            		    			server.sendToMyself(EventsHandler.CHAT, "name|"+thisOne.id+"|"+thisOne.networkName, connID);
            		    		}
            		    	}
            				
        		            server.connectionsCount++;
        		            
        		            System.out.println("New player with connID "+connID+" added.");
        		            
        		            break;
            			}
            		} 
            	}
            	else System.out.println("Max players reached: "+Server.MAX_PLAYERS);
            	
            	try{
            		Thread.sleep(10);
            	}
            	catch(Exception e){
            	}
            	
	        }
	    } catch (IOException e) {
            System.err.println("Could not listen on port " + portNum);
            System.exit(-1);
        }
	}

}
