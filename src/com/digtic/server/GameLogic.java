package com.digtic.server;

import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;


public class GameLogic extends Thread{	
	static long UPDATE_TICK=20;
	//LOGIC
	boolean canUpdate=false;
	int totalPlayers=0;
	
	List<MainObject> objects;
	
	World world;
	
	int controlID=-1;	
	EventsHandler events;

	static byte ACTION_BREAK=0, ACTION_SHOOTSTONE=1, ACTION_ADD=2;
	static byte CUBES_START_POS=2;
	final float GRAVITY=0.1f, MAX_FALLING_SPEED=1f, MAX_FALLING_SPEED_WATER=MAX_FALLING_SPEED/4.0f;
	RandomAccessFile configFile;
	long time=0;
	int selectedDistance=0;
	public int player_latestChunk[]={-1,-1,-1};
	Server server;
		
	public GameLogic(/*Context con, int size_x, int size_y*/Server server)
	{
		this.server=server;
		objects=new ArrayList<MainObject>();
		
		events=new EventsHandler();
		world=new World(this);		
		
		int pos_x=0, pos_z=0;
		int altura=World.WORLD_HEIGHT-2;
		
		pos_x=(int) (Math.random()*50);pos_z=(int) (Math.random()*50);
		addObject(0, new EntityNPCFire(server,0, pos_x,altura,pos_z, this));
		
		
		/*for(int i=0; i<2; i++){			
			pos_x=(int) (Math.random()*50);pos_z=(int) (Math.random()*50);
			addObject(0, new EntityNPCFire(server,0, pos_x,altura,pos_z, this));
			pos_x=(int) (Math.random()*50);pos_z=(int) (Math.random()*50);
			addObject(0, new EntityNPCRock(server,0, pos_x,altura,pos_z, this));
			pos_x=(int) (Math.random()*50);pos_z=(int) (Math.random()*50);
			addObject(0, new EntityNPCWater(server,0, pos_x,altura,pos_z, this));
		}*/
	}
	
	void logic(){
		processEvents();
		if(world!=null)world.update();
		try{
			for(int i=0; world!=null && i<objects.size(); i++){
				MainObject esta=objects.get(i);
				if(esta!=null)esta.update();
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		time=System.currentTimeMillis()/10;
	}

	public void addObject(int id, MainObject n){
		int noRemotePlayers=0;
		for(int i=0; i<objects.size(); i++){
			MainObject thisOne=objects.get(i);
			if(!(thisOne instanceof EntityPlayer))noRemotePlayers++;
		}
		
		n.id=noRemotePlayers+id;
		objects.add(n);		//A LA LOGICA DEL JUEGO
	}
	
	void processEvents(){
		//PROCESS EVENTS
		for(int i=0; i<events.size(); i++){
			SingleEvent cur=events.get(i);
			if(cur!=null && cur.action==EventsHandler.MODIFY_CUBE){
				if(cur.pos[1]>0){	//NO PODER ROMPER ROCA MADRE
					//SETEO EL BLOQUE MODIFICADO
					world.set_getWorldBlock(cur.pos[0],cur.pos[1],cur.pos[2],cur.item);
				}
			}
			events.setToRemove(i);
		}
		events.clear();
	}
	
	long logicPreTime=0, logicStepTime=1000/GameLogic.UPDATE_TICK;
	public void run() {
		while(true){
			long now=System.currentTimeMillis();
			if(now-logicPreTime>=logicStepTime){
				canUpdate=false;
				logic();			

				//System.out.println("Time processing logic: "+(now-logicPreTime)+"ms");
				//server.processRequests();	
				canUpdate=true;
				logicPreTime=now;
			}
			try {
				Thread.sleep(logicStepTime);
			} catch (InterruptedException e) {
			}
		}
	}
}
