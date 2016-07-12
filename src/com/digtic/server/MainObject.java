package com.digtic.server;

abstract class MainObject {
	float pos[];
	float lookAt[];
	float vel[];
	float rot[];
	float size[];
	float moveSpeed;
	int chunk[];
	int whoDown, whoUp, whoLeft, whoRight, whoFront, whoBack;
	boolean onGround;
	boolean isMoving;
	boolean dead;
	int reachRange;
	GameLogic game;
	Server server;
	int health;
	int id=-1;
	EventsHandler events;
	EventsHandler eventsPrediction;
	EventsHandler pendingEvents;
	int eventToSendIndex=0;
	boolean renderBoundingBox;
	int connID=-1;
	String networkName="";

	MainObject(Server server){
		this.server=server;
		networkName="";
		renderBoundingBox=false;
		//////////////////
		pos=new float[3];
		lookAt=new float[3];
		rot=new float[3];
		size=new float[3];
		vel=new float[3];
		chunk=new int[2];
		size[0]=0.5f;
		size[1]=0.5f;
		size[2]=0.5f;
		vel[0]=0;
		vel[1]=0;
		vel[2]=0;
		moveSpeed=0.4f;
		chunk[0]=-1;
		chunk[1]=-1;
		health=100;
		reachRange=0;
		onGround=false;
		dead=false;
		isMoving=false;
		events=new EventsHandler();
		eventsPrediction=new EventsHandler();
		pendingEvents=new EventsHandler();
	}

	public void addAction(int action){
		events.add(action);
	}

	public void addAction(float info[], int action){
		events.add(info, action);
	}


	public void move(float tox, float toy, float toz, float speed){
		pos[0] += tox*speed;
		pos[1] += toy*speed;
		pos[2] += toz*speed;

		isMoving=true;
	}

	void hurt(int damage){
		health-=damage;
		if(health<=0){
			dead=true;
			game.objects.remove(this);
		}
	}

	boolean isDead(){
		return dead;
	}

	void setLookAt(float x, float y, float z){
		if(dead)return;
		float module=FastMath.sqrt(x*x+y*y+z*z);
		lookAt[0]=x/module;
		lookAt[1]=y/module;
		lookAt[2]=z/module;
	}

	boolean inWater(){
		int thisCenter=game.world.set_getWorldBlock((int)pos[0], (int)pos[1], (int)pos[2],  World.BLOCK_NULL);
		int thisUp=game.world.set_getWorldBlock((int)pos[0], (int)(pos[1]+size[1]), (int)pos[2],  World.BLOCK_NULL);
		if(((thisCenter&0xFF)==World.BLOCK_WATER) || ((thisUp&0xFF)==World.BLOCK_WATER))
			return true;
		
		return false;
	}
	
	boolean headInWater(){
		int thisUp=game.world.set_getWorldBlock((int)pos[0], (int)(pos[1]+size[1]), (int)pos[2], World.BLOCK_NULL);
		if((thisUp&0xFF)==World.BLOCK_WATER)
			return true;
		
		return false;
	}

	void loadClosestColliders(float pos[]){
		int check_length=10;
		whoUp=(int) (pos[1]+check_length);
		whoDown=(int) (pos[1]-check_length);
		whoRight=(int) (pos[0]+check_length);
		whoLeft=(int) (pos[0]-check_length);
		whoFront=(int) (pos[2]+check_length);
		whoBack=(int) (pos[2]-check_length);

		/////UP AND DOWN. X & Z PLANE
		int limit_minus[]={FastMath.floor(pos[0]-size[0]/2),FastMath.floor(pos[1]),FastMath.floor(pos[2]-size[2]/2)};
		int limit_plus[]={FastMath.ceil(pos[0]+size[0]/2),FastMath.ceil(pos[1]+size[1]),FastMath.ceil(pos[2]+size[2]/2)};
		int obj=-1;
		
		for(int x=limit_minus[0]; x<limit_plus[0]; x++){
			for(int z=limit_minus[2]; z<limit_plus[2]; z++){
				for(int y=0; y<check_length; y++){
					obj=FastMath.floor(pos[1])-check_length+y;
					if(!game.world.isTraspasableFromSide(x,obj, z, -1) && obj>whoDown)whoDown=obj;
					if(game.world.isClimbeable(x,obj+1, z) && obj>whoDown)whoDown=obj;
					obj=FastMath.floor(pos[1])+check_length-y;
					if(!game.world.isTraspasableFromSide(x,obj, z, -1) && obj<whoUp)whoUp=obj;
					if(game.world.isClimbeable(x,obj, z) && obj<whoUp)whoUp=obj;
				}
			}
		}

		/////LEFT AND RIGHT. Y & Z PLANE
		for(int y=limit_minus[1]; y<limit_plus[1]; y++){
			for(int z=limit_minus[2]; z<limit_plus[2]; z++){
				for(int x=0; x<check_length; x++){
					obj=FastMath.floor(pos[0])-check_length+x;
					if(!game.world.isTraspasableFromSide(obj,y, z, 3) && obj>whoLeft)whoLeft=obj;
					obj=FastMath.floor(pos[0])+check_length-x;
					if(!game.world.isTraspasableFromSide(obj,y, z, 1) && obj<whoRight)whoRight=obj;
				}
			}
		}
		
		/////BACK AND FRONT. X & Y PLANE
		for(int y=limit_minus[1]; y<limit_plus[1]; y++){
			for(int x=limit_minus[0]; x<limit_plus[0]; x++){
				for(int z=0; z<check_length; z++){
					obj=FastMath.floor(pos[2])-check_length+z;
					if(!game.world.isTraspasableFromSide(x,y, obj, 2) && obj>whoBack)whoBack=obj;
					obj=FastMath.floor(pos[2])+check_length-z;
					if(!game.world.isTraspasableFromSide(x,y, obj, 0) && obj<whoFront)whoFront=obj;
				}
			}
		}
	}
	
	void limitMovement(float pos[]){
		//IF COLLIDE WITH UP
		if(pos[1]+size[1]>whoUp){
			if(vel[1]>0)vel[1]=0;
			pos[1]=whoUp-size[1];
			if(this instanceof Entity){
				Entity esta2=(Entity)this;
				esta2.jumping=false;
			}
		}
		//IF COLLIDE WITH DOWN
		float plus=0.0f;
		if(game.world.isClimbeable((int)pos[0], whoDown+1, (int)pos[2])){
			int type=game.world.set_getWorldBlock((int)pos[0], whoDown+1, (int)pos[2], World.BLOCK_NULL);
			short id=(short)(type&0xFF);	//ONLY THE ID PART
			short extraData=(short)(type>>8);
			short rotation=(short) (extraData&0x03);
			short shape=(short) ((extraData&0x0C)>>2);
			if(shape==World.SHAPE_TRIANGLE){
				if(rotation==0)plus=FastMath.abs(pos[2]-((int)pos[2]+1))*1.5f;
				if(rotation==1)plus=FastMath.abs(pos[0]-((int)pos[0]+1))*1.5f;
				if(rotation==2)plus=FastMath.abs(pos[2]-((int)pos[2]))*1.5f;
				if(rotation==3)plus=FastMath.abs(pos[0]-((int)pos[0]))*1.5f;
			}
			else if(shape==World.SHAPE_PYRAMID){
				float center[]={((int)pos[0])+0.5f, ((int)pos[2])+0.5f};
				
				float deltas[]={FastMath.abs(center[0]-pos[0]), FastMath.abs(center[1]-pos[2])};
				
				float distance=FastMath.sqrt(deltas[0]*deltas[0]+deltas[1]*deltas[1]);
				
				if(distance<0.5f)plus=(0.5f-distance)*2;
			}
		}

		if(pos[1]<=whoDown+1+plus){
			if(vel[1]<0)vel[1]=0;
			pos[1]=whoDown+1+plus;
			onGround=true;
		}
		else onGround=false;

		//IF COLLIDE WITH SIDES
		if(pos[0]<whoLeft+size[0]/2.0f+1){
			if(vel[0]<0)vel[0]=0;
			pos[0]=whoLeft+size[0]/2.0f+1;
		}
		if(pos[0]>whoRight-size[0]/2.0f){
			if(vel[0]>0)vel[0]=0;
			pos[0]=whoRight-size[0]/2.0f;
		}
		//IF COLLIDE FRONT/BACK
		if(pos[2]<whoBack+size[2]/2.0f+1){
			if(vel[2]<0)vel[2]=0;
			pos[2]=whoBack+size[2]/2.0f+1;
		}
		if(pos[2]>whoFront-size[2]/2.0f){
			if(vel[2]>0)vel[2]=0;
			pos[2]=whoFront-size[2]/2.0f;
		}
	}
	
	void gravity(){
		///SET GRAVITY IF NOT IN GROUND, STOP FALLING IF IT IS ON GROUND.
		if(!onGround){
			int jumpingState=0;
			if(this instanceof Entity){
				Entity esta2=(Entity)this;
				if(esta2.jumping)jumpingState=1;
				else jumpingState=2;
			}
			if(jumpingState==0 || jumpingState==2){ //0 NUNCA SALTA, 1 ENTIDAD SALTANDO, 2 ENTIDAD NO SALTANDO
				vel[1]-=game.GRAVITY;
				if(inWater()){
					if(vel[1]>game.MAX_FALLING_SPEED_WATER)vel[1]=game.MAX_FALLING_SPEED_WATER;
					else if(vel[1]<-game.MAX_FALLING_SPEED_WATER)vel[1]=-game.MAX_FALLING_SPEED_WATER;
				}
				else{
					if(vel[1]>game.MAX_FALLING_SPEED)vel[1]=game.MAX_FALLING_SPEED;
					else if(vel[1]<-game.MAX_FALLING_SPEED)vel[1]=-game.MAX_FALLING_SPEED;
				}
					
			}
			else if(jumpingState==1){  //1 ENTIDAD SALTANDO
				if(this instanceof Entity){
					Entity esta2=(Entity)this;
					esta2.vel[1]-=game.GRAVITY;
					if(esta2.vel[1]<0.01f){
						esta2.vel[1]=0;
						esta2.jumping=false;
					}
				}
			}
		}
	}

	void runAction(SingleEvent cur){
		loadClosestColliders(pos);

		float lootAtModule=FastMath.sqrt(lookAt[0]*lookAt[0]+lookAt[1]*lookAt[1]+lookAt[2]*lookAt[2]);
		float lookAtN[]={lookAt[0]/lootAtModule, lookAt[1]/lootAtModule, lookAt[2]/lootAtModule};

		if(cur.action==EventsHandler.ACTION_FORWARD){
			this.move(lookAtN[0], 0, lookAtN[2], moveSpeed*(inWater()?0.5f:1));
			//server.sendToAllPlayers(EventsHandler.ACTION_FORWARD,id);
		}
		else if(cur.action==EventsHandler.ACTION_BACK){
			this.move(-lookAtN[0], 0, -lookAtN[2], moveSpeed*(inWater()?0.5f:1));
			//server.sendToAllPlayers(EventsHandler.ACTION_BACK,id);
		}
		else if(cur.action==EventsHandler.ACTION_LEFT){
			this.move(lookAtN[2], 0, -lookAtN[0], moveSpeed*(inWater()?0.5f:1));
			//server.sendToAllPlayers(EventsHandler.ACTION_LEFT,id);
		}
		else if(cur.action==EventsHandler.ACTION_RIGHT){				
			this.move(-lookAtN[2], 0, lookAtN[0], moveSpeed*(inWater()?0.5f:1));
			//server.sendToAllPlayers(EventsHandler.ACTION_RIGHT,id);
		}
		else if(cur.action==EventsHandler.ACTION_JUMP){
			if(!(this instanceof EntityPlayer))
				((Entity)this).jump();
			server.sendToAllPlayers(EventsHandler.ACTION_JUMP,id);
		}
		else if(cur.action==EventsHandler.ACTION_CRUNCH){
			((Entity)this).crunch();
		}
		else if(cur.action==EventsHandler.ACTION_UNCRUNCH){
			((Entity)this).stopCrunch();
		}
		else if(cur.action==EventsHandler.ACTION_LOOK){
			this.setLookAt(cur.info[0], cur.info[1], cur.info[2]);
			//server.sendToAllPlayers(EventsHandler.ACTION_LOOK, id, lookAt[0], lookAt[1], lookAt[2]);
		}
		else if(cur.action==EventsHandler.ACTION_POS){
			this.setPosition(cur.info[0], cur.info[1], cur.info[2]);
			//server.sendToAllPlayers(EventsHandler.ACTION_LOOK, id, lookAt[0], lookAt[1], lookAt[2]);
		}
		else if(cur.action==EventsHandler.MODIFY_CUBE){
			((Entity)this).modifyPointingCube(true);
			//This is a WORLD event, so the update to the server is done after game.processEvents accept this command.
			//What the server will recieve is an EventsHandler.MODIFY_CUBE
		}

		if(!(this instanceof EntityPlayer))
			limitMovement(pos);
	}

	void preUpdate(){
		isMoving=false;		

		//ONLY CLIENT SIDE
		//chunkIndex=game.chunkManager.getChunkIn(chunk[0], chunk[1]);
		//if(chunkIndex<0 || chunkIndex>=game.chunkManager.chunks.size() || !game.chunkManager.chunks.get(chunkIndex).loaded)return;

		gravity();

		loadClosestColliders(pos);
		pos[0]+=vel[0]*(inWater()?0.5f:1);
		pos[1]+=vel[1]*(inWater()?0.5f:1);
		pos[2]+=vel[2]*(inWater()?0.5f:1);		
		limitMovement(pos);
	}

	void update(){
		chunk[0]=Math.round((getPosition_x()-World.CHUNK_SIZE/2)/World.CHUNK_SIZE);
		chunk[1]=Math.round((getPosition_z()-World.CHUNK_SIZE/2)/World.CHUNK_SIZE);
		if(getPosition_x()<0)chunk[0]-=1;
		if(getPosition_z()<0)chunk[1]-=1;
		if(!(this instanceof EntityPlayer))
			preUpdate();

		//PROCESS EVENTS
		for(int i=0; i<events.size(); i++){
			SingleEvent cur=events.get(i);
			if(cur!=null){
				runAction(cur);
				//System.out.println("Event "+cur.id+" "+EventsHandler.getName(cur.action));
				events.setToRemove(i);
			}
			//if(i==events.size()-1)System.out.println(pos[0]+" "+pos[1]+" "+pos[2]);
		}
		events.clear();

		//SEND POS TO OTHER PLAYERS
		int type=-1;
		byte extraData=0;
		if(this instanceof Entity){
			extraData=((Entity)this).getExtraData();
			type=((Entity)this).getEntityType();
		}

		if(this instanceof EntityPlayer)
			server.sendToOtherPlayersClose(EventsHandler.PLAYER_DATA, id, type, pos[0],pos[1], pos[2],lookAt[0], lookAt[1], lookAt[2], extraData, ((EntityPlayer)this).connID, pos[0], pos[2]);
		else server.sendToAllPlayersClose(EventsHandler.PLAYER_DATA, id, type, pos[0],pos[1], pos[2],lookAt[0], lookAt[1], lookAt[2], extraData, pos[0], pos[2]);
		//System.out.println("Player: "+id+" "+pos[0]+" "+pos[1]+" "+pos[2]+" "+lookAt[0]+" "+lookAt[1]+" "+lookAt[2]);
	}

	void setPosition(float []_pos){
		pos[0]=_pos[0];
		pos[1]=_pos[1];
		pos[2]=_pos[2];
	}

	void setPosition(float x, float y, float z){
		pos[0]=x;
		pos[1]=y;
		pos[2]=z;
	}

	void setRotation(float []_rot){
		rot[0]=_rot[0];
		rot[1]=_rot[1];
		rot[2]=_rot[2];
	}

	void setRotation(float x, float y, float z){
		rot[0]=x;
		rot[1]=y;
		rot[2]=z;
	}

	void setSize(float []_size){
		size[0]=_size[0];
		size[1]=_size[1];
		size[2]=_size[2];
	}

	void setSize(float x, float y, float z){
		size[0]=x;
		size[1]=y;
		size[2]=z;
	}

	void setVelocity(float []_vel){
		vel[0]=_vel[0];
		vel[1]=_vel[1];
		vel[2]=_vel[2];
	}

	void setVelocity(float x, float y, float z){
		vel[0]=x;
		vel[1]=y;
		vel[2]=z;
	}

	float [] getPosition(){
		return pos;
	}
	float getPosition_x(){
		return pos[0];
	}
	float getPosition_y(){
		return pos[1];
	}
	float getPosition_z(){
		return pos[2];
	}

	float [] getRotation(){
		return rot;
	}

	float getRotation_x(){
		return rot[0];
	}
	float getRotation_y(){
		return rot[1];
	}
	float getRotation_z(){
		return rot[2];
	}

	float [] getVelocity(){
		return vel;
	}

	float getVelocity_x(){
		return vel[0];
	}
	float getVelocity_y(){
		return vel[1];
	}
	float getVelocity_z(){
		return vel[2];
	}

	float [] getSize(){
		return size;
	}

	float getSize_x(){
		return size[0];
	}

	float getSize_y(){
		return size[1];
	}

	float getSize_z(){
		return size[2];
	}

	float getMaxSize(){
		return Math.max(size[0], Math.max(size[1], size[2]));
	}
}
