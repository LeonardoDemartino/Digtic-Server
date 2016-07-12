package com.digtic.server;

public class EntityNPCFire extends Entity{
	//sounds
	static int walkSound=-1,hitSound=-1,terrainCreateSound=-1;
		
	EntityNPCFire(Server server, int tid, float x, float y, float z, GameLogic game){
		super(server);
		//TYPE ID FOR NETWORK INFORMATION
		type=Entity.TYPE_EntityNPCFire;
		///////////////

		//this.context=context;
		//textureIndex=tid;
		setPosition(x, y, z);
		setRotation(0,0,0);
		setSize(0.8f,normalHeight,0.8f);
		this.game=game;
		/*if(walkSound==-1 || hitSound==-1 || terrainCreateSound==-1){
			walkSound=this.game.soundManager.load(context, R.raw.walk, 1);
			hitSound=this.game.soundManager.load(context, R.raw.shot_fire, 1);
			terrainCreateSound=this.game.soundManager.load(context, R.raw.terrain, 1);
		}*/
	}
	
	/*@Override
	void render(){
		RendererCube.draw(headTexture);
		super.render();
	}*/
	
	@Override
	void update(){
		//BUSCAR ALGUNA ENTIDAD NO ENEMIGA PARA ATACAR Y MOVERSE HASTA ELLA
		int rnd=(int)(Math.random()*2000);
		float headRollSpeed=0.3f;
		float rnd_x=((float)Math.random()*headRollSpeed*2-headRollSpeed);
		float rnd_z=((float)Math.random()*headRollSpeed*2-headRollSpeed);
		
		if(rnd>200){
			float dist=10;
			toAttack=-1;
			for(int i=0; i<game.objects.size(); i++){
				MainObject one=game.objects.get(i);
				if(one instanceof EntityPlayer){
					if(one!=this){
						float dir[]={pos[0]-one.pos[0],
								pos[1]-one.pos[1],
								pos[2]-one.pos[2]};
						float dirMod=FastMath.sqrt(dir[0]*dir[0]+dir[1]*dir[1]+dir[2]*dir[2]);
						if(dirMod<dist){
							dist=dirMod;
							toAttack=i;
						}				
					}
				}
			}
		}
		

		if(toAttack>=0 && toAttack<game.objects.size()){
			MainObject one=game.objects.get(toAttack);
			float dir[]={pos[0]-one.pos[0],
					pos[1]-one.pos[1],
					pos[2]-one.pos[2]};
			float dirMod=FastMath.sqrt(dir[0]*dir[0]+dir[1]*dir[1]+dir[2]*dir[2]);
			if(rnd>195){
				//game.addObject(new PhysicObjectFire(server, 7, pos[0], pos[1]+1, pos[2], (-dir[0]/dirMod)*30, (dir[1]/dirMod)*30,(-dir[2]/dirMod)*30, game));
				//playSound(hitSound, 0,0);
			}
			isMoving=true;
			int minDistance=2;
			setLookAt(-dir[0], lookAt[1], -dir[2]);
			if(dirMod>minDistance){
				if(rnd<1000)
					addAction(EventsHandler.ACTION_FORWARD);
				if(rnd<100)addAction(EventsHandler.ACTION_JUMP);
			}
		}
		else{
			if(rnd<200)
				setLookAt(lookAt[0]+rnd_x, lookAt[1], lookAt[2]+rnd_z);
		}
		
		super.update();
	}
}


