package com.digtic.server;

import java.util.ArrayList;
import java.util.List;

public class EventsHandler {
	static int INVALID=-1;
	static int CHUNK_BUILD_VIDEO=1;
	static int CHUNK_BUILD_VIDEO_NOW=2;
	static int MODIFY_CUBE=3;
	///////////////////SERVER COMUNICATION/////////////////////////
	static int ACTION_FORWARD=4;
	static int ACTION_BACK=5;
	static int ACTION_LEFT=6;
	static int ACTION_RIGHT=7;
	static int ACTION_CRUNCH=8;
	static int ACTION_UNCRUNCH=9;
	static int ACTION_JUMP=10;
	static int ACTION_LOOK=11;
	static int CHUNK_GET_SERVER=12;
	static int NEW_PLAYER=13;
	static int PLAYER_DATA=14;
	static int CHUNK_NEW=15;
	static int CHUNK_DELETE=16;
	static int CHAT=17;
	static int ACTION_POS=18;
	static int REMOVE_ENTITY=19;
	static int HANDSHAKE=20;
	
	List<SingleEvent> se=new ArrayList<SingleEvent>();
	
	public void add(int action){
		se.add(new SingleEvent(-1,-1,-1,-1,action, (byte) -1));
	}
	
	public void add(int x, int y, int z, int chunkIndex, short item, int action){
		se.add(new SingleEvent(x,y,z,chunkIndex,action, item));
	}
	
	public void add(float info[], int action){
		se.add(new SingleEvent(info, action));
	}
	
	public int size(){
		return se.size();
	}
	
	public void setToRemove(int elem){
		if(elem>=0 && elem<se.size()){
			SingleEvent cur=se.get(elem);
			if(cur!=null)cur.remove=true;
		}
	}
	
	public void clear(){
		int total=se.size();
		for(int i=0; i<total; i++){
			SingleEvent cur=se.get(i);
			if(cur!=null && cur.remove){
				se.remove(i);
				i--;
				total--;
			}
			if(total<=0)break;
		}
	}
	
	public SingleEvent get(int elem){
		return se.get(elem);
	}

	public static String getName(int cmd) {
		if(cmd==INVALID)return "INVALID";
		else if(cmd==CHUNK_BUILD_VIDEO)return "CHUNK_BUILD_VIDEO";
		else if(cmd==CHUNK_BUILD_VIDEO_NOW)return "CHUNK_BUILD_VIDEO_NOW";
		else if(cmd==MODIFY_CUBE)return "MODIFY_CUBE";
		else if(cmd==ACTION_FORWARD)return "ACTION_FORWARD";
		else if(cmd==ACTION_BACK)return "ACTION_BACK";
		else if(cmd==ACTION_LEFT)return "ACTION_LEFT";
		else if(cmd==ACTION_RIGHT)return "ACTION_RIGHT";
		else if(cmd==ACTION_CRUNCH)return "ACTION_CRUNCH";
		else if(cmd==ACTION_UNCRUNCH)return "ACTION_UNCRUNCH";
		else if(cmd==ACTION_JUMP)return "ACTION_JUMP";
		else if(cmd==ACTION_LOOK)return "ACTION_LOOK";
		else if(cmd==CHUNK_GET_SERVER)return "CHUNK_GET_SERVER";
		else if(cmd==NEW_PLAYER)return "NEW_PLAYER";
		else if(cmd==PLAYER_DATA)return "PLAYER_DATA";
		else if(cmd==CHAT)return "CHAT";
		else if(cmd==REMOVE_ENTITY)return "REMOVE_ENTITY";
		else if(cmd==HANDSHAKE)return "HANDSHAKE";
		else return "COMMAND NOT FOUND: "+cmd;
	}
}