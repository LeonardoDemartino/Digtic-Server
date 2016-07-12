package com.digtic.server;

public class SingleEvent{
	int pos[]={0,0,0};	//GLOBAL CUBE POSITION
	
	float info[]={0,0,0};	//GLOBAL CUBE POSITION
	
	int indexChunk=0;	//GLOBAL CHUNK POSITION
	int action;	//ACTION
	short item;	//ITEM TYPE
	boolean remove;
	
	SingleEvent(int pos_x, int pos_y, int pos_z, int indexChunk, int action, short item){
		remove=false;
		pos[0]=pos_x;
		pos[1]=pos_y;
		pos[2]=pos_z;
		this.indexChunk=indexChunk;
		this.item=item;
		this.action=action;
	}
	
	SingleEvent(float info[], int action){
		remove=false;
		
		this.info[0]=info[0];
		this.info[1]=info[1];
		this.info[2]=info[2];
		
		pos[0]=0;
		pos[1]=0;
		pos[2]=0;
		this.indexChunk=0;
		this.item=0;
		this.action=action;
	}

	SingleEvent Clone(){
		SingleEvent newOne=new SingleEvent(this.pos[0], this.pos[0], this.pos[2], this.indexChunk, this.action, this.item);
		newOne.info[0]=this.info[0];
		newOne.info[1]=this.info[1];
		newOne.info[2]=this.info[2];
		
		return newOne;
	}
}
