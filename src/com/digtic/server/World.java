package com.digtic.server;

import java.io.IOException;
import java.io.RandomAccessFile;

import com.digtic.server.World;

public class World {
	static int WORLD_HEIGHT=32;
	static int CHUNK_SIZE=32;
	static int DRAW_DISTANCE[]={4, 6, 8, 10, 12, 14};
	static int CHUNK_PER_AXIS_H=DRAW_DISTANCE[0];
	static int totalTexturesInAtlas=256;
	GameLogic game;
	/*public LoaderChunk[] chunks;
	WorldVisibilityMap worldVisibility;*/
	int textureIndex;
	int timeOfDay=0, maxTimeOfDay=10000;
	
	class blockDef{
		int frontFace, leftFace, rightFace, backFace, bottomFace, upFace;
		
		blockDef(){
			frontFace=0;
			leftFace=0;
			rightFace=0;
			backFace=0;
			bottomFace=0;
			upFace=0;
		}
		
		void setFaces(int face){
			setFaces(face, face, face, face, face, face);
		}
		
		void setFaces(int up, int bottom, int left, int right, int front, int back){
			leftFace=left;
			rightFace= right;
			frontFace= front;
			backFace= back;
			upFace= up;
			bottomFace= bottom;
		}
	}
		
	////INDEXES VERSION FOR OPTIMIZATION DURING CHUNK CREATION ///////////////
/*	boolean isTraspasable(int chunk_index, int x, int y, int z){
		short type=set_getWorldBlock(chunk_index, x,y,z,World.BLOCK_NULL);
		short id=(short)(type&0xFF);	//ONLY THE ID PART
		short extraData=(short)(type>>8);;
		short rotation=(short) (extraData&0x03);
		boolean noCube=((extraData&0x0C)>0);
		
		if(id==World.BLOCK_AIR)return true;
		else if(id==World.BLOCK_WATER)return true;
		else if(id==World.BLOCK_SPIDER_WEB)return true;
		else if(id==World.BLOCK_PLANT_BLUE)return true;
		return false;
	}
	
	boolean isTransparentForRender(int chunk_index, int x, int y, int z){
		short type=set_getWorldBlock(chunk_index, x,y,z,World.BLOCK_NULL);
		short id=(short)(type&0xFF);	//ONLY THE ID PART
		short extraData=(short)(type>>8);;
		short rotation=(short) (extraData&0x03);
		boolean noCube=((extraData&0x0C)>0);
		
		if(id==World.BLOCK_AIR)return true;
		else if(id==World.BLOCK_WATER)return true;
		return false;
	}
	
	boolean isTransparent(int chunk_index, int x, int y, int z){		
		short type=set_getWorldBlock(chunk_index, x,y,z,World.BLOCK_NULL);
		short id=(short)(type&0xFF);	//ONLY THE ID PART
		short extraData=(short)(type>>8);;
		short rotation=(short) (extraData&0x03);
		boolean noCube=((extraData&0x0C)>0);
		
		if(id==World.BLOCK_AIR)return true;
		else if(id==World.BLOCK_WATER)return true;
		else if(id==World.BLOCK_GLASS)return true;
		else if(id==World.BLOCK_LEAFS)return true;
		else if(id==World.BLOCK_SPIDER_WEB)return true;
		else if(id==World.BLOCK_PLANT_BLUE)return true;
		
		//TODO: ESTO ESTA MAL Y VA A HACER QUE TODO SE MUEVA LENTO EN UN FUTURO
		else if(noCube)return true;		
		return false;
	}*/
	
	////////NORMAL////////////////////////
	
	boolean isEditable(int x, int y, int z){
		short type=set_getWorldBlock(x,y,z,World.BLOCK_NULL);
		short id=(short)(type&0xFF);	//ONLY THE ID PART
		short extraData=(short)(type>>8);;
		short rotation=(short) (extraData&0x03);
		boolean noCube=((extraData&0x0C)>0);
		
		if(id==World.BLOCK_AIR)return false;
		else if(id==World.BLOCK_WATER)return false;
		else if(id==World.BLOCK_PLANT_BLUE)return false;
		else if(id==World.BLOCK_SPIDER_WEB)return false;
		return true;
	}
	
	boolean isTraspasableFromSide(int x, int y, int z, int comingSide){
		short type=set_getWorldBlock(x,y,z,World.BLOCK_NULL);
		short id=(short)(type&0xFF);	//ONLY THE ID PART
		short extraData=(short)(type>>8);;
		short rotation=(short) (extraData&0x03);
		short shape=(short) ((extraData&0x0C)>>2);
		boolean noCube=((extraData&0x0C)>0);

		if(id==World.BLOCK_AIR)return true;
		else if(id==World.BLOCK_WATER)return true;
		else if(id==World.BLOCK_SPIDER_WEB)return true;
		else if(id==World.BLOCK_PLANT_BLUE)return true;
		
		if(noCube){
			if(shape==World.SHAPE_TRIANGLE){
				if(comingSide==rotation)return false;
				else return true;
			}
			else return true;
		}
		
		return false;
	}
	
	boolean isTransparent(int x, int y, int z){
		short type=set_getWorldBlock(x,y,z,World.BLOCK_NULL);
		short id=(short)(type&0xFF);	//ONLY THE ID PART
		short extraData=(short)(type>>8);;
		short rotation=(short) (extraData&0x03);
		boolean noCube=((extraData&0x0C)>0);
		
		if(id==World.BLOCK_AIR)return true;
		else if(id==World.BLOCK_WATER)return true;
		else if(id==World.BLOCK_GLASS)return true;
		else if(id==World.BLOCK_LEAFS)return true;
		else if(id==World.BLOCK_SPIDER_WEB)return true;
		else if(id==World.BLOCK_PLANT_BLUE)return true;
		return false;
	}
	
	boolean isTransparentAndNotAir(int x, int y, int z){
		short type=set_getWorldBlock(x,y,z,World.BLOCK_NULL);
		short id=(short)(type&0xFF);	//ONLY THE ID PART
		short extraData=(short)(type>>8);;
		short rotation=(short) (extraData&0x03);
		boolean noCube=((extraData&0x0C)>0);
		
		/*if(id==World.BLOCK_AIR)return true;
		else */if(id==World.BLOCK_WATER)return true;
		else if(id==World.BLOCK_GLASS)return true;
		else if(id==World.BLOCK_LEAFS)return true;
		else if(id==World.BLOCK_SPIDER_WEB)return true;
		else if(id==World.BLOCK_PLANT_BLUE)return true;
		return false;
	}
	
	boolean isTransparentForRender(int x, int y, int z){
		short type=set_getWorldBlock(x,y,z,World.BLOCK_NULL);
		short id=(short)(type&0xFF);	//ONLY THE ID PART
		short extraData=(short)(type>>8);;
		short rotation=(short) (extraData&0x03);
		boolean noCube=((extraData&0x0C)>0);
		
		if(id==World.BLOCK_AIR)return true;
		else if(id==World.BLOCK_WATER)return true;
		return false;
	}
	
	boolean isClimbeable(int x, int y, int z){
		short type=set_getWorldBlock(x,y,z,World.BLOCK_NULL);
		short id=(short)(type&0xFF);	//ONLY THE ID PART
		short extraData=(short)(type>>8);;
		short rotation=(short) (extraData&0x03);
		short shape=(short) ((extraData&0x0C)>>2);
		
		if(id==World.BLOCK_AIR)return false;
		else if(id==World.BLOCK_WATER)return false;
		else if(shape==World.SHAPE_TRIANGLE)return true;
		else if(shape==World.SHAPE_PYRAMID)return true;
		return false;
	}
	
	blockDef blockFaces[]=new blockDef[totalTexturesInAtlas];
		
	
	void writeLong(long in, RandomAccessFile os){
    	try {
			os.writeLong(in);
		} catch (IOException e) {
		}
    }
	
	void writeByte(byte in, RandomAccessFile os){
		try {
			os.writeByte(in);
		} catch (IOException e) {
		}
    }
	
	byte readByte(RandomAccessFile os){
		try {
			return os.readByte();
		} catch (IOException e) {
		}
		return 0;
	}
	
	int readInt(RandomAccessFile os){
		try {
			return os.readInt();
		} catch (IOException e) {
		}
		return 0;
	}
	
	public short set_getWorldBlock(int x, int y, int z, short val){
		if(y>=World.WORLD_HEIGHT || y<0)return World.BLOCK_AIR;
		
		if(val>=0){
			game.server.writeBlock(x,y,z, val);
			return World.BLOCK_AIR;
		}
		return game.server.readBlock(x,y,z);
	}
		
	////////////////////////////
	////////////////////////////
	////////////////////////////
		
	//BLOCKS TYPES
	static short BLOCK_NULL=-1,
			BLOCK_AIR=0,
			BLOCK_GRASS=1,
			BLOCK_WATER=2,
			BLOCK_DUST=3,
			BLOCK_ROCK=4,
			BLOCK_LAVA=5,
			BLOCK_BRICK=6,
			BLOCK_WOOD_1=7,
			BLOCK_QUARTZ=8,
			BLOCK_CARPET_1=9,
			BLOCK_PUMPKIN=10,
			BLOCK_CHEESE=11,
			BLOCK_LEAFS=12,
			BLOCK_TREE_WOOD_1=13,
			BLOCK_FURNACE=14,
			BLOCK_LIBRARY=15,
			BLOCK_GLASS=16,
			BLOCK_SPIDER_WEB=17,
			BLOCK_PLASMA_RED=18,
			BLOCK_PLASMA_VIO=19,
			BLOCK_PLASMA_GRE=20,
			BLOCK_ICE=21,
			BLOCK_PLANT_BLUE=22;

	//BLOCKS SHAPES
	static int
		SHAPE_QUAD=0,
		SHAPE_TRIANGLE=1,
		SHAPE_PYRAMID=2,
		SHAPE_QUAD_MINI=3,
		SHAPE_SPRITE=4;
		
	World(GameLogic game){
		this.game=game;
		textureIndex=0;
		for(int i=0; i<totalTexturesInAtlas; i++)
			blockFaces[i]=new blockDef();
		
		blockFaces[BLOCK_AIR].setFaces(-1);
		blockFaces[BLOCK_GRASS].setFaces(0,2,3,3,3,3);
		blockFaces[BLOCK_WATER].setFaces(14+12*16);
		blockFaces[BLOCK_DUST].setFaces(2);
		blockFaces[BLOCK_ROCK].setFaces(1);
		blockFaces[BLOCK_LAVA].setFaces(14);
		blockFaces[BLOCK_BRICK].setFaces(7+0*16);
		blockFaces[BLOCK_WOOD_1].setFaces(4+0*16);
		blockFaces[BLOCK_QUARTZ].setFaces(7+11*16);
		blockFaces[BLOCK_CARPET_1].setFaces(1+9*16);
		blockFaces[BLOCK_PUMPKIN].setFaces(6+6*16,6+7*16,6+7*16,6+7*16,7+7*16,6+7*16);
		blockFaces[BLOCK_CHEESE].setFaces(0+3*16);
		blockFaces[BLOCK_LEAFS].setFaces(4+3*16);
		blockFaces[BLOCK_TREE_WOOD_1].setFaces(5+1*16,5+1*16,4+1*16,4+1*16,4+1*16,4+1*16);
		blockFaces[BLOCK_FURNACE].setFaces(14+3*16,14+3*16,13+2*16,13+2*16,12+2*16,13+2*16);
		blockFaces[BLOCK_LIBRARY].setFaces(4+0*16,4+0*16,3+2*16,3+2*16,3+2*16,3+2*16);
		blockFaces[BLOCK_GLASS].setFaces(1+3*16);
		blockFaces[BLOCK_SPIDER_WEB].setFaces(11+0*16);
		blockFaces[BLOCK_PLASMA_RED].setFaces(13+7*16);
		blockFaces[BLOCK_PLASMA_VIO].setFaces(14+4*16);
		blockFaces[BLOCK_PLASMA_GRE].setFaces(5+8*16);
		
		blockFaces[BLOCK_ICE].setFaces(3+4*16);
		
		blockFaces[BLOCK_PLANT_BLUE].setFaces(8+5*16);
	}

	void update(){
		//HACER PASAR EL TIEMPO, CALCULAR LUMINOSIDAD, ETC
		timeOfDay++;
		if(timeOfDay>maxTimeOfDay)timeOfDay=0;
		//timeOfDay=3000;
	}
}
