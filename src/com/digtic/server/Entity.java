package com.digtic.server;

abstract class Entity extends MainObject{	
	int pointingAt[];
	float pointingDist;
	boolean isCrunching=false;
	byte item;
	float normalHeight=1.8f, crunchHeight=0.8f;
	int toAttack=-1;
	float jumpSpeed;
	boolean jumping;
	int type;
	
	//ONLY CLIENT SIDE
	int headTexture, bodyTexture, legTexture, armTexture;
	/////////////IDs//////////////////////////
	static int TYPE_EntityNPCFire=0;
	static int TYPE_EntityNPCRock=1;
	static int TYPE_EntityNPCWater=2;
	static int TYPE_EntityPlayer=3;
	/////////////////////////////////

	Entity(Server server){		
		super(server);
		pointingAt=new int[6];
		health=20;
		onGround=false;
		dead=false;
		pointingDist=0;
		reachRange=6;
		lookAt[0]=0;
		lookAt[1]=0;
		lookAt[2]=-1;
		toAttack=-1;
		item=0;
		jumping=false;
		jumpSpeed=0.5f;
	}
	
	int getEntityType(){
		return type;
	}
	
	byte getExtraData(){
		byte data=0;
		if(isCrunching)data|=0x80;
		if(onGround)data|=0x40;
		return data;
	}
	
	boolean checkExtraData(byte extraData, int action){
		if((extraData&0x80)==0x80 && action==EventsHandler.ACTION_CRUNCH)return true;
		if((extraData&0x80)==0x00 && action==EventsHandler.ACTION_UNCRUNCH)return true;
		return false;
	}
	
	void crunch(){
		isCrunching=true;
		size[1]=crunchHeight;
	}
	
	void stopCrunch(){
		if(size[1]<normalHeight){
			if(whoUp>0 && whoUp>pos[1]+size[1]+1){
				size[1]=normalHeight;
				isCrunching=false;
			}
		}
	}
	
	void jump(){
		if(dead)return;
		if(!onGround && !inWater())return;
		vel[1]=jumpSpeed;
		jumping=true;
	}
	
	public void modifyPointingCube(boolean send){
    	if(this==null || this.isDead())return;
    	short item=0;
    	if(this.item==0)item=GameLogic.ACTION_BREAK;
    	else if(this.item==1)item=GameLogic.ACTION_SHOOTSTONE;
    	else if(this.item>=GameLogic.CUBES_START_POS)item=GameLogic.ACTION_ADD;
    	
    	if(item==GameLogic.ACTION_BREAK || item==GameLogic.ACTION_ADD){
	    	int 	x=this.pointingAt[0],
	    			y=this.pointingAt[1],
	    			z=this.pointingAt[2];
	    	if(this.pointingDist>0 && this.pointingDist<this.reachRange && y>=0 && y<World.WORLD_HEIGHT){
	    		int xOffset=0, zOffset=0, yOffset=0;
	    		if(item>GameLogic.ACTION_BREAK){
	    			item--;
	    			if(this.pointingAt[3]<this.pointingAt[0])
	      				xOffset=-1;
	      			else if(this.pointingAt[3]>this.pointingAt[0])
	      				xOffset=+1;
	      			else if(this.pointingAt[4]>this.pointingAt[1])
	      				yOffset=+1;
	      			else if(this.pointingAt[4]<this.pointingAt[1])
	      				yOffset=-1;
	      			else if(this.pointingAt[5]>this.pointingAt[2])
	      				zOffset=+1;
	      			else if(this.pointingAt[5]<this.pointingAt[2])
	      				zOffset=-1;
	    		}
	    		
	    		if(y+yOffset<World.WORLD_HEIGHT){
	    			if(canBlockHere(x+xOffset,y+yOffset,z+zOffset)){
	    				game.events.add(x+xOffset, y+yOffset, z+zOffset, -1, item, EventsHandler.MODIFY_CUBE);
	    				//if(send)
	    				//	game.serverManager.sendDataToServer("CHUNK_MOD|"+eventToSendIndex+"|"+(x+xOffset)+"|"+(y+yOffset)+"|"+(z+zOffset)+"|"+action+"|");
	    				//this.playSound(EntityPlayer.terrainCreateSound, 0,0);
	    			}
	    		}
	    	}
    	}
    	else if(item==GameLogic.ACTION_SHOOTSTONE){
    		//game.addObject(new PhysicObjectRock(server, 7, this.pos[0], this.pos[1]+this.size[1]-0.1f, this.pos[2], this.lookAt[0]*30, this.lookAt[1]*30,this.lookAt[2]*30, game));
    	}
    }
	
	boolean canBlockHere(int x, int y,int z){
		//Log.v("Delek", x+":"+(int)player.pos[0]+" "+y+":"+(int)player.pos[1]+" "+z+":"+(int)player.pos[2]);
		if(x==(int)pos[0] && y==(int)pos[1] && z==(int)pos[2])return false;
		return true;
	}
	
	void setNotWalking(){
		isMoving=false;
	}
		
	float getLookAt_x(){
		return lookAt[0];
	}
	float getLookAt_y(){
		return lookAt[1];
	}
	float getLookAt_z(){
		return lookAt[2];
	}
}
