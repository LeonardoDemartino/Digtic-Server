package com.digtic.server;

public class EntityPlayer extends Entity{
	//sounds
	static int walkSound=-1,walkWaterSound=-1,hitSound=-1,terrainCreateSound=-1;
	long timeAux=0;
	int remoteMovementsCount;
	
	EntityPlayer(Server server, int connID, float x, float y, float z, GameLogic game/*, GameRender render*/){
		super(server);
		//TYPE ID FOR NETWORK INFORMATION
		type=Entity.TYPE_EntityPlayer;
		///////////////
		
		this.connID=connID;

		//textureIndex=0;
		/*this.headTexture=headTexture;
		this.bodyTexture=bodyTexture;
		this.armTexture=armTexture;
		this.legTexture=legTexture;*/
		setPosition(x, y, z);
		setRotation(0,0,0);
		setSize(0.6f,normalHeight,0.6f);
		this.game=game;
		/*this.render=render;
		if(walkSound==-1 || walkWaterSound==-1 || hitSound==-1 || terrainCreateSound==-1){
			walkSound=this.game.soundManager.load(context, R.raw.walk, 1);
			walkWaterSound=this.game.soundManager.load(context, R.raw.walkwater, 1);
			hitSound=this.game.soundManager.load(context, R.raw.headhit, 1);
			terrainCreateSound=this.game.soundManager.load(context, R.raw.terrain, 1);
		}*/
		//NETWORK
		remoteMovementsCount=0;
	}
	
	/*@Override
	void render(){
		float mod=FastMath.sqrt(lookAt[0]*lookAt[0]+lookAt[1]*lookAt[1]+lookAt[2]*lookAt[2]);
		float x=lookAt[0]/mod;
		float y=lookAt[1]/mod;
		float z=lookAt[2]/mod;
		float headSize=size[0]/2;
		float bodyHeight=size[0]/1.5f; 
		float bodyLength=bodyHeight/2;
		float center_x=-bodyHeight/2, center_y=0, center_z=-bodyLength/2;
		
		//HEAD!
		setMainTransformation(getPosition_x(), getPosition_y(), getPosition_z(), (float) ((Math.atan2(x, z) - Math.atan2(0, 1))*57.2957795f), 0, 0, center_x, center_y, center_z);
		////////////
		Matrix.translateM(render.mModelMatrix, 0, (bodyHeight-headSize)/2, size[1]-headSize, 0);									//POSICION
		Matrix.rotateM(render.mModelMatrix, 0, -y*70, 1, 0,0);																	//ROTACION EJE Y
		Matrix.scaleM(render.mModelMatrix, 0, headSize, headSize, headSize);														//TAMAÑO DEL OBJETO
        //RENDER
		addNewMatrix();
		RendererCube.draw(render.headTextures[headTexture]);
		
		////////////////////////////////////////////////////////////
		
		//BODY
		setMainTransformation(getPosition_x(), getPosition_y(), getPosition_z(), (float) ((Math.atan2(x, z) - Math.atan2(0, 1))*57.2957795f), 0, 0, center_x, center_y, center_z);
		
		Matrix.translateM(render.mModelMatrix, 0, 0, size[1]/2, 0);																	//POSICION
		Matrix.scaleM(render.mModelMatrix, 0, bodyHeight, size[1]/2-headSize, bodyLength);												//TAMAÑO DEL OBJETO
		
        //RENDER
		addNewMatrix();
		RendererCube.draw(render.bodyTextures[bodyTexture]);

		////////////////////////////////////////////////////////////
		
		//LEG 1
		setMainTransformation(getPosition_x(), getPosition_y(), getPosition_z(), (float) ((Math.atan2(x, z) - Math.atan2(0, 1))*57.2957795f), 0, 0, center_x, center_y, center_z);
		
		Matrix.scaleM(render.mModelMatrix, 0, bodyHeight/2.5f, size[1]/2, bodyLength);													//TAMAÑO DEL OBJETO
		//RENDER
		addNewMatrix();
		RendererCube.draw(render.legTextures[legTexture]);
		
		//LEG 2
		setMainTransformation(getPosition_x(), getPosition_y(), getPosition_z(), (float) ((Math.atan2(x, z) - Math.atan2(0, 1))*57.2957795f), 0, 0, center_x, center_y, center_z);
		
		Matrix.translateM(render.mModelMatrix, 0, bodyHeight-bodyHeight/2.5f, 0, 0);														//POSICION
		Matrix.scaleM(render.mModelMatrix, 0, bodyHeight/2.5f, size[1]/2, bodyLength);													//TAMAÑO DEL OBJETO
		//RENDER
		addNewMatrix();
		RendererCube.draw(render.legTextures[legTexture]);
		
		
		//LEFT ARM
		setMainTransformation(getPosition_x(), getPosition_y(), getPosition_z(), (float) ((Math.atan2(x, z) - Math.atan2(0, 1))*57.2957795f), 0, 0, center_x, center_y, center_z);
		
		Matrix.translateM(render.mModelMatrix, 0, bodyHeight, size[1]-size[1]/2, 0);																//POSICION
		Matrix.scaleM(render.mModelMatrix, 0, bodyHeight/3, size[1]/3, bodyLength/1.25f);													//TAMAÑO DEL OBJETO
		//RENDER
		addNewMatrix();
		RendererCube.draw(render.armTextures[armTexture]);
		
		//RIGHT ARM
		setMainTransformation(getPosition_x(), getPosition_y(), getPosition_z(), (float) ((Math.atan2(x, z) - Math.atan2(0, 1))*57.2957795f), 0, 0, center_x, center_y, center_z);
		
		Matrix.translateM(render.mModelMatrix, 0, -bodyHeight/3, size[1]-size[1]/2, 0);																//POSICION
		Matrix.scaleM(render.mModelMatrix, 0, bodyHeight/3, size[1]/3, bodyLength/1.25f);													//TAMAÑO DEL OBJETO
		//RENDER
		addNewMatrix();
		RendererCube.draw(render.armTextures[armTexture]);
				
		super.render();
	}*/
	
	@Override
	void jump(){
		super.jump();
		if(game.time-timeAux>80){
			/*if(inWater())playSound(walkWaterSound,0,1);
			else if(onGround)playSound(walkSound,0,1);*/
			timeAux=game.time;
		}
	}
	
	@Override
	void update(){
		super.update();
		
		if(isMoving){
			if(game.time-timeAux>80){
				/*if(inWater())
					playSound(walkWaterSound,0,1);
				else if(onGround)playSound(walkSound,0,1);*/
				timeAux=game.time;
			}
		}
		
		if(dead)size[1]=0.2f;		
	}
}



