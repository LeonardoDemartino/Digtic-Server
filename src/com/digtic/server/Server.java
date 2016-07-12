package com.digtic.server;


import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread{
	//MAP
	static int MAX_PLAYERS=16;
	int totalChunkCubes=World.CHUNK_SIZE*World.WORLD_HEIGHT*World.CHUNK_SIZE;	
	WorldCreator creator;
	boolean log=true;
	int connectionsCount=0;
	String path="C:/Digtic";
	String worldPath=path+"/World";
	
	long lastTimeRAMChunksSaved=0;
	long timeBetweenRAMChunksSave=1000*30;
	int maxCountRAMSavingPerUpdate=10;
	
	//Opened chunk files buffer
	class chunkData{
		int x, z;
		short data[];
		boolean modified;
		chunkData(int x, int z, short[] data){
			this.x=x;
			this.z=z;
			modified=false;
			this.data=data;
		}
	}	

	List<chunkData> chunkFiles=new ArrayList<chunkData>();

	RandomAccessFile getChunkFromDisk(int chunk_x, int chunk_z){
		try{
			RandomAccessFile chunkFile=null;
			File dir=new File(worldPath);
			File f=new File(dir, "chunk_"+chunk_x+"_"+chunk_z+".mcd");

			//If it does not exit, create it and populate it.
			if(!f.exists()){
				System.out.println("Creating chunk file for "+chunk_x+"_"+chunk_z);
				f.createNewFile();
				chunkFile = new RandomAccessFile(f, "rw");
				WorldCreator.populate(chunk_x, chunk_z, chunkFile);
				System.out.println("New chunk "+chunk_x+"_"+chunk_z+" populated");
			}
			//If it exist, get it
			else chunkFile = new RandomAccessFile(f, "rw");
			
			return chunkFile;
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	void saveChunkDataToDisk(chunkData chunk){
		try {
			RandomAccessFile chunkFileWrite=getChunkFromDisk(chunk.x, chunk.z);
			chunkFileWrite.seek(0);
			for(int i=0; i<chunk.data.length; i++)
				chunkFileWrite.writeShort(chunk.data[i]);
			chunkFileWrite.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//Returns a chunk object, from buffer memory or disk
	int max_chunks_files_memory=10;
	chunkData getChunkData(int chunk_x, int chunk_z, boolean toWrite){
		//System.out.println("New chunk requested. File chunks in buffer "+chunkFiles.size());
		for(int i=0; i<chunkFiles.size(); i++){
			chunkData f=chunkFiles.get(i);
			if(f!=null && f.x==chunk_x && f.z==chunk_z){
				if(toWrite)f.modified=true;
				return f;
			}
		}
		//NOT IN MEMORY, LOAD IT FROM DISK
		try{
			//OPEN FILE
			RandomAccessFile chunkFile=getChunkFromDisk(chunk_x, chunk_z);
			
			short data[]=new short[totalChunkCubes];
			
			try {
				chunkFile.seek(0);
				for(int i=0; i<totalChunkCubes; i++)
					data[i]=chunkFile.readShort();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			
			chunkData newOne=new chunkData(chunk_x, chunk_z, data);
			if(toWrite)newOne.modified=true;

			chunkFiles.add(newOne);
			if(chunkFiles.size()>max_chunks_files_memory*game.objects.size()){
				chunkData toDelete=chunkFiles.get(0);
				
				///////////////////////////////
				if(toDelete.modified)saveChunkDataToDisk(toDelete);
				
				chunkFiles.remove(0);
				toDelete=null;
			}
			return getChunkData(chunk_x, chunk_z, toWrite);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	GameLogic game;
	ConnectionThread players[]=new ConnectionThread[MAX_PLAYERS];
	List<ConnectionData> dataList = new ArrayList<ConnectionData>();
	
	Server(){
		if(log)System.out.println("Digtic Server v0.4");
		new File(path).mkdirs();
		new File(worldPath).mkdirs();
    	game=new GameLogic(this);
    	game.start();
	}
		
	public int lowestNatural(int[] array) {	  
		boolean allNegatives=true;
		for (int i=0; i <array.length; i++){
			if(array[i]<0)array[i]=99999999;
			else allNegatives=false;
		}
		if(allNegatives)return -1;
		
		int min = array[0];
		for (int i = 1; i < array.length; i++) {
			if (array[i] < min) {
				min = array[i];
			}
		}
		if(min==99999999)min=-1;
		return min;
	}

	void writeBlock(int x, int y, int z,short val){
		int chunk[]={x/World.CHUNK_SIZE, z/World.CHUNK_SIZE};	
		if(x<0)chunk[0]-=1;
		if(z<0)chunk[1]-=1;
		System.out.println("Writing block "+x+" "+y+" "+z+" "+val);
		getChunkData(chunk[0], chunk[1], true).data[((FastMath.rem(x, World.CHUNK_SIZE))*World.CHUNK_SIZE*World.WORLD_HEIGHT)
					+   ((FastMath.rem(z, World.CHUNK_SIZE)*World.WORLD_HEIGHT))
					+  y]=val;
	}
	
	short readBlock(int x, int y, int z){
		int chunk[]={x/World.CHUNK_SIZE, z/World.CHUNK_SIZE};	
		if(x<0)chunk[0]-=1;
		if(z<0)chunk[1]-=1;
		//System.out.println("Reading block "+x+" "+y+" "+z);
		return getChunkData(chunk[0], chunk[1], false).data[((FastMath.rem(x, World.CHUNK_SIZE))*World.CHUNK_SIZE*World.WORLD_HEIGHT)
					+   ((FastMath.rem(z, World.CHUNK_SIZE)*World.WORLD_HEIGHT))
					+  y];
	}
	
	void processRequests(){
    	//SEND!!!
    	for(int i = 0; i < dataList.size(); i++) {
    		ConnectionData thisData=dataList.get(i);
            if(thisData!=null){
            	try{
            		synchronized(players[thisData.toPlayer].out){
            			if(players[thisData.toPlayer].out!=null && thisData.dataBytes!=null){
			            	players[thisData.toPlayer].out.write(thisData.dataBytes);
			            	//System.out.println(""+Core_Utils.bytesToHex(thisData.dataBytes));
	            		}
		            	thisData.dataBytes=null;
            		}
            	}
            	catch(Exception e){
            		//e.printStackTrace();
            	}
            }
        }
    	////////////////CLEAR/////////
    	dataList.removeAll(dataList);
    	//////////////////////////////
    	
    	//Chunks in RAM with modifications to disk every 30 seconds
    	long now=System.currentTimeMillis();
    	if(now-lastTimeRAMChunksSaved>timeBetweenRAMChunksSave){
    		lastTimeRAMChunksSaved=now;
    		
    		int savedSoFar=0;
	    	for(int i=0; i<chunkFiles.size(); i++){
				chunkData thisOne=chunkFiles.get(i);
				
				if(thisOne!=null && thisOne.modified){
					saveChunkDataToDisk(thisOne);
					thisOne.modified=false;
					savedSoFar++;
					
				}
				
				//Update limit to prevent heavy load (remaining ones will be saved on next update!)
				if(savedSoFar>=maxCountRAMSavingPerUpdate)break;
			}
	    	
	    	if(savedSoFar>0)System.out.println("---- Updates in RAM saver "+savedSoFar+" chunks to disk.");
	    	//else System.out.println("---- No updates in RAM in the latest "+(timeBetweenRAMChunksSave/1000)+" seconds.");
    	}
    }
	
	void sendToMyself(int a, int b, int c, float d, float e, float f, float g, float h, float i, int me){
    	int dataOI[]={a,b,c};
    	float dataOF[]={d,e,f,g,h,i};
    	
		byte data1[]=Core_Utils.int2byte(dataOI);
		byte data2[]=Core_Utils.float2byte(dataOF);
		byte data[]=new byte[data1.length+data2.length];
		System.arraycopy(data1, 0, data, 0, 							data1.length);
		System.arraycopy(data2, 0, data, data1.length, 					data2.length);

    	for(int playerID=0; playerID<MAX_PLAYERS; playerID++){
    		if(players[playerID]!=null && players[playerID].getSocket()!=null && players[playerID].myself.connID==me)
    			dataList.add(new ConnectionData(players[playerID].myself.connID, data));
    	}
    }
	
	void sendToMyself(int a, int b, int c, int d, byte e[], int me){
		int dataO[]={a,b,c,d};
		byte data[]=Core_Utils.int2byte(dataO);
		
		for(int i=0; i<MAX_PLAYERS; i++){
    		if(players[i]!=null && players[i].getSocket()!=null && players[i].myself.connID==me){
    			dataList.add(new ConnectionData(players[i].myself.connID,data));
    			dataList.add(new ConnectionData(players[i].myself.connID,e));
    		}
    	}
	}
	
	void sendToMyself(int a, int b, int c, int d, int e, int me){
		int dataO[]={a,b,c,d,e};
		byte data[]=Core_Utils.int2byte(dataO);
		
		for(int i=0; i<MAX_PLAYERS; i++){
    		if(players[i]!=null && players[i].getSocket()!=null && players[i].myself.connID==me)
    			dataList.add(new ConnectionData(players[i].myself.connID,data));
    	}
	}
	
	void sendToMyself(int a, int b, int c, int me){
		int dataO[]={a,b,c};
		byte data[]=Core_Utils.int2byte(dataO);
		
		for(int i=0; i<MAX_PLAYERS; i++){
    		if(players[i]!=null && players[i].getSocket()!=null && players[i].myself.connID==me)
    			dataList.add(new ConnectionData(players[i].myself.connID,data));
    	}
	}
	
	void sendToMyself(int a, int b, int c, float d, float e, float f, float g, float h, float i, byte j, int me){
    	int dataOI[]={a,b,c};
    	float dataOF[]={d,e,f,g,h,i};
    	
		byte data1[]=Core_Utils.int2byte(dataOI);
		byte data2[]=Core_Utils.float2byte(dataOF);
		byte data3[]={j};
		byte data[]=new byte[data1.length+data2.length+data3.length];
		System.arraycopy(data1, 0, data, 0, 							data1.length);
		System.arraycopy(data2, 0, data, data1.length, 					data2.length);
		System.arraycopy(data3, 0, data, data1.length+data2.length, 	data3.length);

    	for(int playerID=0; playerID<MAX_PLAYERS; playerID++){
    		if(players[playerID]!=null && players[playerID].getSocket()!=null && players[playerID].myself.connID==me)
    			dataList.add(new ConnectionData(players[playerID].myself.connID, data));
    	}
    }
	
	void sendToMyself(int a, String b, int me){
		try {
			int dataOI[]={a, b.length()};

			byte data1[]=Core_Utils.int2byte(dataOI);
			byte data2[]=b.getBytes("UTF-16BE");

			byte data[]=new byte[data1.length+data2.length];

			System.arraycopy(data1, 0, data, 0, 							data1.length);
			System.arraycopy(data2, 0, data, data1.length, 					data2.length);

			for(int playerID=0; playerID<MAX_PLAYERS; playerID++){
				if(players[playerID]!=null && players[playerID].getSocket()!=null && players[playerID].myself.connID==me)
					dataList.add(new ConnectionData(players[playerID].myself.connID, data));
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    }
	
	void sendToOtherPlayers(int a, int b, int c, float d, float e, float f, float g, float h, float i, byte j, int me){
    	int dataOI[]={a,b,c};
    	float dataOF[]={d,e,f,g,h,i};
    	
		byte data1[]=Core_Utils.int2byte(dataOI);
		byte data2[]=Core_Utils.float2byte(dataOF);
		byte data3[]={j};
		
		byte data[]=new byte[data1.length+data2.length+data3.length];
		System.arraycopy(data1, 0, data, 0, 							data1.length);
		System.arraycopy(data2, 0, data, data1.length, 					data2.length);
		System.arraycopy(data3, 0, data, data1.length+data2.length, 	data3.length);

    	for(int playerID=0; playerID<MAX_PLAYERS; playerID++){
    		if(players[playerID]!=null && players[playerID].getSocket()!=null && players[playerID].myself.connID!=me)
    			dataList.add(new ConnectionData(players[playerID].myself.connID, data));
    	}
    }
	
	void sendToOtherPlayers(int a, int b, int c, int d, short e, int me){
		int dataO[]={a,b,c,d};
		short dataO2[]={e};
		
		byte data1[]=Core_Utils.int2byte(dataO);
		byte data2[]=Core_Utils.short2byte(dataO2);
		byte data[]=new byte[data1.length+data2.length];
		
		System.arraycopy(data1, 0, data, 0, 							data1.length);
		System.arraycopy(data2, 0, data, data1.length, 					data2.length);
		
    	for(int i=0; i<MAX_PLAYERS; i++){
    		if(players[i]!=null && players[i].getSocket()!=null && players[i].myself.connID!=me)
    			dataList.add(new ConnectionData(players[i].myself.connID, data));
    	}
    }
	
	void sendToOtherPlayers(int a, int b, int me){
		int dataO[]={a,b};
		byte data[]=Core_Utils.int2byte(dataO);
		
    	for(int i=0; i<MAX_PLAYERS; i++){
    		if(players[i]!=null && players[i].getSocket()!=null && players[i].myself.connID!=me)
    			dataList.add(new ConnectionData(players[i].myself.connID, data));
    	}
    }
	
	/*void sendToOtherPlayersa(int a, int b, float c, float d, float e, int me){
    	int dataOI[]={a,b};
    	float dataOF[]={c,d,e};
    	
		byte data1[]=Core_Utils.int2byte(dataOI);
		byte data2[]=Core_Utils.float2byte(dataOF);
		byte data[]=new byte[data1.length+data2.length];
		System.arraycopy(data1, 0, data, 0, 							data1.length);
		System.arraycopy(data2, 0, data, data1.length, 					data2.length);

    	for(int playerID=0; playerID<MAX_PLAYERS; playerID++){
    		if(players[playerID]!=null && players[playerID].getSocket()!=null && players[playerID].myself.connID!=me)
    			dataList.add(new ConnectionData(players[playerID].myself.connID, data));
    	}
    }*/
	
	void sendToAllPlayers(int a, int b){
		int dataO[]={a,b};
		byte data[]=Core_Utils.int2byte(dataO);
		
    	for(int i=0; i<MAX_PLAYERS; i++){
    		if(players[i]!=null && players[i].getSocket()!=null)
    			dataList.add(new ConnectionData(players[i].myself.connID, data));
    	}
    }
	
	void sendToAllPlayers(int a, int b, float c, float d, float e){
    	int dataOI[]={a,b};
    	float dataOF[]={c,d,e};
    	
		byte data1[]=Core_Utils.int2byte(dataOI);
		byte data2[]=Core_Utils.float2byte(dataOF);
		byte data[]=new byte[data1.length+data2.length];
		System.arraycopy(data1, 0, data, 0, 							data1.length);
		System.arraycopy(data2, 0, data, data1.length, 					data2.length);

    	for(int playerID=0; playerID<MAX_PLAYERS; playerID++){
    		if(players[playerID]!=null && players[playerID].getSocket()!=null)
    			dataList.add(new ConnectionData(players[playerID].myself.connID, data));
    	}
    }
	
	void sendToAllPlayers(int a, String b){
		try {
			int dataOI[]={a, b.length()};

			byte data1[]=Core_Utils.int2byte(dataOI);
			byte data2[]=b.getBytes("UTF-16BE");

			byte data[]=new byte[data1.length+data2.length];

			System.arraycopy(data1, 0, data, 0, 							data1.length);
			System.arraycopy(data2, 0, data, data1.length, 					data2.length);

			for(int playerID=0; playerID<MAX_PLAYERS; playerID++){
				if(players[playerID]!=null && players[playerID].getSocket()!=null)
					dataList.add(new ConnectionData(players[playerID].myself.connID, data));
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    }
	
    void sendToAllPlayers(int a, int b, int c, float d, float e, float f, float g, float h, float i, byte j){
    	int dataOI[]={a,b,c};
    	float dataOF[]={d,e,f,g,h,i};

		byte data1[]=Core_Utils.int2byte(dataOI);
		byte data2[]=Core_Utils.float2byte(dataOF);
		byte data3[]={j};
		
		byte data[]=new byte[data1.length+data2.length+data3.length];
		System.arraycopy(data1, 0, data, 0, 							data1.length);
		System.arraycopy(data2, 0, data, data1.length, 					data2.length);
		System.arraycopy(data3, 0, data, data1.length+data2.length, 	data3.length);
		
    	for(int playerID=0; playerID<MAX_PLAYERS; playerID++){
    		if(players[playerID]!=null && players[playerID].getSocket()!=null){
    			dataList.add(new ConnectionData(players[playerID].myself.connID, data));
    		}
    	}
    }
    
    
    /////CLOSE FUNCTIONS!
    int closeDistance=World.CHUNK_SIZE*4;
    
    void sendToAllPlayersClose(int a, int b, int c, float d, float e, float f, float g, float h, float i, byte j, float pos_x, float pos_z){
    	int dataOI[]={a,b,c};
    	float dataOF[]={d,e,f,g,h,i};

		byte data1[]=Core_Utils.int2byte(dataOI);
		byte data2[]=Core_Utils.float2byte(dataOF);
		byte data3[]={j};
		
		byte data[]=new byte[data1.length+data2.length+data3.length];
		System.arraycopy(data1, 0, data, 0, 							data1.length);
		System.arraycopy(data2, 0, data, data1.length, 					data2.length);
		System.arraycopy(data3, 0, data, data1.length+data2.length, 	data3.length);
		
    	for(int playerID=0; playerID<MAX_PLAYERS; playerID++){
    		if(players[playerID]!=null && players[playerID].getSocket()!=null){
    			if(FastMath.abs(players[playerID].myself.pos[0]-pos_x)<=closeDistance && FastMath.abs(players[playerID].myself.pos[2]-pos_z)<=closeDistance)
    				dataList.add(new ConnectionData(players[playerID].myself.connID, data));
    		}
    	}
    }
    
    void sendToMyselfClose(int a, int b, int c, int d, byte e[], int me, float pos_x, float pos_z){
		int dataO[]={a,b,c,d};
		byte data[]=Core_Utils.int2byte(dataO);
		
		for(int i=0; i<MAX_PLAYERS; i++){
    		if(players[i]!=null && players[i].getSocket()!=null && players[i].myself.connID==me){
    			if(FastMath.abs(players[i].myself.pos[0]-pos_x)<=closeDistance && FastMath.abs(players[i].myself.pos[2]-pos_z)<=closeDistance){
	    			dataList.add(new ConnectionData(players[i].myself.connID,data));
	    			dataList.add(new ConnectionData(players[i].myself.connID,e));
	    		}
    		}
    	}
	}
    
    void sendToMyselfClose(int a, int b, int c, float d, float e, float f, float g, float h, float i, byte j, int me, float pos_x, float pos_z){
    	int dataOI[]={a,b,c};
    	float dataOF[]={d,e,f,g,h,i};
    	
		byte data1[]=Core_Utils.int2byte(dataOI);
		byte data2[]=Core_Utils.float2byte(dataOF);
		byte data3[]={j};
		byte data[]=new byte[data1.length+data2.length+data3.length];
		System.arraycopy(data1, 0, data, 0, 							data1.length);
		System.arraycopy(data2, 0, data, data1.length, 					data2.length);
		System.arraycopy(data3, 0, data, data1.length+data2.length, 	data3.length);

    	for(int playerID=0; playerID<MAX_PLAYERS; playerID++){
    		if(players[playerID]!=null && players[playerID].getSocket()!=null && players[playerID].myself.connID==me)
    			if(FastMath.abs(players[playerID].myself.pos[0]-pos_x)<=closeDistance && FastMath.abs(players[playerID].myself.pos[2]-pos_z)<=closeDistance)
    				dataList.add(new ConnectionData(players[playerID].myself.connID, data));
    	}
    }
    
    void sendToOtherPlayersClose(int a, int b, int c, float d, float e, float f, float g, float h, float i, byte j, int me, float pos_x, float pos_z){
    	int dataOI[]={a,b,c};
    	float dataOF[]={d,e,f,g,h,i};
    	
		byte data1[]=Core_Utils.int2byte(dataOI);
		byte data2[]=Core_Utils.float2byte(dataOF);
		byte data3[]={j};
		
		byte data[]=new byte[data1.length+data2.length+data3.length];
		System.arraycopy(data1, 0, data, 0, 							data1.length);
		System.arraycopy(data2, 0, data, data1.length, 					data2.length);
		System.arraycopy(data3, 0, data, data1.length+data2.length, 	data3.length);

    	for(int playerID=0; playerID<MAX_PLAYERS; playerID++){
    		if(players[playerID]!=null && players[playerID].getSocket()!=null && players[playerID].myself.connID!=me)
    			if(FastMath.abs(players[playerID].myself.pos[0]-pos_x)<=closeDistance && FastMath.abs(players[playerID].myself.pos[2]-pos_z)<=closeDistance)
    				dataList.add(new ConnectionData(players[playerID].myself.connID, data));
    	}
    }
    
    long logicPreTime=0, logicStepTime=1;
	public void run() {
		while(true){
			long now=System.currentTimeMillis();
			if(now-logicPreTime>=logicStepTime){				
				processRequests();
				//System.out.println("Server response: "+(now-logicPreTime)+"ms");
				logicPreTime=now;
			}
			try {
				Thread.sleep(logicStepTime);
			} catch (InterruptedException e) {
			}
		}
	}
}