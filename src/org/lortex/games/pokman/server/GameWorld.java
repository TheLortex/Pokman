package org.lortex.games.pokman.server;

import java.io.IOException;
import java.util.Vector;

import org.andengine.entity.primitive.Rectangle;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.lortex.games.pokman.client.TileMapping;
import org.lortex.games.pokman.common.Element;
import org.lortex.games.pokman.common.MazeGenerator;
import org.lortex.games.pokman.common.Type;

import android.util.Pair;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;

public class GameWorld {

	private PokmanServer mContext;
	private PhysicsWorld mPhysicsWorld;
	
	private TileMapping mMapper;

	private FixtureDef wall_fixturedef;
	private FixtureDef pok_fixturedef;
	private FixtureDef point_fixturedef;
	private FixtureDef ghost_fixturedef;
	
	public static final short CATEGORYBIT_WALL = 1;
    public static final short CATEGORYBIT_POKMAN = 2;
    public static final short CATEGORYBIT_POINT = 4;
    public static final short CATEGORYBIT_GHOST = 8;
    public static final short CATEGORYBIT_GHOST_DEAD = 16;

    public static final short MASKBITS_WALL   = CATEGORYBIT_POKMAN + CATEGORYBIT_GHOST + CATEGORYBIT_GHOST_DEAD;
    public static final short MASKBITS_POKMAN = CATEGORYBIT_WALL + CATEGORYBIT_GHOST + CATEGORYBIT_POINT; 
    public static final short MASKBITS_POINT  = CATEGORYBIT_POKMAN; 
    public static final short MASKBITS_GHOST  = CATEGORYBIT_WALL + CATEGORYBIT_POKMAN + CATEGORYBIT_GHOST; 
    public static final short MASKBITS_GHOST_DEAD  = CATEGORYBIT_WALL; 

    private Vector<Pair<GameEntity,GameEntity>> kills = new Vector<Pair<GameEntity,GameEntity>>();
    
    GameEventsManager mgr;
    
    LevelSet current_level;
    
    
	public GameWorld(PokmanServer context) {
		mContext = context;
		mgr = new GameEventsManager(mContext);
		
		current_level = (new LevelManager()).get(mContext.getLevel());
		
		//todo: change to fixed step physics world
		mPhysicsWorld = new PhysicsWorld(new Vector2(0,0), false) {
			@Override
			public void onUpdate(final float pSecondsElapsed) { // gameTick
				/*
				 * 
				 *  Apply forces
				 * 
				 */
				
				
				Vector2 force = new Vector2();
				synchronized(mContext.getPlayers()) {
					for(int j=0;j<mContext.getPlayers().size();j++) {
						Player p = mContext.getPlayers().valueAt(j);
						synchronized(p.controlledEntities) {
							for(GameEntity e : p.controlledEntities) {
								if(e.type == Type.POKMAN)  {
									force.x = p.sensorData.x*current_level.pacmanGravityScale;
									force.y = p.sensorData.y*current_level.pacmanGravityScale;
									e.body.applyForce(force, e.body.getWorldCenter());
								} else if(e.type == Type.GHOST) {
									force.x = p.sensorData.x*current_level.ghostGravityScale;
									force.y = p.sensorData.y*current_level.ghostGravityScale;
									
									e.body.applyForce(force, e.body.getWorldCenter());
									
									GameEntity focus = e.focus;
									if(focus != null) {
										if(focus.body != null) {
											float forceX = focus.body.getWorldCenter().x - e.body.getWorldCenter().x;
											float forceY = focus.body.getWorldCenter().y - e.body.getWorldCenter().y;
											
											if(mgr.getStatusOfEntity(e.id) == GameEventsManager.OKAY) { 
	
												force.set(forceX, forceY);
												force.x = force.x / current_level.ghostAttractDivFactor;
												force.y = force.y / current_level.ghostAttractDivFactor;
											} else { 
												int mulX = (forceX > 0) ? 1 : -1;
												int mulY = (forceY > 0) ? 1 : -1;
												
												
												force.set((forceX*mulX < 5.0) ? mulX*-5 : 0,(forceY*mulY < 5.0) ? mulY*-5 : 0);
											}
											e.body.applyForce(force, e.body.getWorldCenter());
										}
									}
								}
							}
						}
					}
				}
				
				
				
				/*
				 * 
				 *  Update engine
				 * 
				 */
				super.onUpdate(pSecondsElapsed);
				
				synchronized (mContext.getPlayers()) {
					/*
					 * 
					 *  Send the result to players
					 * 
					 */
					for(int j=0;j<mContext.getPlayers().size();j++) {
						Player p = mContext.getPlayers().valueAt(j);
						synchronized (p.controlledEntities) {
							for (int i = 0; i < p.controlledEntities.size(); i++) {
								GameEntity e = p.controlledEntities.get(i);
								mContext.sendBodyData(e.body);
							}
						}
					}
				}
				/*
				 * 
				 *  Handle kills (from collisions)
				 * 
				 */
				for(Pair<GameEntity, GameEntity> data : kills) {
					boolean shouldHideTheBody = mgr.entityDied(data.first, data.second);
					if(shouldHideTheBody)
						this.destroyBody(data.first.body);
				}
				kills.clear();
			}
		};

		wall_fixturedef = PhysicsFactory.createFixtureDef(1, 0f, 1, false, CATEGORYBIT_WALL, MASKBITS_WALL, (short)0);
		pok_fixturedef = PhysicsFactory.createFixtureDef(1, 0f, 0.2f, false, CATEGORYBIT_POKMAN, MASKBITS_POKMAN, (short)0);
		ghost_fixturedef = PhysicsFactory.createFixtureDef(1, 0f, 0.2f, false, CATEGORYBIT_GHOST, MASKBITS_GHOST, (short)0);
		point_fixturedef = PhysicsFactory.createFixtureDef(0, 0f, 0f, false, CATEGORYBIT_POINT, MASKBITS_POINT, (short)0);
		point_fixturedef.isSensor = true;
		
		this.mPhysicsWorld.setContactListener(new ContactListener() {
			@Override
			public void beginContact(Contact contact) {	
				Body a = contact.getFixtureA().getBody();
				Body b = contact.getFixtureB().getBody();
				
				int type_a = -1;
				int type_b = -1;
				if(a.getUserData() instanceof GameEntity)
					type_a = ((GameEntity)a.getUserData()).type;

				if(b.getUserData() instanceof GameEntity)
					type_b = ((GameEntity)b.getUserData()).type;

				if(type_a == Type.POKMAN && type_b == Type.POINT) {
					kills.add(new Pair<GameEntity, GameEntity>((GameEntity)b.getUserData(),(GameEntity)a.getUserData()));
				} else if(type_a == Type.POINT && type_b == Type.POKMAN) {
					kills.add(new Pair<GameEntity, GameEntity>((GameEntity)a.getUserData(),(GameEntity)b.getUserData()));
				} else if((type_a == Type.POKMAN && type_b == Type.GHOST) || (type_a == Type.GHOST && type_b == Type.POKMAN)) {
					GameEntity shouldDie = mgr.whoShouldDie((GameEntity)a.getUserData(), (GameEntity)b.getUserData());
					GameEntity shouldNotDie = mgr.whoShouldNotDie((GameEntity)a.getUserData(), (GameEntity)b.getUserData());
					if(shouldDie == null || shouldNotDie == null)
						return;
					
					kills.add(new Pair<GameEntity, GameEntity>(shouldDie,shouldNotDie));
				} else if(type_a == Type.POKMAN && type_b == Type.BONUS) {
					kills.add(new Pair<GameEntity, GameEntity>((GameEntity)b.getUserData(),(GameEntity)a.getUserData()));
				} else if(type_a == Type.BONUS && type_b == Type.POKMAN) {
					kills.add(new Pair<GameEntity, GameEntity>((GameEntity)a.getUserData(),(GameEntity)b.getUserData()));
				}
			}

			@Override
			public void endContact(Contact contact) {}
			@Override
			public void preSolve(Contact contact, Manifold oldManifold) {}
			@Override
			public void postSolve(Contact contact, ContactImpulse impulse) {}
		});
	}

	
	public PhysicsWorld getPhysicsWorld() {
		return mPhysicsWorld;
	}
	
	public void putWalls(MazeGenerator map) {
		mMapper = new TileMapping(map, mContext.getContext());
		try {
			mMapper.loadTextures();
		} catch (IOException e) {e.printStackTrace();}
		
		for(int x=0;x<map.getWidth();x++) {
			for(int y=0;y<map.getHeight();y++) {
				if(map.value(x, y) == Element.WALL) {
					ITextureRegion tex = mMapper.selectTexture(x, y);
					Vector2 offset = mMapper.getComputedOffset();
					PhysicsFactory.createBoxBody(mPhysicsWorld, new Rectangle(x*TileMapping.TILE_SIZE + offset.x,y*TileMapping.TILE_SIZE+offset.y,tex.getWidth(),tex.getHeight(),mContext.getContext().getVertexBufferObjectManager()), BodyType.StaticBody, wall_fixturedef);
				} 
			}
		}
	}
	
	public Body addPoint(float x, float y) {
		return  PhysicsFactory.createCircleBody(mPhysicsWorld, x*TileMapping.TILE_SIZE,y*TileMapping.TILE_SIZE,3, BodyType.StaticBody, point_fixturedef);
	}
	
	public Body addAPokman(float x, float y) {
		return PhysicsFactory.createCircleBody(mPhysicsWorld, x*TileMapping.TILE_SIZE, y*TileMapping.TILE_SIZE, 18, BodyType.DynamicBody, pok_fixturedef);
	}

	public Body addAGhost(float x, float y) {
		return PhysicsFactory.createCircleBody(mPhysicsWorld, x*TileMapping.TILE_SIZE, y*TileMapping.TILE_SIZE, 18, BodyType.DynamicBody, ghost_fixturedef);
	}

	public Body addBonus(float x, float y) {
		return  PhysicsFactory.createCircleBody(mPhysicsWorld, x*TileMapping.TILE_SIZE,y*TileMapping.TILE_SIZE,8, BodyType.StaticBody, point_fixturedef);
	}

}
