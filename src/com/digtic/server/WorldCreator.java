package com.digtic.server;

import java.io.IOException;
import java.io.RandomAccessFile;

public class WorldCreator{	
	public static float writeLand(int x, int seed, int z){
		return (float) (SimplexNoise.noise(x/50.0,seed,z/50.0));
	}
	
	public static float writeCaves(int x, int seed, int z){
		return (float) (SimplexNoise.noise(x/100.0,seed, z/100.0));	//LLENO DE CAPAS VERTICALES, CEBOLLA WORLD
	}
	
	static void populate(int x, int z, RandomAccessFile f){
		try {
			short chunkBuffer[][][]=new short[World.CHUNK_SIZE][World.WORLD_HEIGHT][World.CHUNK_SIZE];
			int seed=125;
			f.seek(0);
			
			for(int q=0; q<World.CHUNK_SIZE; q++){
				for(int e=0; e<World.CHUNK_SIZE; e++){
					float value=0;
					int y_index=0;					
					/////SURFACE
					
					//MOUNTAINS!
					value=(writeLand(	x*World.CHUNK_SIZE+q,seed,z*World.CHUNK_SIZE+e)+1)/2.0f;
					y_index=(int) (value*(World.WORLD_HEIGHT/2-1));
					for(int y=y_index; y>=0; y--){						
						chunkBuffer[q][y][e]=World.BLOCK_GRASS;	
					}

					//WATER
					for(int y=World.WORLD_HEIGHT/8; y>=0; y--){
						if(chunkBuffer[q][y][e]==World.BLOCK_AIR)
							chunkBuffer[q][y][e]=World.BLOCK_WATER;	
					}
					
					//RANDOM TREES!!!
					int rnd=(int) (Math.random()*100);
					if(rnd==32 && y_index+1<World.WORLD_HEIGHT && chunkBuffer[q][y_index+1][e]!=World.BLOCK_WATER){
						//TREE TYPE
						int treeType=(int) (Math.random()*3);
						short treeBodyType=0, treeHeadType=0;
						if(treeType==0){
							treeBodyType=World.BLOCK_PLASMA_RED;
							treeHeadType=(short) (World.BLOCK_PLASMA_RED | (World.SHAPE_PYRAMID<<10));
						}
						
						if(treeType==1){
							treeBodyType=World.BLOCK_PLASMA_VIO;
							treeHeadType=(short) (World.BLOCK_PLASMA_VIO | (World.SHAPE_PYRAMID<<10));
						}
						if(treeType==2){
							treeBodyType=World.BLOCK_PLASMA_GRE;
							treeHeadType=(short) (World.BLOCK_PLASMA_GRE | (World.SHAPE_PYRAMID<<10));
						}
						
						//TREE ROOT
						int rootSize=(int) (Math.random()*4)+1;
						for(int i=0;i<rootSize; i++){
							int thisY=y_index+1+i;
							if(thisY<World.WORLD_HEIGHT)chunkBuffer[q][thisY][e]=(short) (World.BLOCK_TREE_WOOD_1 | (World.SHAPE_QUAD_MINI<<10));
						}
						
						//TREE BODY
						int bodySize=(int) (Math.random()*4);
						for(int i=0;i<bodySize; i++){
							int thisY=y_index+1+rootSize+i;
							if(thisY<World.WORLD_HEIGHT)chunkBuffer[q][thisY][e]=treeBodyType;
						}
						
						//TREE HEAD
						int thisY=y_index+1+rootSize+bodySize;
						if(thisY<World.WORLD_HEIGHT)chunkBuffer[q][thisY][e]=treeHeadType;
					}
					
					//RANDOM PLANTS
					if(rnd>80 && y_index+1<World.WORLD_HEIGHT && chunkBuffer[q][y_index+1][e]!=World.BLOCK_WATER){
						//PLANT
						int thisY=y_index+1;
						if(thisY<World.WORLD_HEIGHT)chunkBuffer[q][thisY][e]=World.BLOCK_PLANT_BLUE;
					}
					
					//RANDOM PUMPKINS
					/*if(rnd>90 && y_index+1<World.WORLD_HEIGHT && chunkBuffer[q][y_index+1][e]!=World.BLOCK_WATER){
						//PLANT
						int thisY=y_index+1;
						if(thisY<World.WORLD_HEIGHT)chunkBuffer[q][thisY][e]=World.BLOCK_PUMPKIN;
					}*/
					
					//UNDERGROUND
					
					//SPACE UNDERGROUND
					/*value=(writeCaves(	x*World.CHUNK_SIZE+q,seed,z*World.CHUNK_SIZE+e)+1)/2.0f;
					y_index=(int) (value*(World.WORLD_HEIGHT/2-1));
					for(int y=y_index; y>=0; y--){			
						if(y==y_index)chunkBuffer[q][y][e]=World.BLOCK_ROCK;
						else chunkBuffer[q][y][e]=World.BLOCK_AIR;	
					}
					
					//UNDERGROUND POOLS
					value=(writeCaves(	x*World.CHUNK_SIZE+q,seed,z*World.CHUNK_SIZE+e)+1)/2.0f;
					y_index=(int) (value*(World.WORLD_HEIGHT/4-1));
					for(int y=y_index; y>=0; y--){			
						chunkBuffer[q][y][e]=World.BLOCK_ROCK;	
					}
					
					//DEEP WATER
					for(int y=8; y>=0; y--){
						if(chunkBuffer[q][y][e]==World.BLOCK_AIR)
							chunkBuffer[q][y][e]=World.BLOCK_WATER;	
					}*/
				}
			}
			
			
			for(int q=0; q<World.CHUNK_SIZE; q++){
				for(int e=0; e<World.CHUNK_SIZE; e++){
					for(int w=0; w<World.WORLD_HEIGHT; w++){
						byte rot=(byte)(Math.random()*4);
						chunkBuffer[q][w][e]|=(rot<<8);	//ROTATION
						
						f.writeShort(chunkBuffer[q][w][e]);
					}
				}
			}

			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
