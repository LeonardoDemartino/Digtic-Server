package com.digtic.server;

import java.net.*;
import java.io.*;

public class ConnectionThread extends Thread {
    private Socket socket = null;
    Server server;
    DataOutputStream out=null;
    DataInputStream in=null;
	boolean connection=true;
	EntityPlayer myself;

	//DISCONECTED REASONS
	static int DISCONNECTED_KICK_HACK=0;
	static int DISCONNECTED_KICK_HANDSHAKE=1;
	static int DISCONNECTED_RESET=2;
	
	String getDisconnectMessage(int id){
		if(id==DISCONNECTED_KICK_HACK)return  "Hacking! moved too fast? :(";
		else if(id==DISCONNECTED_KICK_HANDSHAKE)return  "Not responding to handshake!";
		else if(id==DISCONNECTED_RESET)return  "Client closed the connection!";
		return "**--ERROR ID FOR ERROR NOT FOUND--**";
	}
	
	long handShakeTime=1000*30;
	long handShakeTimeLast=0;
	int handShakeRandomServer=0, handShakeRandomClient=0;
	
    public ConnectionThread(Socket socket, Server server, EntityPlayer yourself, GameLogic game) {
        this.setSocket(socket);
        this.server=server;
        this.myself=yourself;
    }
    
    //////////////////////////////////////////////////////////
    
    int getFirstCommand(String command, String cmdList[], int inputPos){
    	for(int cmd=0; cmd<cmdList.length; cmd++){
    		if(command.startsWith(cmdList[cmd], inputPos))return cmd;
    	}
    	return -1;
    }

    public void run() {
    	try {
    		out = new DataOutputStream(getSocket().getOutputStream());
    		in = new DataInputStream(getSocket().getInputStream());
    		int disconnectedReasonID=-1;
    		
    		System.out.println("Connection for "+myself.id);

    		while(connection){
    			if(!getSocket().isClosed() && !getSocket().isInputShutdown() && !getSocket().isOutputShutdown()){
    				try {
    					int cmd=in.readInt();
    					if(cmd!=-1){
    						if(cmd!=EventsHandler.PLAYER_DATA)
    							System.out.println("Player "+myself.connID+" "+EventsHandler.getName(cmd));
    						if(cmd==EventsHandler.NEW_PLAYER){
    							server.sendToMyself(EventsHandler.NEW_PLAYER, myself.id, server.game.world.timeOfDay, myself.pos[0], myself.pos[1], myself.pos[2], myself.lookAt[0], myself.lookAt[1], myself.lookAt[2], myself.connID);
    						}
    						else if(
    								cmd==EventsHandler.ACTION_FORWARD ||
    								cmd==EventsHandler.ACTION_BACK ||
    								cmd==EventsHandler.ACTION_LEFT ||
    								cmd==EventsHandler.ACTION_RIGHT ||
    								cmd==EventsHandler.ACTION_JUMP ||
    								cmd==EventsHandler.ACTION_CRUNCH
    								){
    							//System.out.println("Recieved: Player "+myself.connID+" "+EventsHandler.getName(cmd));
    							myself.addAction(cmd);
    						}
    						else if(cmd==EventsHandler.ACTION_LOOK){
    							float look[]={in.readFloat(),in.readFloat(),in.readFloat()};
    							//System.out.println("Recieved: Player "+myself.connID+" "+EventsHandler.getName(cmd));
    							myself.addAction(look, EventsHandler.ACTION_LOOK);
    						}
    						else if(cmd==EventsHandler.PLAYER_DATA){
    							float pos[]={in.readFloat(),in.readFloat(),in.readFloat()};
    							float look[]={in.readFloat(),in.readFloat(),in.readFloat()};
    							byte extraData=in.readByte();
    							
    							//System.out.println("Recieved: Player "+myself.connID+" "+EventsHandler.getName(cmd));
    							/*if(
    							FastMath.abs(pos[0]-myself.pos[0])>50
    							||
    							FastMath.abs(pos[1]-myself.pos[1])>50
    							||
    							FastMath.abs(pos[2]-myself.pos[2])>50){
    								disconnectedReasonID=ConnectionThread.DISCONNECTED_KICK_HACK;
    					    		connection=false;
    							}
    							else{*/
    								myself.addAction(pos, EventsHandler.ACTION_POS);
    								myself.addAction(look, EventsHandler.ACTION_LOOK);
    								
    								if(myself.checkExtraData(extraData, EventsHandler.ACTION_CRUNCH))
    									myself.addAction(EventsHandler.ACTION_CRUNCH);
    								else if(myself.checkExtraData(extraData, EventsHandler.ACTION_UNCRUNCH))
    									myself.addAction(EventsHandler.ACTION_UNCRUNCH);
    								
    								if((extraData&0x40)>0)myself.onGround=true;
    								else myself.onGround=false;
    							//}
    						}
    						else if(cmd==EventsHandler.MODIFY_CUBE){
    							int x=in.readInt(), y=in.readInt(), z=in.readInt();
    							short value=in.readShort();
    							server.sendToOtherPlayers(EventsHandler.MODIFY_CUBE, x,y,z,value, myself.connID);
    							server.writeBlock(x,y,z,value);
    							System.out.println("Params: "+x+" "+y+" "+z+" "+value);
    						}
    						else if(cmd==EventsHandler.CHUNK_GET_SERVER){
    							int x=in.readInt(), z=in.readInt();
    							byte compressedChunk[]=CompressionUtils.compress(Core_Utils.short2byte(server.getChunkData(x, z, false).data));
    							//System.out.println("Sending chunk: "+x+" "+z+" for player "+myself.connID+" sending "+compressedChunk.length+" bytes compressed.");
    							server.sendToMyselfClose(EventsHandler.CHUNK_GET_SERVER, x,z,compressedChunk.length,compressedChunk, myself.connID, myself.pos[0], myself.pos[2]);
    						}			
    						else if(cmd==EventsHandler.CHAT){
    							int length=in.readInt();
    							String text="";
    							for(int i=0; i<length; i++){
    								text=""+text+in.readChar();
    							}
    							if(text.startsWith("name|")){
    								myself.networkName=text.substring(5,text.length());
    								server.sendToAllPlayers(EventsHandler.CHAT, "name|"+myself.id+"|"+myself.networkName);
    								System.out.println("Sending new name: "+"name|"+myself.id+"|"+myself.networkName);
    							}
    							else{
    								if(myself.networkName.length()>0)
    									server.sendToAllPlayers(EventsHandler.CHAT, myself.networkName+": "+text);
    								else server.sendToAllPlayers(EventsHandler.CHAT, "Player "+myself.id+": "+text);
    							
    								System.out.println("Chat command: "+text);
    							}
    						}
    						else if(cmd==EventsHandler.HANDSHAKE){
    							handShakeRandomClient=in.readInt();
    						}
    						
    						long now=System.currentTimeMillis();
    						if(now-handShakeTimeLast>handShakeTime){
    							if(handShakeRandomClient!=handShakeRandomServer){
    								disconnectedReasonID=ConnectionThread.DISCONNECTED_KICK_HANDSHAKE;
    								connection=false;
    							}
    							else{
	    							handShakeTimeLast=now;
	    							handShakeRandomServer=(int) (Math.random()*10000);
	    							server.sendToMyself(EventsHandler.HANDSHAKE, handShakeRandomServer, server.game.world.timeOfDay, myself.connID);
	    							
	    							System.out.println("Time of day: "+ server.game.world.timeOfDay);
    							}
    						}
    					}
    				} catch (IOException e) {
    					disconnectedReasonID=ConnectionThread.DISCONNECTED_RESET;
    					connection=false;
    				}
    			}
    			else{
    				disconnectedReasonID=ConnectionThread.DISCONNECTED_RESET;
    				
    				connection=false;
    			}
    		}
    		System.out.println("- CONNECTION TERMINATED FOR PLAYER "+myself.id+": "+getDisconnectMessage(disconnectedReasonID));
    		server.sendToOtherPlayers(EventsHandler.REMOVE_ENTITY, myself.id, myself.connID);

    		if(disconnectedReasonID!=ConnectionThread.DISCONNECTED_RESET){
    			//WRITE DIRECTLY TO THE OUTPUT BEFORE CLOSING IT!
    			int byebye[]={EventsHandler.REMOVE_ENTITY, myself.id};
    			out.write(Core_Utils.int2byte(byebye));
    		}
    		
    		//REMOVING ENTITY
    		server.game.objects.remove(myself);
    		
    		server.connectionsCount--;
    		out.close();
    		in.close();
    		socket.close();
    		out=null;
    		in=null;
    		socket=null;
    	} catch (IOException e) {
    		connection=false;
    		e.printStackTrace();
    	}
    }

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}
}