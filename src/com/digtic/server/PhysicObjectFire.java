package com.digtic.server;

public class PhysicObjectFire extends PhysicObject{	
	PhysicObjectFire(Server server, int tid, float x, float y, float z, float vel_x, float vel_y, float vel_z, GameLogic game){
		super(server);
		setPosition(x, y, z);
		setRotation(0,0,0);
		setSize(0.2f, 0.2f, 0.5f);
		vel[0]=vel_x;
		vel[1]=vel_y;
		vel[2]=vel_z;
		reachRange=1;
		this.game=game;
	}
	
	/*@Override
	void render(){
		RendererCube.draw(textureIndex);
		super.render();
	}*/
	
	@Override
	void update(){
		if(onGround){
			//game.events.add(0, (int)pos[0], (int)pos[1]-1, (int)pos[2], game.world.chunkIn((int)pos[0], (int)pos[1]-1, (int)pos[2]), World.BLOCK_LAVA, EventsHandler.MODIFY_CUBE);
			//game.world.setChunkBuildVideo((int)pos[0]/World.CHUNK_SIZE, (int)(pos[1]-1)/World.CHUNK_SIZE, (int)pos[2]/World.CHUNK_SIZE);
			game.objects.remove(this);
		}
		else{
			for(int i=0; i<game.objects.size(); i++){
				MainObject one=game.objects.get(i);
				if(one instanceof EntityPlayer ||one instanceof EntityNPCRock || one instanceof EntityNPCWater){
					Entity one1=(Entity)one;
					if(one!=this
							&& FastMath.sqrt(
									(one1.pos[0]-pos[0])*(one1.pos[0]-pos[0])
									+
									(one1.pos[1]-pos[1])*(one1.pos[1]-pos[1])
									+
									(one1.pos[2]-pos[2])*(one1.pos[2]-pos[2]))<reachRange){
										one1.hurt(1);
										//one1.playSound(EntityPlayer.hitSound, 0,0);
										game.objects.remove(this);
										
					}
				}
			}
		}
		super.update();
	}
}
