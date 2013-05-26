package com.lortexgames.pokman_final.activities;

import java.io.IOException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.andengine.engine.Engine;
import org.andengine.engine.camera.ZoomCamera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Line;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.shape.Shape;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.sprite.batch.SpriteBatch;
import org.andengine.entity.sprite.batch.SpriteGroup;
import org.andengine.entity.text.Text;
import org.andengine.extension.input.touch.controller.MultiTouch;
import org.andengine.extension.input.touch.controller.MultiTouchController;
import org.andengine.extension.input.touch.detector.PinchZoomDetector;
import org.andengine.extension.input.touch.detector.PinchZoomDetector.IPinchZoomDetectorListener;
import org.andengine.extension.input.touch.exception.MultiTouchException;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.debug.Debug;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.lortexgames.pokman_final.AppInterface;
import com.lortexgames.pokman_final.Element;
import com.lortexgames.pokman_final.FontManager;
import com.lortexgames.pokman_final.MazeGenerator;
import com.lortexgames.pokman_final.TileMapping;
import com.lortexgames.pokman_final.addons.MaxStepPhysicsWorld;
import com.lortexgames.pokman_final.handlers.BluetoothConnectedThread;
import com.lortexgames.pokman_final.handlers.BluetoothReceiverInterface;
import com.lortexgames.pokman_final.handlers.EntityFollowerHandler;
import com.lortexgames.pokman_final.handlers.PlayerHandler;
import com.lortexgames.pokman_final.handlers.TextureHandler;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.SparseArray;
import android.view.Display;
import android.view.WindowManager;

public class MultiplayerActivity extends SimpleBaseGameActivity  implements SensorEventListener, BluetoothReceiverInterface, IPinchZoomDetectorListener, IOnSceneTouchListener {

	private FontManager font;
	private ZoomCamera mCamera;
	private Scene mScene;
	
	private static final int TILE_SIZE=40;
    private final static int HUD_HEIGHT=120;

  //  private static final int GM_POK_VS_GHOST = 0;
    private static final int GM_DEATHMATCH   = 1;
  //  private static final int GM_COOP         = 2;
    
	private int marginLeft;
	private int marginTop;

    private int N_COL=25;
    private int N_ROW=35;

	private BluetoothConnectedThread server;
	private Vector<BluetoothConnectedThread> clients;
	private HashMap<BluetoothConnectedThread,PlayerHandler> mPlayerEntities=new HashMap<BluetoothConnectedThread,PlayerHandler>(); 
	private PlayerHandler mServerPlayer;
	private boolean mServer;

	private SparseArray<Body> mGhosts  = new SparseArray<Body>();
	private SparseArray<Body> mPokmans = new SparseArray<Body>();
	
	private Vector<SparseArray<Sprite>> mPoints = new Vector<SparseArray<Sprite>>();

	private MazeGenerator maze;
	private TileMapping tileMapper;
	
	private MaxStepPhysicsWorld mPhysicsWorld;
	
	private float[] rawValues = {0,0};
	private float[] realValues= {0,0};
	private SensorManager sensorManager;
	private Sensor accelerometer;
	public static final int axisSwap[][] = { 
	    {  1,  1,  0,  1  },     // ROTATION_0 
	    {-1,  1,  1,  0  },     // ROTATION_90 
	    {-1,    -1,  0,  1  },     // ROTATION_180 
	    {  1,    -1,  1,  0  }  }; // ROTATION_270 
	private static int[] as;


	private boolean gameStarted=false;
	private PointCollisionThread pointCollisionDetector;
	private TextureHandler tex;

	private int mIndex;
	private int nPlayers;
	private int mGamemode = GM_DEATHMATCH;
	
	private int nReady=0;
	private SparseArray<Text> mScoresText = new SparseArray<Text>();
	private PinchZoomDetector mPinchZoomDetector;
	private float mPinchZoomStartedCameraZoomFactor;
	private boolean receivingMap;
	
	private EntityFollowerHandler efh;
	private float mMinZoomFactor;
	
	@Override
	public EngineOptions onCreateEngineOptions() {
		mCamera = new ZoomCamera(0, 0, MenuActivity.getWidth(), MenuActivity.getHeight());
		EngineOptions engineOptions =  new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(MenuActivity.getWidth(),MenuActivity.getHeight()), mCamera);
		engineOptions.setUpdateThreadPriority(android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);
		return engineOptions;
	}
	
	@Override
	public Engine onCreateEngine(EngineOptions pEngineOptions) {
		Engine e = super.onCreateEngine(pEngineOptions);
        try {
            if(MultiTouch.isSupported(this)) {
                    e.setTouchController(new MultiTouchController());
            } else {
            	Debug.e("Sorry your device does NOT support MultiTouch!\n\n(No PinchZoom is possible!)");
            }
	    } catch (final MultiTouchException err) {
	           Debug.e("Sorry your Android Version does NOT support MultiTouch!\n\n(No PinchZoom is possible!)");
	    }
		return e;
	}

	@Override
	protected void onCreateResources() {
		font = new FontManager(this);
		tex  = new TextureHandler(this);
		
		mServer = getIntent().getBooleanExtra("isServer", false);
		mIndex  = getIntent().getIntExtra("pIndex", 1);
		nPlayers= getIntent().getIntExtra("nPly", 4);

		toastOnUIThread("I'm player "+mIndex);
		
		AppInterface app = (AppInterface)this.getApplication();

		mCamera.setBounds(0, -HUD_HEIGHT, N_COL*TILE_SIZE, N_ROW*TILE_SIZE);
		mCamera.setBoundsEnabled(true);
		
		mMinZoomFactor =  (float)(MenuActivity.getHeight() - HUD_HEIGHT - 50) / (float) (N_ROW * TILE_SIZE);
		
		if(mMinZoomFactor > 1)
			mCamera.setZoomFactor(mMinZoomFactor);
		
		marginLeft = 0;
		marginTop  = 0;
		
		maze = new MazeGenerator(N_COL, N_ROW, 1);
		if(mServer) {
			clients = app.getAllConnectionThread();
			for(int i=0;i<clients.size();i++)
				clients.get(i).setContext(this);
			
			maze.randomize();
		} else {
			server = app.getConnectionThread();
			server.setContext(this);
			server.write("MAPPLS".getBytes());
			receivingMap=false;
			new Timer().schedule(new TimerTask() {
				@Override
				public void run() {
					if(receivingMap==false)
						server.write("MAPPLS".getBytes());
				}
			},2000);
		}
		
		tileMapper = new TileMapping(maze,this);
		try {
			tileMapper.loadTextures();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			loadTextures();
		} catch (IOException e1) {
			Debug.e(e1);
		}
		
		font.load(30);
		font.load(24);

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		mPhysicsWorld = new MaxStepPhysicsWorld(60, new Vector2(0f, 0f), false, 8 ,1);
		

	}

	


	@Override
	protected Scene onCreateScene() {
		mScene = new Scene();
		mScene.registerUpdateHandler(mPhysicsWorld);
		
		mCamera.setHUD(setupHUD());
		efh = new EntityFollowerHandler(this, mCamera,HUD_HEIGHT);
		if(mServer) {
			int ghostIndex=1;
			
			for(int i=0;i<clients.size();i++) {
				PlayerHandler ply = new PlayerHandler();
				
				Vector2 pos = getPosition(i+2,true);
				Sprite curPoksprite = new Sprite(pos.x, pos.y, tex.get("pok"), this.getVertexBufferObjectManager());
				Body pokguy = PhysicsFactory.createCircleBody(mPhysicsWorld, curPoksprite, BodyType.DynamicBody,  PhysicsFactory.createFixtureDef(1f, 0, 0.2f));
				pokguy.setUserData(new String("pokman"));
				
				ply.addPokman(pokguy);
				curPoksprite.setZIndex(50);
				
				efh.addSprite(curPoksprite);

				mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(curPoksprite, pokguy));
				mScene.attachChild(curPoksprite);
				
				TextureRegion trg = tex.get("ghost"+ghostIndex);
				if(trg == null)
					trg = tex.get("ghost1");

				pos = getPosition(i+2,false);
				Sprite curGhostsprite = new Sprite(pos.x, pos.y, trg, this.getVertexBufferObjectManager());
				Body aGhost = PhysicsFactory.createCircleBody(mPhysicsWorld, curGhostsprite, BodyType.DynamicBody,  PhysicsFactory.createFixtureDef(1f, 0, 0.2f));
				aGhost.setUserData(new String("ghost"));
				ply.addGhost(aGhost);
				ghostIndex++;


				curGhostsprite.setZIndex(50);
				efh.addSprite(curGhostsprite);
				
				mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(curGhostsprite, aGhost));
				mScene.attachChild(curGhostsprite);
				
				mPlayerEntities.put(clients.get(i),ply);
			}
			mServerPlayer = new PlayerHandler();

			Vector2 pos = getPosition(1,true);
			Sprite curPoksprite = new Sprite(pos.x, pos.y, tex.get("pok"), this.getVertexBufferObjectManager());
			Body pokguy = PhysicsFactory.createCircleBody(mPhysicsWorld, curPoksprite, BodyType.DynamicBody,  PhysicsFactory.createFixtureDef(1f, 0, 0.2f));
			pokguy.setUserData(new String("pokman"));
			
			mServerPlayer.addPokman(pokguy);

			curPoksprite.setZIndex(50);
			efh.addSprite(curPoksprite);
			
			mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(curPoksprite, pokguy));
			mScene.attachChild(curPoksprite);
			mCamera.setChaseEntity(curPoksprite);
			
			TextureRegion trg = tex.get("ghost"+ghostIndex);
			if(trg == null)
				trg = tex.get("ghost1");
			
			pos = getPosition(1,false);
			Sprite curGhostsprite = new Sprite(pos.x, pos.y, trg, this.getVertexBufferObjectManager());
			Body aGhost = PhysicsFactory.createCircleBody(mPhysicsWorld, curGhostsprite, BodyType.DynamicBody,  PhysicsFactory.createFixtureDef(1f, 0, 0.2f));
			aGhost.setUserData(new String("ghost"));
			mServerPlayer.addGhost(aGhost);

			curGhostsprite.setZIndex(50);
			efh.addSprite(curGhostsprite);
			
			mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(curGhostsprite, aGhost));
			mScene.attachChild(curGhostsprite);
			
			
			
			showMap(maze.getCountWalls());
			mScene.registerUpdateHandler(new IUpdateHandler() {

				@Override
				public void onUpdate(float pSecondsElapsed) {
					serverTick();
				}

				@Override
				public void reset() {}
			});
			pointCollisionDetector = new PointCollisionThread();
			pointCollisionDetector.start();
			
			mPhysicsWorld.setContactListener(new ContactListener() {
				@Override
				public void beginContact(Contact contact) {}

				@Override
				public void endContact(Contact contact) {
					
					
					/*Body A = contact.getFixtureA().getBody();
					Body B = contact.getFixtureB().getBody();
					
					if((((String)A.getUserData()).equalsIgnoreCase("pokman") && ((String)B.getUserData()).equalsIgnoreCase("ghost")) || (((String)A.getUserData()).equalsIgnoreCase("ghost") && ((String)B.getUserData()).equalsIgnoreCase("pokman"))) {
						Body pokman = ((String)A.getUserData()).equalsIgnoreCase("pokman") ? A : B;
						//TODO: Manage pokman death
					}
						*/
					
				}

				@Override
				public void preSolve(Contact contact, Manifold oldManifold) {
					Body A = contact.getFixtureA().getBody();
					Body B = contact.getFixtureB().getBody();
					
					String AuserData = (String)A.getUserData();
					String BuserData = (String)B.getUserData();
					
					if((AuserData == null) || (BuserData == null))
						return;
					
					if( (AuserData.equalsIgnoreCase("pokman") && BuserData.equalsIgnoreCase("ghost")) || (AuserData.equalsIgnoreCase("ghost") && BuserData.equalsIgnoreCase("pokman"))) {
						final Body pokman = AuserData.equalsIgnoreCase("pokman") ? A : B;
				//		final Body ghost  = AuserData.equalsIgnoreCase("pokman") ? B : A;
						
						for(int i=0;i<clients.size();i++) {
							PlayerHandler curClient = mPlayerEntities.get(clients.get(i));
							for(int g=0;g<curClient.getPokmans().size();g++) {
								if(curClient.getPokmans().get(g) == pokman)
									curClient.updateScore(-30);
							}
						}
						
						PlayerHandler curClient = mServerPlayer;
						for(int g=0;g<curClient.getPokmans().size();g++) {
							if(curClient.getPokmans().get(g) == pokman)
								curClient.updateScore(-30);
						}
						
						runOnUpdateThread( new Runnable(){
							@Override
							public void run() {
								pokman.setTransform(3, 3, 0);
							}
						});
					}
				}

				@Override
				public void postSolve(Contact contact, ContactImpulse impulse) {}
			});
		}

		if(MultiTouch.isSupportedByAndroidVersion()) {
            try {
                    this.mPinchZoomDetector = new PinchZoomDetector(this);
            } catch (final MultiTouchException e) {
                    this.mPinchZoomDetector = null;
            }
	    } else {
	            this.mPinchZoomDetector = null;
	    }

		mScene.setOnSceneTouchListener(this);
		mScene.sortChildren();
		return mScene;
	}
	@Override
	public void onResumeGame() {
		super.onResumeGame();
		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
	}
	
	@Override
	public void onPauseGame() {
		super.onPauseGame();
		sensorManager.unregisterListener(this, accelerometer);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		if(mServer) {
			broadcast("STOP DSL");
			for(int i=0;i<clients.size();i++)
				clients.get(i).cancel();
		} else {
			server.write("RAGEQUIT".getBytes());
			server.cancel();
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		gameStarted=false;
		mScene.detachSelf();
	}
	
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if(gameStarted) {
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
				int orientation = display.getRotation();
				
				rawValues[0] = event.values[0];
				rawValues[1] = event.values[1];
				
				realValues[0] = 0f;
				realValues[1] = 0f;
				
				canonicalToScreenOrientation(orientation,rawValues,realValues);
	
				float x = realValues[0];
				float y = realValues[1];
				
				if(mServer) {
					mServerPlayer.newGravity(-x, y);
				} else {
					server.write(("GRVT "+(-x)+" "+y).getBytes());
				}
			}
		}
	}
	



	@Override
	public synchronized void message(String string, int bytes, BluetoothConnectedThread sender) {
		String msg = string.substring(0, bytes);
    	//Debug.i("BT::Socket::<"+msg);
		
		manageRequest(msg, sender);
	}


	private synchronized void manageRequest(String request, BluetoothConnectedThread sender) {
		String[] trames = request.split("#");
		for(int i=0;i<trames.length;i++) {
			String curMsg = trames[i];
			String[] split = curMsg.split(" ");
			
			if(split.length > 0) {
				if(split[0].equalsIgnoreCase("MAP")&& split.length == 4) {
					int x = Integer.parseInt(split[1]);
					int y = Integer.parseInt(split[2]);
					int element = Integer.parseInt(split[3]);
					
					maze.value(x, y, Element.values()[element]);
					
					receivingMap=true;
				} else if(split[0].equalsIgnoreCase("MAPOK")&& split.length == 2) {
					showMap(Integer.parseInt(split[1]));
					gameStarted=true;
				} else if(split[0].equalsIgnoreCase("POKM") && split.length == 6) {
					try {
						int pid = Integer.parseInt(split[1]);
						int index = Integer.parseInt(split[2]);
						float x = Float.parseFloat(split[3]);
						float y = Float.parseFloat(split[4]);
						float angle = Float.parseFloat(split[5]);
						
						updatePokman(index,x,y,angle,pid);
						
					} catch (NumberFormatException e) {
						Debug.w(e);
					}
					
				}  else if(split[0].equalsIgnoreCase("GHST") && split.length == 5) {
					try {
						int index = Integer.parseInt(split[1]);
						float x = Float.parseFloat(split[2]);
						float y = Float.parseFloat(split[3]);
						float angle = Float.parseFloat(split[4]);
						
						updateGhost(index,x,y,angle);
					} catch (NumberFormatException e) {
						Debug.w(e);
					}
					
				} else if(split[0].equalsIgnoreCase("EAT")&& split.length == 3) {
					final int x=Integer.parseInt(split[1]);
					final int y=Integer.parseInt(split[2]);
					
        			runOnUpdateThread(new Runnable() {
        				@Override
        				public void run() {
        					if(mPoints.get(x).get(y) != null) {
        						mPoints.get(x).get(y).detachSelf();
        						mPoints.get(x).remove(y);
        					}
        				}
        			});
				} else if(split[0].equalsIgnoreCase("STOP")) {
					this.toastOnUIThread("Server stopped");
					finish();
				} else if(split[0].equalsIgnoreCase("SCORE") && split.length == 3) {
					int id    = Integer.parseInt(split[1]);
					int score = Integer.parseInt(split[2]);
					
					updateScore(id,score);
				}else if(split[0].equalsIgnoreCase("GRVT")&& split.length == 3) {
					float newX = Float.parseFloat(split[1]);
					float newY = Float.parseFloat(split[2]);
					mPlayerEntities.get(sender).newGravity(newX, newY);
				}else if(split[0].equalsIgnoreCase("RAGEQUIT")) {
					this.toastOnUIThread("A client has quit");
				} else if(split[0].equalsIgnoreCase("MAPPLS")) {
					sendMap(sender);
				}
			}
		}
	}

	private synchronized void updatePokman(final int index, final float x, final float y, final float angle,final int pid) {
		this.runOnUpdateThread(new Runnable() {
			@Override
			public void run() {
				if(mPokmans.get(index) == null) {
					Sprite spr = new Sprite(0,0,tex.get("pok"),getVertexBufferObjectManager());
					efh.addSprite(spr);
					Body pac   = PhysicsFactory.createBoxBody(mPhysicsWorld, spr, BodyType.DynamicBody, PhysicsFactory.createFixtureDef(1, 0, 0.2f));
					
					mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(spr, pac,true,true));
					mScene.attachChild(spr);
					
					mPokmans.put(index, pac);

					if(pid == mIndex)
						mCamera.setChaseEntity(spr);
				} 
				
				mPokmans.get(index).setTransform(x, y, angle);
				efh.update();
			}
		});
	}
	
	private synchronized void updateGhost(final int index, final float x, final float y, final float angle) {
		this.runOnUpdateThread(new Runnable() {
			@Override
			public void run() {
				if(mGhosts.get(index) == null) {
					TextureRegion hi = tex.get("ghost"+index);
					if(hi == null)
						hi = tex.get("ghost1");
						
					Sprite spr = new Sprite(0,0,hi,getVertexBufferObjectManager());
					Body ghst   = PhysicsFactory.createBoxBody(mPhysicsWorld, spr, BodyType.DynamicBody, PhysicsFactory.createFixtureDef(1, 0, 0.2f));
					efh.addSprite(spr);
					mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(spr, ghst,true,true));
					mScene.attachChild(spr);
					
					mGhosts.put(index, ghst);
				} 
				
				mGhosts.get(index).setTransform(x, y, angle);
				efh.update();
			}
		});
	}
	

	private void showMap(int nWalls) {
		SpriteBatch sprWall = new SpriteBatch(tileMapper.getTexture(),nWalls,this.getVertexBufferObjectManager());
		SpriteGroup sprPoints = new SpriteGroup(tex.get("point").getTexture(), 1000, this.getVertexBufferObjectManager());
		
		for(int y=0;y<N_ROW;y++) {
			for(int x=0;x<N_COL;x++) {
				if(y==0)
					mPoints.add(new SparseArray<Sprite>());
				
				if(maze.value(x, y) == Element.MUR){
					Sprite wall = tileMapper.getWallSprite(marginLeft + x*(TILE_SIZE), marginTop+y*(TILE_SIZE),x,y, mScene);
					if(mServer)
						PhysicsFactory.createBoxBody(this.mPhysicsWorld, wall, BodyType.StaticBody, PhysicsFactory.createFixtureDef(1, 0f, 1f));
					
					sprWall.draw(wall);
				} else if(maze.value(x, y) == Element.POINT) {
					Sprite point = new Sprite((marginLeft + x*(TILE_SIZE) + TILE_SIZE/2)-tex.get("point").getWidth()/2, (marginTop + y*(TILE_SIZE) + TILE_SIZE/2)-tex.get("point").getHeight()/2,tex.get("point"),this.getVertexBufferObjectManager());
					point.setZIndex(0);
					mPoints.get(x).put(y, point);
					
					sprPoints.attachChild(point);
				}
			}
		}
		sprWall.submit();
		sprPoints.submit();
		mScene.attachChild(sprWall);
		mScene.attachChild(sprPoints);
	}
	
	private void sendMap(BluetoothConnectedThread client) {
		for(int y=0;y<N_ROW;y++) {
			for(int x=0;x<N_COL;x++) {
				client.write(("MAP "+x+" "+y+" "+maze.value(x, y).ordinal()).getBytes());
			}
			
		}
		client.write(("MAPOK "+maze.getCountWalls()).getBytes());
		nReady++;
		if(nReady+1 == nPlayers)
			gameStarted=true;
	}
	
	private void broadcast(String request) {
		for(int i=0;i<clients.size();i++) 
			clients.get(i).write(request.getBytes());
	}
	
	private HUD setupHUD() {
		// Draw line on the half
		HUD hud = new HUD();
		float w = MenuActivity.getWidth();
		float h = HUD_HEIGHT;
		
		Rectangle background = new Rectangle(0,0,w,h,this.getVertexBufferObjectManager());
		background.setColor(0, 0, 0);
		hud.attachChild(background);
		
		Line separator = new Line(w/2f,15, w/2f, h-15,this.getVertexBufferObjectManager());
		hud.attachChild(separator);
		int sidePlyIndex = 1;
		for(int i=1;i<=nPlayers;i++) {
			Text score;
			if(i==mIndex) {
				score = new Text(0,0, font.get(30),"SCORE\nXXXXXX",this.getVertexBufferObjectManager());
				score.setX(w/4f - score.getWidth()/2f);
				score.setY(h/2f - score.getHeight()/2f);
			} else {

				score = new Text(0,(h/((float)nPlayers))*sidePlyIndex - 12, font.get(24),"P"+i+" : XXXXXX",this.getVertexBufferObjectManager());
				score.setX(3*w/4f - score.getWidth()/2f);
				
				sidePlyIndex++;
			}
			hud.attachChild(score);
			
			mScoresText .put(i,score);
		}
		return hud;
	}
	
	protected void updateScore(int id, int score) {
		if(id == mIndex)
			mScoresText.get(id).setText("SCORE\n"+String.format("%05d", score));
		else
			mScoresText.get(id).setText("P"+id+" : "+String.format("%05d", score));
	}
	
	protected void serverTick() {
		if(gameStarted) {
		
			int ghstIndex=1;
			int pokIndex=1;
			for(int i=0;i<clients.size();i++) {
				for(int c=0;c<clients.size();c++) {
					PlayerHandler ply = mPlayerEntities.get(clients.get(c));
	
					Vector<Body> ghosts  = ply.getGhosts();
					for(int g=0;g<ghosts.size();g++) {
						clients.get(i).write(("GHST "+ghstIndex+" "+ghosts.get(g).getPosition().x+" "+ghosts.get(g).getPosition().y+" "+ghosts.get(g).getAngle()).getBytes());
						ghstIndex++;
					}
					
					Vector<Body> pokmans = ply.getPokmans();
					for(int p=0;p<pokmans.size();p++) {
						clients.get(i).write(("POKM "+(c+2)+ " "+pokIndex+" "+pokmans.get(p).getPosition().x+" "+pokmans.get(p).getPosition().y+" "+pokmans.get(p).getAngle()).getBytes());
						pokIndex++;
					}
					
					clients.get(i).write(("SCORE "+(c+2)+" "+ply.getScore()).getBytes());
					updateScore(c+2,ply.getScore());
				}
				
				
	
				Vector<Body> ghosts  = mServerPlayer.getGhosts();
				for(int g=0;g<ghosts.size();g++) {
					clients.get(i).write(("GHST "+ghstIndex+" "+ghosts.get(g).getPosition().x+" "+ghosts.get(g).getPosition().y+" "+ghosts.get(g).getAngle()).getBytes());
					ghstIndex++;
				}
				
				Vector<Body> pokmans = mServerPlayer.getPokmans();
				for(int p=0;p<pokmans.size();p++) {
					clients.get(i).write(("POKM 1 "+pokIndex+" "+pokmans.get(p).getPosition().x+" "+pokmans.get(p).getPosition().y+" "+pokmans.get(p).getAngle()).getBytes());
					pokIndex++;
				}

				clients.get(i).write(("SCORE 1 "+mServerPlayer.getScore()).getBytes());
				updateScore(1,mServerPlayer.getScore());
			}
		}
		efh.update();
	}

	private Vector2 getPosition(int index, boolean pok) {
		int x=0,y=0;
		if(pok) {
			switch((index%4)+1) {
			case 1:
				x=(N_COL-1)/2;
				y=(N_ROW+1)/2-1;
				break;
			case 2:
				x=(N_COL+1)/2;
				y=(N_ROW+1)/2-1;
				break;
			case 3:
				x=(N_COL-1)/2;
				y=(N_ROW+1)/2+1;
				break;
			case 4:
				x=(N_COL+1)/2;
				y=(N_ROW+1)/2+1;
				break;
			}
		} else {
			switch((index%4)+1) {
			case 1:
				x=1;
				y=1;
				break;
			case 2:
				x=N_COL-2;
				y=1;
				break;
			case 3:
				x=1;
				y=N_ROW-2;
				break;
			case 4:
				x=N_COL-2;
				y=N_ROW-2;
				break;
			}
		}
		return new Vector2(x*TILE_SIZE+marginLeft,y*TILE_SIZE+marginTop);
	}
	
	private void loadTextures() throws IOException {
		tex.load("pok","gfx/newgfx/pacman.png");
		tex.load("pokM","gfx/newgfx/pacman_mange.png");
		tex.load("ghost1","gfx/newgfx/monster_red_mange.png");
		tex.load("ghost2","gfx/newgfx/monster_blue_mange.png");
		tex.load("ghost3","gfx/newgfx/monster_pink_mange.png");
		tex.load("ghost4","gfx/newgfx/monster_orange_mange.png");
		tex.load("ghstVul","gfx/newgfx/monster_vulnerable.png");
		tex.load("ghstVul2","gfx/newgfx/monster_vulnerable2.png");
		tex.load("ghstDead","gfx/newgfx/monster_death.png");
		tex.load("ghstDead2","gfx/newgfx/monster_death2.png");
		tex.load("bPoint","gfx/bonus_point.png");
		tex.load("point","gfx/point.png");
		tex.load("popup","gfx/bg_popup.png");
		tex.load("startText","gfx/bg_start_text.png");

	}
	
	static void canonicalToScreenOrientation(int displayRotation, float[] canVec, float[] screenVec) 
	{ 
	    as = axisSwap[displayRotation]; 
	    screenVec[0]  =  (float)as[0] * canVec[ as[2] ]; 
	    screenVec[1]  =  (float)as[1] * canVec[ as[3] ]; 
	} 
	
	private class PointCollisionThread extends Thread {
		private boolean continuer=true;
		
		public void run() {
        	while(continuer) {
        		// Check points for each pokman
        		if(gameStarted) {
	        		for(int i=0;i<=clients.size();i++) {
	        			PlayerHandler curGuy = null;
	        			if(i==clients.size()) 
	        				curGuy = mServerPlayer;
	        			else 
	        				curGuy = mPlayerEntities.get(clients.get(i));
	        			
	        			for(int p=0;p<curGuy.getPokmans().size();p++) {
	        				Body pkbd = curGuy.getPokmans().get(p);
	                    	Shape pokShape = (Shape) mPhysicsWorld.getPhysicsConnectorManager().findShapeByBody(pkbd);
	                    	
	        				int pacManX = (int) ((pokShape.getX()-marginLeft+tex.get("pok").getWidth()/2f)/TILE_SIZE);
	        				int pacManY = (int) ((pokShape.getY()-marginTop+tex.get("pok").getHeight()/2f)/TILE_SIZE);
	
	                    	if((pacManX < 0)||(pacManX > N_COL))
	                    		pacManX = 0;
	                    	
	                    	if((pacManY < 0)||(pacManY > N_ROW))
	                    		pacManY = 0;
	
	                    	if(pokShape != null) {
		                    	if(mPoints.get(pacManX).get(pacManY) != null) {
		                    		if(pokShape.collidesWith(mPoints.get(pacManX).get(pacManY))) {
		                        		final Sprite toDetach = mPoints.get(pacManX).get(pacManY);
		                    			
		                    			MultiplayerActivity.this.runOnUpdateThread(new Runnable() {
		                    				@Override
		                    				public void run() {
		                    					toDetach.detachSelf();
		                    				}
		                    			});
	
		                    			mPoints.get(pacManX).remove(pacManY);
	                					
		                    			if(mGamemode == GM_DEATHMATCH)
		                    				curGuy.updateScore(1);
		                    			else
		                    				curGuy.updateScore(10);
		                    			//clients.get(i).write(bytes)
		                    			broadcast("EAT "+pacManX+" "+pacManY);
		                    		}
		                    	}
	                    	}
	        			}
	        		}
        		}
        		try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
        	}
        }
	}

	@Override
    public void onPinchZoomStarted(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent) {
		Debug.i("Pinch zoom started");
        this.mPinchZoomStartedCameraZoomFactor = mCamera.getZoomFactor();
	}

    @Override
    public void onPinchZoom(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent, final float pZoomFactor) {
    	float zoom = mPinchZoomStartedCameraZoomFactor * pZoomFactor;
		Debug.i(""+zoom);
    	if((zoom > mMinZoomFactor)&&(zoom < 2))
			this.mCamera.setZoomFactor(zoom);
    }

    @Override
    public void onPinchZoomFinished(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent, final float pZoomFactor) {
    	float zoom = mPinchZoomStartedCameraZoomFactor * pZoomFactor;
    	if((zoom > mMinZoomFactor)&&(zoom < 2))
    		this.mCamera.setZoomFactor(zoom);
    }
	
	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
        if(this.mPinchZoomDetector != null) {
            this.mPinchZoomDetector.onTouchEvent(pSceneTouchEvent);
	    } 
	
	    return true;
	}


}
