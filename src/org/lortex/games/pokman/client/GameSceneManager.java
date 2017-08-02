package org.lortex.games.pokman.client;

import java.io.IOException;
import java.util.Vector;

import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.AlphaModifier;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.sprite.batch.SpriteBatch;
import org.andengine.entity.sprite.batch.SpriteGroup;
import org.andengine.entity.text.Text;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.util.adt.color.Color;
import org.andengine.util.adt.list.SmartList;
import org.lortex.games.pokman.client.scenes.GameScene;
import org.lortex.games.pokman.common.Element;
import org.lortex.games.pokman.common.GameMode;
import org.lortex.games.pokman.common.MazeGenerator;
import org.lortex.games.pokman.common.Type;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;

public class GameSceneManager {
	private Scene mScene;
	private GameScene mContext;
	private TileMapping mapper;
	private Vector<Sprite> mapSprites = new Vector<Sprite>();

	private SparseArray<Sprite> ghostSprites = new SparseArray<Sprite>();
	private SparseArray<Sprite> pokSprites  = new SparseArray<Sprite>();
	
	private int mapCenterX, mapCenterY;
	private int mapWidth, mapHeight;
	private SpriteGroup sprPoints;
	
    private Font theFont;
	private Text startbutton;
	
	private Vector<Integer> deadGhosts = new  Vector<Integer>();

	private Vector<Sprite> removeQueue = new Vector<Sprite>();
	private Vector<Sprite> addQueue = new Vector<Sprite>();


	public GameSceneManager(GameScene context, Scene scene) {
		mScene = scene;
		mContext = context;
		
		mScene.registerUpdateHandler(new IUpdateHandler() {

			@Override
			public void onUpdate(float pSecondsElapsed) {
				renderTick();
			}

			@Override
			public void reset() {}
		});
		
		mapCenterX = mContext.getContext().getWidth()/2;
		mapCenterY = SceneManagerActivity.getHeight()/2 + GameHUD.HUD_HEIGHT/2;
		
		loadResources();
		
		sprPoints = new SpriteGroup(mContext.getTextureHandler().get("point").getTexture(), 1000, mContext.getContext().getVertexBufferObjectManager())  {
		    @Override
		    protected boolean onUpdateSpriteBatch() {
		            return false;
		    }

		    @Override
		    protected void onManagedUpdate(float pSecondsElapsed) {
		            super.onManagedUpdate(pSecondsElapsed);
		            final SmartList<IEntity> children = this.mChildren;
		            if(children != null) {
		                    final int childCount = children.size();
		                    for(int i = 0; i < childCount; i++) {
		                            this.drawWithoutChecks((Sprite)children.get(i));
		                    }
		                    submit();
		            }
		    }
		};
		String startText;

		if(mContext.getGameMode().mode == GameMode.SINGLEPLAYER)
			startText = "LEVEL "+mContext.getGameMode().level;
		else
			startText = "GET READY";

		if(mContext.getGameMode().mode != GameMode.ONLINE_MULTIPLAYER_TEST) {
			startbutton = new Text(mContext.getContext().getWidth()/2.0f, GameHUD.HUD_HEIGHT/2+50, theFont, startText, mContext.getContext().getVertexBufferObjectManager()){
			    @Override
			    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float X, float Y) 
			    {
			    	if (pSceneTouchEvent.isActionDown() || pSceneTouchEvent.isActionMove())
			        {
			        	this.setColor(Color.YELLOW);
			        }else if (pSceneTouchEvent.isActionUp())
			        {
			        	this.detachSelf();
			        	mContext.readyToStart();
			        } 
			        return true;
			    };
			};
			
			new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
				@Override
				public void run() {
					mContext.getContext().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							mScene.registerTouchArea(startbutton);
							if(mContext.getGameMode().mode == GameMode.SINGLEPLAYER)
								startbutton.setText("START");
							else
								startbutton.setText("JOIN");
						}
					});
				}
			}, 1500);
			mScene.attachChild(startbutton);
		}
		
		mScene.setOnSceneTouchListener(new IOnSceneTouchListener() {
			@Override
			public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
				if(startbutton != null)
					startbutton.setColor(Color.WHITE);
				return false;
			}
		});
	}


	private void loadResources() {
		FontFactory.setAssetBasePath("font/");

		this.theFont = FontFactory.createFromAsset(mContext.getContext().getFontManager(), mContext.getContext().getTextureManager(), 256, 256, TextureOptions.BILINEAR, mContext.getContext().getAssets(), "police.ttf", 60, true, android.graphics.Color.WHITE);
		this.theFont.load();
	}
	
	public void renderMap(MazeGenerator map) {
		for(Sprite spr : mapSprites) {
			spr.detachSelf();
		}
		
		mapSprites.clear();

		mapper = new TileMapping(map, mContext.getContext());
		try {
			mapper.loadTextures();
		} catch (IOException e) {
			e.printStackTrace();
		}

		mapWidth = map.getWidth();
		mapHeight = map.getHeight();

		
		SpriteBatch sprWalls = new SpriteBatch(mapper.getTexture(),map.getCountWalls(true),mContext.getContext().getVertexBufferObjectManager());
		
		for(int x=0;x<map.getWidth();x++) {
			for(int y=0;y<map.getHeight();y++) {
				if(map.value(x, y) == Element.WALL) {
					Sprite wSpr = mapper.getWallSprite((int) (mapCenterX + (x - mapWidth/2.0f)*TileMapping.TILE_SIZE), (int) (mapCenterY + (y - mapHeight/2.0f)*TileMapping.TILE_SIZE), x, y);
					mapSprites.add(wSpr);
					sprWalls.draw(wSpr);
				}
			}
		}
		sprWalls.submit();
		//sprWalls.setShaderProgram(mContext.getCustomColorShaderProgram()); TODO: WORK ON IT
		mScene.attachChild(sprWalls);
		mScene.attachChild(sprPoints);
	}
	
	public void onEat(final Sprite p){
		if(p != null) {
			if(p.getUserData() instanceof String) {
				if(((String)p.getUserData()).equalsIgnoreCase("POKMAN")) {
					mContext.getContext().runOnUiThread(new Runnable() {
						@Override 
						public void run() {
							p.setTextureRegion(mContext.getTextureHandler().get("pokman_mange"));
							
							new Handler().postDelayed(new Runnable() {
								@Override
								public void run() {
									p.setTextureRegion(mContext.getTextureHandler().get("pokman"));
								}
							}, 200);
						}
					});
				}
			}
		}
	}

	
	public Sprite pleaseAddThisEntityMyDear(int type, float x, float y, float rotation, int data) {
		Sprite theSprite;
		switch(type) {
		case Type.POKMAN:
			Log.w("LORTEX","New pokman rendered! "+data);
			theSprite = new Sprite((int) (mapCenterX + x - (mapWidth/2.0f)*TileMapping.TILE_SIZE + TileMapping.TILE_SIZE/2f), (int) (mapCenterY + y - (mapHeight/2.0f)*TileMapping.TILE_SIZE), mContext.getTextureHandler().get("pokman"), mContext.getContext().getVertexBufferObjectManager());
			theSprite.setRotation(rotation);
			theSprite.setUserData("POKMAN");
			pokSprites.append(data, theSprite);
			addQueue.add(theSprite);
			return theSprite;
		case Type.POINT:
			theSprite = new Sprite((int) (mapCenterX + x - (mapWidth/2.0f)*TileMapping.TILE_SIZE + TileMapping.TILE_SIZE/2f), (int) (mapCenterY + y - (mapHeight/2.0f)*TileMapping.TILE_SIZE), mContext.getTextureHandler().get("point"), mContext.getContext().getVertexBufferObjectManager());
			theSprite.setRotation(rotation);
			theSprite.setAlpha(0);
			theSprite.registerEntityModifier(new AlphaModifier(1f,0,1));
			sprPoints.attachChild(theSprite);
			return theSprite;
		case Type.BONUS:
			Log.w("LORTEX","New bonus rendered! ");
			theSprite = new Sprite((int) (mapCenterX + x - (mapWidth/2.0f)*TileMapping.TILE_SIZE + TileMapping.TILE_SIZE/2f), (int) (mapCenterY + y - (mapHeight/2.0f)*TileMapping.TILE_SIZE), mContext.getTextureHandler().get("bonus"), mContext.getContext().getVertexBufferObjectManager());
			theSprite.setRotation(rotation);
			theSprite.setAlpha(0);
			theSprite.registerEntityModifier(new AlphaModifier(1f,0,1));
			addQueue.add(theSprite);
			return theSprite;
		case Type.GHOST:
			theSprite = new Sprite((int) (mapCenterX + x - (mapWidth/2.0f)*TileMapping.TILE_SIZE + TileMapping.TILE_SIZE/2f), (int) (mapCenterY + y - (mapHeight/2.0f)*TileMapping.TILE_SIZE), mContext.getTextureHandler().get("ghost"+(data%4+1)), mContext.getContext().getVertexBufferObjectManager());
			theSprite.setRotation(rotation);
			addQueue.add(theSprite);
			ghostSprites.append(data, theSprite);
			return theSprite;
		}
		return null;
	}
	
	public void pleaseMoveThisEntityMyDear(Sprite theSprite, float x, float y, float rotation) {
		if(theSprite == null)
			return; 
		theSprite.setX((int) (mapCenterX + x - (mapWidth/2.0f)*TileMapping.TILE_SIZE) + TileMapping.TILE_SIZE/2f);
		theSprite.setY((int) (mapCenterY + y - (mapHeight/2.0f)*TileMapping.TILE_SIZE));
		theSprite.setRotation((float) (-rotation*360/(2*3.14)));
	}

	public void gtfo(final Sprite sprite) {
		if(sprite != null) {
			removeQueue.add(sprite);
		}
	}

	protected void renderTick() {
		synchronized(removeQueue) {
			for(Sprite spr : removeQueue)
				spr.detachSelf();
		}
		removeQueue.clear();
		
		synchronized(addQueue) {
			for(Sprite spr : addQueue) {
				mScene.attachChild(spr);
			}
		}
		addQueue.clear();
	}

	public void bonusActivated(int id) {}
	public void bonusNearEnd(int id) {}
	public void bonusEnded(int id) {}

	public void ghost_vulnerable(int data_ghost) {
		mStopEndVulnAnimation.put(data_ghost,true);
		ghostSprites.get(data_ghost).setTextureRegion(mContext.getTextureHandler().get("ghost_vulnerable"));
		Log.i("Lortex","Ghost vulnerable : "+data_ghost);
	}

	public void ghost_vulnerable_near_end(int data_ghost) {
		mStopEndVulnAnimation.put(data_ghost,false);
		new Handler(Looper.getMainLooper()).post(new UpdateVulnerableMonstersRunnable(data_ghost, true));
		Log.i("Lortex","Ghost vulnerable near end "+data_ghost);
	}

	public void ghost_vulnerable_ended(int data_ghost) {
		mStopEndVulnAnimation.put(data_ghost,true);
		ghostSprites.get(data_ghost).setTextureRegion(mContext.getTextureHandler().get("ghost"+(data_ghost%4+1)));
		Log.i("Lortex","Ghost vulnerable end : "+data_ghost);
	}

	public void ghostDeath(int data) {
		mStopEndDeathAnimation.put(data,true);
		ghostSprites.get(data).setTextureRegion(mContext.getTextureHandler().get("ghost_death"));
		deadGhosts.add(data);
		
		Log.i("Lortex","Ghost dead : "+data);
	}

	public void ghostReviveSoon(int data) {
		mStopEndDeathAnimation.put(data,false);
		new Handler(Looper.getMainLooper()).post(new UpdateDeadMonsterRunnable(data,true));
		Log.i("Lortex","Ghost dead revive soon : "+data);
	}

	public void ghostRevive(int data) {
		mStopEndDeathAnimation.put(data,true);
		ghostSprites.get(data).setTextureRegion(mContext.getTextureHandler().get("ghost"+(data%4+1)));
		int index = deadGhosts.indexOf(data);
		Log.i("Lortex","Ghost revive : "+data);
		
		if(index != -1)
			deadGhosts.remove(index);
	}


	private SparseBooleanArray mStopEndVulnAnimation = new SparseBooleanArray();
	private SparseBooleanArray mStopEndDeathAnimation = new SparseBooleanArray();
	
	public class UpdateVulnerableMonstersRunnable implements Runnable {
		private boolean mTick;
		private int mData;
		
		public UpdateVulnerableMonstersRunnable(int data, boolean tick) {
			super();
			mTick = tick;
			mData = data;
		}
		
		@Override
		public void run() {
			if(mStopEndVulnAnimation.get(mData))
				return;
			
			Sprite spr = ghostSprites.get(mData);
			
			if(deadGhosts.indexOf(mData) == -1) {
				if(mTick)
					spr.setTextureRegion(mContext.getTextureHandler().get("ghost_vulnerable2"));
				else
					spr.setTextureRegion(mContext.getTextureHandler().get("ghost_vulnerable"));
			}
			new Handler().postDelayed(new UpdateVulnerableMonstersRunnable(mData,!mTick),500);
		}
	}

	public class UpdateDeadMonsterRunnable implements Runnable {
		private boolean mTick;
		private int mData;
		
		public UpdateDeadMonsterRunnable(int data, boolean tick) {
			super();
			mTick = tick;
			mData = data;
		}
		
		@Override
		public void run() {
			if(mStopEndDeathAnimation.get(mData))
				return;
			
			Sprite spr = ghostSprites.get(mData);
			if(mTick)
				spr.setTextureRegion(mContext.getTextureHandler().get("ghost_res"+(mData%4+1)));
			else
				spr.setTextureRegion(mContext.getTextureHandler().get("ghost_death"));
			
			new Handler().postDelayed(new UpdateDeadMonsterRunnable(mData,!mTick),500);
		}
	}

	
}
