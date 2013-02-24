package com.lortexgames.pokman;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.scene.menu.MenuScene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.sprite.batch.SpriteBatch;
import org.andengine.entity.text.Text;
import org.andengine.entity.util.FPSLogger;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.bitmap.BitmapTexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.adt.io.in.IInputStreamOpener;
import org.andengine.util.debug.Debug; 

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.lortexgames.pokman.R;
import com.purplebrain.giftiz.sdk.GiftizSDK;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Vibrator;
import android.provider.Settings;
import android.provider.Settings.System;
import android.util.Pair;
import android.util.SparseArray;
import android.view.Display;
import android.view.WindowManager;

public class GameActivity extends SimpleBaseGameActivity  implements SensorEventListener {

	private Camera mCamera;
	private static final int HUD_HEIGHT=150;
	
	public static final String WIN = "com.lortexgames.pokman.WIN";
	public static final String SCORE = "com.lortexgames.pokman.SCORE";
	public static final String NVIES = "com.lortexgames.pokman.NVIES";

	final short CATEGORY_PACMAN = 0x0001;
	final short CATEGORY_GHOST =0x0002;
	final short CATEGORY_SCENERY = 0x0004;
    final short CATEGORY_GHOSTEYES = 0x0008;
	
	private int TILE_SIZE = 40;
	private int nRow, nCol;
	private int marginLeft, marginTop;

	private int nVies = 3;
	private int mScore = 0;
	private int mPointValue = 10;

	private int spawnPacX, spawnPacY;
	private int spawnGhostX=-1, spawnGhostY=-1;
	
	private MazeGenerator mazeGen;
	
    private FixedStepPhysicsWorld mPhysicsWorld;

	private TextureRegion mPacmanTextureRegion;
	private TextureRegion mPacmanMangeTextureRegion;

	private boolean update=false;
	
	private Body pacman;
	private HashMap<Body,Sprite> ghosts;
	private Vector<SparseArray<Sprite>> points;
	private Vector<Sprite> bonus_points;
	private TextureRegion mRedGhostTextureRegion;
	private TextureRegion mPinkGhostTextureRegion;
	private TextureRegion mBlueGhostTextureRegion;
	private TextureRegion mOrangeGhostTextureRegion;
	private TextureRegion mPointTextureRegion;
	private TextureRegion mBonusPointTextureRegion;
	private TextureRegion mVulGhost1TextureRegion;
	private TextureRegion mVulGhost2TextureRegion;

	//private boolean paused=false;
	private boolean gameEnded=false;

	private SoundPool soundPool;
	private int idStart,idPoco;
	private int nbKill;
	
	private Sprite pacmanShape;
	
	public TileMapping tileMapper;
	
	// HUD
	private Vector<Sprite> pacLives;
    
	private Text fScoreText=null, vScoreText=null,readyText=null;
	
	
	private boolean isPocoing=false;
	private int mLevel;
	//private float percentageMusic;
	private float percentageGfx;
	private float volume;
	private Scene mScene;
	private ITextureRegion mPopupBackgroundTextureRegion;
	private SensorManager sensorManager;
	private Sensor accelerometer;

	private float calibrationX;
	private float calibrationY;
	
	private int mBonusActivated;
	private TextureRegion mGhostEyesTextureRegion;
	private Text levelText;
	private Rectangle readyTextBackground;
	private TextureRegion mGhostEyesInvTextureRegion;
	private boolean autoCalibration;

	private Text resumeText;
	private Text restartText;
	private Text menuText;
	private FontManager font;
	private FPSLogger fpsLogger;
//	private Text fpsText;
	protected float mGhostGravityScale;
	private float mPacmanGravityScale;
	private float mGhostAttractDivFactor;
	private int mWinPoints;
	
	@Override
	public EngineOptions onCreateEngineOptions() {
		//Debug.i("Creating engine");
		font = new FontManager(this);
		
		Intent intent = getIntent();
		mLevel = intent.getIntExtra(MenuActivity.LEVEL,1);
		mScore = intent.getIntExtra(SCORE,0);
		nVies = intent.getIntExtra(NVIES,3);
		
		
		SharedPreferences settings = getSharedPreferences(MenuActivity.PREFS_NAME, 0);
		//spercentageMusic = settings.getInt("music", 100)/100f;
		percentageGfx = settings.getInt("sfx", 100)/100f;
		autoCalibration = settings.getBoolean("calibration", true);
		autoCalibration = false;
		
		
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		
		calibrationX = -999;
		
		idStart = soundPool.load(this, R.raw.start, 1);
	    idPoco = soundPool.load(this, R.raw.pocopoco, 1);

		soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {

			@Override
			public void onLoadComplete(SoundPool arg0, int soundId, int arg2) {
				AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
				float actualVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
				float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
				final float volume = actualVolume / maxVolume;
				if((soundId == idStart)&&(percentageGfx>0))
					soundPool.play(idStart, percentageGfx*volume, percentageGfx*volume, 1, 0, 1f);

			}
		});

		mCamera = new Camera(0, 0, MenuActivity.SCREENWIDTH, MenuActivity.SCREENHEIGHT);
		
		int gameH, gameL;
		gameH = MenuActivity.SCREENHEIGHT-HUD_HEIGHT;
		gameL =  MenuActivity.SCREENWIDTH;

		nRow = (int) Math.floor(gameH/ ((double) getTileSize()));
		nRow = nRow%2==0 ? nRow - 1 : nRow;
		nCol = (int) Math.floor(gameL  / ((double) getTileSize()));
		nCol = nCol%2==0 ? nCol - 1 : nCol;
		
		marginLeft = (gameL - nCol * (getTileSize())) / 2;
		marginTop = HUD_HEIGHT;

		ghosts = new HashMap<Body,Sprite> ();
		points = new Vector<SparseArray<Sprite>> ();
		bonus_points = new Vector<Sprite> ();
		pacLives = new Vector<Sprite> ();
		
		mazeGen = new MazeGenerator(nCol,nRow,mLevel);
		mazeGen.randomize();
		
		tileMapper = new TileMapping(mazeGen,this);
		EngineOptions engineOptions =  new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(MenuActivity.SCREENWIDTH,MenuActivity.SCREENHEIGHT), mCamera);
		engineOptions.setUpdateThreadPriority(android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);
		
		nbKill=0;
		
		return engineOptions;
	}
	
	@Override
	public void onDestroy() {
	    super.onDestroy();
	}
	
	@Override
	public void onPause() {
		if((!mScene.hasChildScene()) && (!gameEnded))
			mScene.setChildScene(createPopupScene(),false,true,true);
		

		GiftizSDK.onPauseMainActivity(this); 
	    super.onPause();
	}
	
	@Override
	public void onStop() {
		super.onStop();
	}
	
	@Override
	public void onResume() {	
		super.onResume();
		GiftizSDK.onResumeMainActivity(this); 
	}
	
	
	@Override
	protected void onCreateResources() {

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
			
		try {
			ITexture pacmanMangeTexture = new BitmapTexture(this.getTextureManager(),new IInputStreamOpener() { @Override public InputStream open() throws IOException {return getAssets().open("gfx/newgfx/pacman_mange.png");}});
			ITexture redGhostTexture = new BitmapTexture(this.getTextureManager(),new IInputStreamOpener() { @Override public InputStream open() throws IOException {return getAssets().open("gfx/newgfx/monster_red_mange.png");}});
			ITexture blueGhostTexture = new BitmapTexture(this.getTextureManager(),new IInputStreamOpener() { @Override public InputStream open() throws IOException {return getAssets().open("gfx/newgfx/monster_blue_mange.png");}});
			ITexture pinkGhostTexture = new BitmapTexture(this.getTextureManager(),new IInputStreamOpener() { @Override public InputStream open() throws IOException {return getAssets().open("gfx/newgfx/monster_pink_mange.png");}});
			ITexture orangeGhostTexture = new BitmapTexture(this.getTextureManager(),new IInputStreamOpener() { @Override public InputStream open() throws IOException {return getAssets().open("gfx/newgfx/monster_orange_mange.png");}});
			ITexture bonusPointTexture = new BitmapTexture(this.getTextureManager(),new IInputStreamOpener() { @Override public InputStream open() throws IOException {return getAssets().open("gfx/bonus_point.png");}});
			ITexture vulGhost1Texture = new BitmapTexture(this.getTextureManager(),new IInputStreamOpener() { @Override public InputStream open() throws IOException {return getAssets().open("gfx/newgfx/monster_vulnerable.png");}});
			ITexture vulGhost2Texture = new BitmapTexture(this.getTextureManager(),new IInputStreamOpener() { @Override public InputStream open() throws IOException {return getAssets().open("gfx/newgfx/monster_vulnerable2.png");}});
			ITexture ghostEyesTexture = new BitmapTexture(this.getTextureManager(),new IInputStreamOpener() { @Override public InputStream open() throws IOException {return getAssets().open("gfx/newgfx/monster_death.png");}});
			ITexture ghostEyesInvTexture = new BitmapTexture(this.getTextureManager(),new IInputStreamOpener() { @Override public InputStream open() throws IOException {return getAssets().open("gfx/newgfx/monster_death2.png");}});
			ITexture pointTexture = new BitmapTexture(this.getTextureManager(),new IInputStreamOpener() { @Override public InputStream open() throws IOException {return getAssets().open("gfx/point.png");}});
			ITexture pacmanTexture = new BitmapTexture(this.getTextureManager(),new IInputStreamOpener() { @Override public InputStream open() throws IOException {return getAssets().open("gfx/newgfx/pacman.png");}});
			ITexture popupBackgroundTexture = new BitmapTexture(this.getTextureManager(),new IInputStreamOpener() { @Override public InputStream open() throws IOException {return getAssets().open("gfx/bg_popup.png");}});
			
			pacmanMangeTexture.load();
			redGhostTexture.load();
			blueGhostTexture.load();
			pinkGhostTexture.load();
			orangeGhostTexture.load();
			bonusPointTexture.load();
			vulGhost1Texture.load();
			vulGhost2Texture.load();
			ghostEyesTexture.load();
			ghostEyesInvTexture.load();
			pointTexture.load();
			pacmanTexture.load();
			popupBackgroundTexture.load();
			
			mPacmanMangeTextureRegion = TextureRegionFactory.extractFromTexture(pacmanMangeTexture);
			mRedGhostTextureRegion = TextureRegionFactory.extractFromTexture(redGhostTexture);
			mBlueGhostTextureRegion = TextureRegionFactory.extractFromTexture(blueGhostTexture);
			mPinkGhostTextureRegion = TextureRegionFactory.extractFromTexture(pinkGhostTexture);
			mOrangeGhostTextureRegion = TextureRegionFactory.extractFromTexture(orangeGhostTexture);
			mBonusPointTextureRegion = TextureRegionFactory.extractFromTexture(bonusPointTexture);
			mVulGhost1TextureRegion = TextureRegionFactory.extractFromTexture(vulGhost1Texture);
			mVulGhost2TextureRegion = TextureRegionFactory.extractFromTexture(vulGhost2Texture);
			mGhostEyesTextureRegion = TextureRegionFactory.extractFromTexture(ghostEyesTexture);
			mGhostEyesInvTextureRegion = TextureRegionFactory.extractFromTexture(ghostEyesInvTexture);
			mPointTextureRegion = TextureRegionFactory.extractFromTexture(pointTexture);
			mPacmanTextureRegion = TextureRegionFactory.extractFromTexture(pacmanTexture);
			mPopupBackgroundTextureRegion = TextureRegionFactory.extractFromTexture(popupBackgroundTexture);
			
			
		} catch (IOException e1) {
			Debug.e(e1);
		}
		
		font.load(30, Color.WHITE);
		font.load(31, Color.YELLOW);
		font.load(66, Color.WHITE);
		font.load(48, Color.WHITE);
		
		try {
			tileMapper.loadTextures();
		} catch (IOException e) {
			Debug.e(e);
		}
		
		getLevelValues();
	}

	private void getLevelValues() {
		LevelManager lvl = new LevelManager();
		LevelSet values = lvl.get(mLevel);
		this.mGhostAttractDivFactor=values.ghostAttractDivFactor;
		this.mGhostGravityScale=values.ghostGravityScale;
		this.mPacmanGravityScale=values.pacmanGravityScale;
		this.mPointValue=values.pointValue;
		this.mWinPoints=values.winPoints;
	}

	@Override
	protected Scene onCreateScene() {
		AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		float actualVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		volume = actualVolume / maxVolume;
		
		MediaPlayer audio = MediaPlayer.create(getApplicationContext(), R.raw.start);
		int duration = audio.getDuration() + 100;
		audio.release();

		
		new Timer().schedule(new TimerTask() {

			@Override
			public void run() {
				Iterator<Body> it=ghosts.keySet().iterator(); 
				while(it.hasNext()) 
					it.next().setGravityScale(mGhostGravityScale);
				
				readyText.setAlpha(0f);
				readyTextBackground.setAlpha(0f);
				pacman.setGravityScale(1f);

				/*if((percentageMusic>0)&&(!paused))
					soundPool.play(idSiren, percentageMusic*volume, percentageMusic*volume, 1, -1, 1f);
				*/
				
				update=true;
				new Timer().schedule(new TimerTask() {

					@Override
					public void run() {
						levelText.setText("LEVEL "+mLevel);
						levelText.setX((float) ((fScoreText.getWidth() + fScoreText.getX()) + ((MenuActivity.SCREENWIDTH - fScoreText.getWidth() - fScoreText.getX())/2.0) - levelText.getWidth()/2.0));
						levelText.setAlpha(1f);
					}
				}, 1000);
			}
		}, duration);
		
		
		mScene = new Scene();
		fpsLogger = new FPSLogger();
		mScene.registerUpdateHandler(fpsLogger);
		
		mScene.setBackground(new Background(0f,0f,0f));
		
		this.mPhysicsWorld  = new FixedStepPhysicsWorld(24, 1, new Vector2(0f, 9.8f), false, 8, 1);
		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(1, 0f, 1f);
		wallFixtureDef.filter.categoryBits = CATEGORY_SCENERY;
		wallFixtureDef.filter.maskBits = -1;
		
		int nbGhosts = 0;
		
		SpriteBatch sprWall = new SpriteBatch(tileMapper.getTextureAtlas(),mazeGen.getCountWalls(),this.getVertexBufferObjectManager());

		for(int y=0;y<nRow;y++) {
			for(int x=0;x<nCol;x++) {
				points.add(new SparseArray<Sprite>());
				
				if(mazeGen.value(x, y) == Element.POINT) {
					addPoint(mScene,getMarginLeft() + x*(getTileSize()) + getTileSize()/2, getMarginTop()+y*(getTileSize()) + getTileSize()/2);
				} else if(mazeGen.value(x, y) == Element.BONUS) {
					addBonusPoint(mScene,getMarginLeft() + x*(getTileSize()) + getTileSize()/2, getMarginTop()+y*(getTileSize()) + getTileSize()/2);
				} else if(mazeGen.value(x, y) == Element.SPAWNGHOST){
					if(spawnGhostX == -1)
						spawnGhostX = x;
					
					if(spawnGhostY == -1)
						spawnGhostY = y;
					
					
					nbGhosts++;
					TextureRegion selTexture = mRedGhostTextureRegion;
					switch(nbGhosts%5) {
					case 2:
						selTexture = mPinkGhostTextureRegion;
						break;
					case 3:
						selTexture = mBlueGhostTextureRegion;
						break;
					case 4:
						selTexture = mOrangeGhostTextureRegion;
						break;
					}
					
					Sprite ghostSprite = new Sprite(getMarginLeft()+x*getTileSize(),getMarginTop()+ y*getTileSize(),selTexture,this.getVertexBufferObjectManager());
					ghostSprite.setZIndex(42);
					ghosts.put(createGhostBody(mScene,ghostSprite), ghostSprite);
					
				} else if(mazeGen.value(x, y) == Element.SPAWNPAC){
					Rectangle pacspwn = new Rectangle(getMarginLeft() + x*TILE_SIZE, getMarginTop()+y*TILE_SIZE,TILE_SIZE, TILE_SIZE,this.getVertexBufferObjectManager());
					pacspwn.setColor(0.1f,0.1f,0.1f);
					mScene.attachChild(pacspwn);

					pacman = createPacman(mScene,x,y);
					pacman.setGravityScale(0f);
					
					spawnPacX = x;
					spawnPacY = y;
				} else {
					Sprite wall = tileMapper.getWallSprite(x, y, mScene);
					PhysicsFactory.createBoxBody(this.mPhysicsWorld, wall, BodyType.StaticBody, wallFixtureDef);
					sprWall.draw(wall);
					//mScene.attachChild(wall);
				}
			}
		}
		sprWall.submit();
		mScene.attachChild(sprWall);
		manageHUDUI(mScene);
		
		this.mPhysicsWorld.setContactListener(new ContactListener() {
            @Override
            public void beginContact(final Contact pContact) {}
            @Override
            public void endContact(final Contact pContact) {}
			@Override
			public void preSolve(Contact contact, Manifold oldManifold) {}

			@Override
			public void postSolve(Contact contact, ContactImpulse impulse) {
				Body bodyA = contact.getFixtureA().getBody();
				Body bodyB = contact.getFixtureB().getBody();
				
				if((bodyA == pacman)||(bodyB == pacman)) {
					final Body other = (bodyA == pacman) ? bodyB : bodyA;
					
					if(ghosts.containsKey(other)) {
						GameActivity.this.runOnUpdateThread(new Runnable() {

							@Override
						    public void run() {
						    	if((ghosts.get(other).getTextureRegion() == mVulGhost1TextureRegion)||(ghosts.get(other).getTextureRegion() == mVulGhost2TextureRegion)){ // A ghost is killed

						    		final Body target = other;
						    		final Sprite targetSprite = ghosts.get(other);
						    		targetSprite.setTextureRegion(mGhostEyesTextureRegion);
						    		Filter newFilter = new Filter();
						    		newFilter.categoryBits = CATEGORY_GHOSTEYES;
						    		newFilter.maskBits= CATEGORY_SCENERY | CATEGORY_GHOSTEYES;
						    		target.getFixtureList().get(0).setFilterData(newFilter);
						    		target.setGravityScale(0.5f);
						    		nbKill++;
						    		mScore += 200*nbKill;
						    		
						    		new Timer().schedule(new TimerTask() {
										@Override
										public void run() {
								    		Filter newFilter = new Filter();
								    		newFilter.categoryBits = CATEGORY_GHOST;
								    		newFilter.maskBits= CATEGORY_SCENERY | CATEGORY_PACMAN | CATEGORY_GHOST;
								    		target.getFixtureList().get(0).setFilterData(newFilter);
								    		target.setGravityScale(0.9f);
											resetGhostColor(target); // mGhostEyesInvTextureRegion
										}
						    		}, 10000);
						    		
						    		new Timer().schedule(new TimerTask() {
										@Override
										public void run() {
											targetSprite.setTextureRegion(mGhostEyesInvTextureRegion);
										}
						    		}, 8000);
						    		
						    		new Timer().schedule(new TimerTask() {
										@Override
										public void run() {
											targetSprite.setTextureRegion(mGhostEyesTextureRegion);
										}
						    		}, 8500);
						    		
						    		new Timer().schedule(new TimerTask() {
										@Override
										public void run() {
											targetSprite.setTextureRegion(mGhostEyesInvTextureRegion);
										}
						    		}, 9000);
						    		
						    		new Timer().schedule(new TimerTask() {
										@Override
										public void run() {
											targetSprite.setTextureRegion(mGhostEyesTextureRegion);
										}
						    		}, 9500);
						    	} else if(ghosts.get(other).getTextureRegion() != mGhostEyesTextureRegion){ // Pacman is killed by a non-dead ghost
									nVies--;
									pacmanShape.detachSelf();
									other.getWorld().destroyBody(pacman);
									
									if(nVies > 0) {
										manageHUDUI(mScene);
										createPacman(mScene,spawnPacX,spawnPacY);
										Vector<Body> toRemove = new Vector<Body>();
										Vector<Pair<Body,Sprite>>toAdd = new Vector<Pair<Body,Sprite>>();
										Iterator<Body> it=ghosts.keySet().iterator(); // on crée un Iterator pour parcourir notre HashSet
										while(it.hasNext()) // tant qu'on a un suivant
										{
											Body body = it.next();
											Sprite ghost = ghosts.get(body);
											int ghostX = (int) ((ghost.getX() - marginLeft)/TILE_SIZE);
											int ghostY = (int) ((ghost.getY() - marginTop)/TILE_SIZE);
	
											
											if(((ghostX >= spawnPacX - 1)&&(ghostX <= spawnPacX + 1)&&(ghostY >= spawnPacY -1)&&(ghostY <= spawnPacY+1))&&(ghost.getTextureRegion() != mGhostEyesTextureRegion)&&(ghost.getTextureRegion() != mGhostEyesInvTextureRegion)) {
												Sprite ghostSprite = new Sprite(getMarginLeft()+spawnGhostX*getTileSize(),getMarginTop()+ spawnGhostY*getTileSize(),ghost.getTextureRegion(),GameActivity.this.getVertexBufferObjectManager());
												ghostSprite.setZIndex(42);
												toAdd.add(new Pair<Body,Sprite>(createGhostBody(mScene,ghostSprite), ghostSprite));
												
												ghost.detachSelf();
												toRemove.add(body);
												mPhysicsWorld.destroyBody(body);
											}
										}
										
										for(int i=0;i<toRemove.size();i++) 
											ghosts.remove(toRemove.get(i));
	
										for(int i=0;i<toAdd.size();i++) 
											ghosts.put(toAdd.get(i).first, toAdd.get(i).second);
										
									} else {
										soundPool.release();
										gameEnded=true;
							        	Intent intent = new Intent(GameActivity.this, EndGameActivity.class);
							        	intent.putExtra(WIN, false);
							        	intent.putExtra(SCORE, mScore);
							        	
							        	intent.putExtra(MenuActivity.LEVEL, mLevel);
							        	intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
							        	
							        	
					                    
										startActivity(intent);
										finish();
									}
							    }
						    }
						});
					}
				}
			}
		});
		SharedPreferences settings = this.getSharedPreferences(MenuActivity.PREFS_NAME, 0);
    	if(settings.getInt("giftizMissionStatus", 0)==1) {// First victory 
    		GiftizSDK.missionComplete(this);
    		Editor editor = settings.edit();
    		editor.putInt("giftizMissionStatus", 2);
    		editor.commit();
    	}
		
		/*fpsText = new Text(0,10,font.get(30, Color.WHITE),String.format("%03d", 0),getVertexBufferObjectManager());
		fpsText.setX(MenuActivity.SCREENWIDTH - fpsText.getWidth() - 15);
		mScene.attachChild(fpsText);*/
		
		mScene.registerUpdateHandler(this.mPhysicsWorld);
		this.getEngine().registerUpdateHandler(new IUpdateHandler() {
            @Override
            public void reset() { }

            @Override
            public void onUpdate(final float pSecondsElapsed) {
            	/*if(fpsText != null) {
        			fpsText.setText(String.format("%03d", (int)fpsLogger.getFPS()));
        			fpsLogger.reset();
        		}*/
            	
            	tickLoop();
            }
		});
		mScene.sortChildren();
		return mScene;
	}
	@Override
	public void onResumeGame() {
		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
		super.onResumeGame();
	}
	
	@Override
	public void onPauseGame() {
		sensorManager.unregisterListener(this, accelerometer);
		super.onPauseGame();
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
			int orientation = display.getRotation();
			
			Vector2 gravity = new Vector2();
			
			final float rawValues[] = {event.values[0],event.values[1]};
			final float realValues[] = {0,0};
			canonicalToScreenOrientation(orientation,rawValues,realValues);

			float x = realValues[0];
			float y = realValues[1];
			
			if(calibrationX == -999) {
				calibrationX = x;
				calibrationY = y;
			}
			if(autoCalibration)
				gravity.set(calibrationX-x, y-calibrationY);
			else
				gravity.set(-x, y);
			
			gravity.mul(5);
			gravity.mul(mPacmanGravityScale);
			
			if(update)
				this.mPhysicsWorld.setGravity(gravity);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {}
	
	private Body createPacman(Scene scene, int x, int y) {
		final FixtureDef objectFixtureDef = PhysicsFactory.createFixtureDef(1f, 0, 0.2f);
		objectFixtureDef.filter.categoryBits= CATEGORY_PACMAN;
		objectFixtureDef.filter.maskBits= CATEGORY_GHOST | CATEGORY_SCENERY;
		
		Sprite pacmanSprite = new Sprite(getMarginLeft()+x*getTileSize(), getMarginTop()+y*getTileSize(),this.mPacmanTextureRegion,this.getVertexBufferObjectManager());
		pacmanSprite.setZIndex(42);
		pacmanShape = pacmanSprite;
		final Body body = PhysicsFactory.createCircleBody(this.mPhysicsWorld, pacmanSprite, BodyType.DynamicBody, objectFixtureDef);
		
        this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(pacmanSprite, body, true, true));
        
        
        pacmanSprite.setUserData(body);
        body.setGravityScale(1f);
        
        scene.registerTouchArea(pacmanSprite);
		scene.attachChild(pacmanSprite);
		
		return body;
	}
	
	private Body createGhostBody(Scene scene,Sprite ghostSprite) {
		final FixtureDef ghostFixtureDef = PhysicsFactory.createFixtureDef(1f, 0, 0.1f);
		ghostFixtureDef.filter.categoryBits = CATEGORY_GHOST;
		ghostFixtureDef.filter.maskBits= CATEGORY_GHOST | CATEGORY_PACMAN | CATEGORY_SCENERY;
		 
		Body body = PhysicsFactory.createCircleBody(this.mPhysicsWorld, ghostSprite, BodyType.DynamicBody, ghostFixtureDef);
		
        this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(ghostSprite, body, true, true));
        
        
        ghostSprite.setUserData(body);
        
        body.setGravityScale(0f);
        
        scene.registerTouchArea(ghostSprite);
		scene.attachChild(ghostSprite);
		
		return body;
	}
	
	private void addPoint(Scene scene,int x, int y) {
		/*float pX = x-mPointTextureRegion.getWidth()/2;
		float pY = y-mPointTextureRegion.getHeight()/2;
		pointSpriteBatch.draw((ITextureRegion)mPointTextureRegion, pX, pY, mPointTextureRegion.getWidth(), mPointTextureRegion.getHeight(),ColorUtils.convertRGBAToARGBPackedFloat(1f, 1f, 1f, 0f));
		*/
		Sprite point = new Sprite(x-mPointTextureRegion.getWidth()/2, y-mPointTextureRegion.getHeight()/2,this.mPointTextureRegion,this.getVertexBufferObjectManager());
		point.setZIndex(0);
		points.get((x-marginLeft)/TILE_SIZE).put((y-marginTop)/TILE_SIZE, point);
		
		scene.attachChild(point);
	}
	
	private void addBonusPoint(Scene scene,int x, int y) {
		Sprite point = new Sprite(x-mBonusPointTextureRegion.getWidth()/2, y-mBonusPointTextureRegion.getHeight()/2,this.mBonusPointTextureRegion,this.getVertexBufferObjectManager());
		point.setZIndex(0);
		
		bonus_points.add(point);
        scene.registerTouchArea(point);
		scene.attachChild(point);
	}
	
	private void manageHUDUI(Scene scene) {
		// Pacman lives
		if(pacLives.size() < nVies) {
			for(int i=pacLives.size(); i<nVies;i++) {
				float y=0;
				if(nVies <= 4)
					y=HUD_HEIGHT/2-mPacmanTextureRegion.getHeight()/2;
				else
					y=HUD_HEIGHT/2-mPacmanTextureRegion.getHeight()-5;
				
				float x=30+i*(mPacmanTextureRegion.getWidth()+10);
				if(i>3) {
					y+= mPacmanTextureRegion.getHeight() + 10;
					x-=4*(mPacmanTextureRegion.getWidth()+10);
				}
				
				Sprite liveIcon = new Sprite(x,y,mPacmanTextureRegion,getVertexBufferObjectManager());
				scene.attachChild(liveIcon);
				pacLives.add(liveIcon);
			}
		} else if(pacLives.size() > nVies) {
			while (pacLives.size() > nVies) {
				Sprite spr = pacLives.lastElement();
				spr.detachSelf();
				pacLives.remove(pacLives.size()-1);
			}
		}
		
		// Score screen
		
		if(fScoreText == null) {
			fScoreText = new Text(100+3*(mPacmanTextureRegion.getWidth()+10),50,font.get(30, Color.WHITE),"SCORE",getVertexBufferObjectManager());
			scene.attachChild(fScoreText);
		}
		
		if(vScoreText == null) {
			vScoreText = new Text(100+3*(mPacmanTextureRegion.getWidth()+10),80,font.get(30, Color.WHITE),String.format("%05d", mScore),6,getVertexBufferObjectManager());
			scene.attachChild(vScoreText);
		} else {
			vScoreText.setText(String.format("%05d", mScore));
		}
		
		
		
		//Ready text
		if(levelText == null) {
			levelText = new Text(0,65,this.font.get(31, Color.YELLOW),"LEVEL 999",getVertexBufferObjectManager());
			levelText.setX((float) ((fScoreText.getWidth() + fScoreText.getX()) + ((MenuActivity.SCREENWIDTH - fScoreText.getWidth() - fScoreText.getX())/2.0) - levelText.getWidth()/2.0));
			levelText.setAlpha(0f);
			scene.attachChild(levelText);

			
			readyText = new Text(0,0,font.get(66, Color.WHITE),"GET READY",getVertexBufferObjectManager());
			readyText.setColor(1f,1f,0f);
			readyText.setX(MenuActivity.SCREENWIDTH/2f - readyText.getWidth()/2f);
			readyText.setY(MenuActivity.SCREENHEIGHT/2f - readyText.getHeight()/2f);
			readyText.setAlpha(0f);
			readyText.setZIndex(1500);
			
			readyTextBackground = new Rectangle(readyText.getX()-15,readyText.getY()-15,readyText.getWidth()+30,readyText.getHeight()+30,getVertexBufferObjectManager());
			readyTextBackground.setAlpha(0f);
			readyTextBackground.setColor(0f,0f,0f);
			readyTextBackground.setZIndex(1000);
			scene.attachChild(readyTextBackground);
			scene.attachChild(readyText);
			scene.sortChildren();
			new Timer().schedule(new TimerTask() {
				@Override
				public void run() {
					readyText.setAlpha(1.0f);
					readyTextBackground.setAlpha(1.0f);
				}
			}, 500);
		}
	}

	public int getTileSize() {
		return TILE_SIZE;
	}

	public int getMarginLeft() {
		return marginLeft;
	}

	public int getMarginTop() {
		return marginTop;
	}
	
	public void tickLoop() {
		if(!update)
			return;
		
		Iterator<Body> it=ghosts.keySet().iterator(); // on crée un Iterator pour parcourir notre HashSet
		while(it.hasNext()) // tant qu'on a un suivant
		{
			Body ghost = it.next();
			Vector2 force = new Vector2();
			float forceX = pacman.getPosition().x - ghost.getPosition().x;
			float forceY = pacman.getPosition().y - ghost.getPosition().y;
			if((ghosts.get(ghost).getTextureRegion() == mVulGhost1TextureRegion)||(ghosts.get(ghost).getTextureRegion()==mVulGhost2TextureRegion)||(ghosts.get(ghost).getTextureRegion()==mGhostEyesTextureRegion)) {
				/*if(forceX > 15) TODO: Bosser la dessus
					forceX=15;
				
				if(forceY > 15)
					forceY=15;
				force.set(-15+forceX,-15+forceY);
				force.div(4);*/
				force.set(0,0);
			} else {
				force.set(forceX, forceY);
				force.div(mGhostAttractDivFactor);
			}
			
			ghost.applyForceToCenter(force);
		}
		
		// -----------------------------
		for(int i=0;i<bonus_points.size();i++) {
			if(bonus_points.get(i).collidesWith(pacmanShape)) {
    			mScore += 42;
    			
				bonus_points.get(i).detachSelf();
				bonus_points.remove(i);
				this.mBonusActivated++;
				
				blinkLoop(false);
				
				new Timer().schedule(new TimerTask(){
					@Override
					public void run() {
						mBonusActivated--;
						if(mBonusActivated == 0) {
							resetGhostsColor();
							nbKill=0;
						}
					}
				}, 10000);
				
				new Timer().schedule(new TimerTask(){
					@Override
					public void run() {
						blinkLoop(true);
					}
				}, 4000);
			}
		}
		
		final int pacManX = (int) ((pacmanShape.getX() - marginLeft + pacmanShape.getWidth()/2f)/TILE_SIZE);
    	final int pacManY = (int) ((pacmanShape.getY() - marginTop + pacmanShape.getHeight()/2f)/TILE_SIZE);
    	
    	if(points.get(pacManX).get(pacManY) != null) {
    		if(points.get(pacManX).get(pacManY).collidesWith(pacmanShape)) {
    			points.get(pacManX).get(pacManY).detachSelf();
    			points.get(pacManX).remove(pacManY);
    			
    			mScore += mPointValue;
    			if(!isPocoing) {
    				isPocoing = true;
        			soundPool.play(idPoco, percentageGfx*volume, percentageGfx*volume, 1, 0, 1f);

        			pacmanShape.setTextureRegion(mPacmanMangeTextureRegion);
        			
    				new Timer().schedule(new TimerTask(){
						@Override
						public void run() {
							isPocoing=false;
						}
    				}, 80);
    				
    				new Timer().schedule(new TimerTask(){
						@Override
						public void run() {
		        			pacmanShape.setTextureRegion(mPacmanTextureRegion);
						}
    				}, 150);
    			}
    			
    			manageHUDUI(mScene);
    			boolean flag_allempty = true;
    			for(int i=0;i<points.size();i++)
    				if(points.get(i).size() > 0)
    					flag_allempty = false;
    				
    			if(flag_allempty) {
					gameEnded=true;
		        	SharedPreferences settings = getSharedPreferences(MenuActivity.PREFS_NAME, 0);
		        	if(settings.getInt("giftizMissionStatus", 0)==0) {// First victory 
		        		Editor editor = settings.edit();
		        		editor.putInt("giftizMissionStatus", 1);
		        		editor.commit();
		        	}

		        	mScore += mWinPoints;
		        	
					if(mLevel+1<=LevelManager.MAX_LEVEL) {
			        	Intent intent = new Intent(GameActivity.this, GameActivity.class);
			        	intent.putExtra(MenuActivity.LEVEL, mLevel+1);
			        	intent.putExtra(SCORE, mScore);
			        	Editor editor = settings.edit();
	                    int maxLevel = settings.getInt("maxLevel", 1);
			        	if(maxLevel < mLevel +1) {
			        		editor.putInt("maxLevel", mLevel+1);
			        		editor.commit();
				        	this.toastOnUIThread("LEVEL UP");
			        	}
			        	
			        	if(nVies+1<=8)
			        		intent.putExtra(NVIES, nVies+1);
			        		
						startActivityForResult(intent, 1);
	    				finish();
					} else {
			        	Intent intent = new Intent(GameActivity.this, EndGameActivity.class);
			        	intent.putExtra(MenuActivity.LEVEL, mLevel);
			        	intent.putExtra(WIN, true);
			        	intent.putExtra(SCORE, mScore);
						startActivityForResult(intent, 1);
	    				finish();
					}
    			}
    		}
        }
	}
	protected void resetGhostsColor() {
		Iterator<Body> it=ghosts.keySet().iterator(); // on crée un Iterator pour parcourir notre HashSet
		int nGhost = 0;
		while(it.hasNext()) // tant qu'on a un suivant
		{
			nGhost ++;
			Body ghost = it.next();
			Sprite ghostSprite = ghosts.get(ghost);
			
			TextureRegion selTexture = mRedGhostTextureRegion;
			switch(nGhost%5) {
			case 2:
				selTexture = mPinkGhostTextureRegion;
				break;
			case 3:
				selTexture = mBlueGhostTextureRegion;
				break;
			case 4:
				selTexture = mOrangeGhostTextureRegion;
				break;
			}
			
			if(ghostSprite.getTextureRegion() != mGhostEyesTextureRegion)
				ghostSprite.setTextureRegion(selTexture);
		}
	}
	
	protected void resetGhostColor(Body target) {
		Iterator<Body> it=ghosts.keySet().iterator(); // on crée un Iterator pour parcourir notre HashSet
		int nGhost = 0;
		while(it.hasNext()) // tant qu'on a un suivant
		{
			nGhost ++;
			Body ghost = it.next();
			if(target == ghost) {
				Sprite ghostSprite = ghosts.get(ghost);
				
				TextureRegion selTexture = mRedGhostTextureRegion;
				switch(nGhost%5) {
				case 2:
					selTexture = mPinkGhostTextureRegion;
					break;
				case 3:
					selTexture = mBlueGhostTextureRegion;
					break;
				case 4:
					selTexture = mOrangeGhostTextureRegion;
					break;
				}
				
				ghostSprite.setTextureRegion(selTexture);
			}
		}
	}
	

	protected void blinkLoop(final boolean loop) {
		if((mBonusActivated == 0)||(mBonusActivated > 1))
			return;
		
	/*	this.runOnUpdateThread(new Runnable() {
			@Override
			public void run() {*/
				ITextureRegion selTexture = null;
				
				Iterator<Body> it=ghosts.keySet().iterator();

				while(it.hasNext()){
					Body ghost = it.next();
					Sprite ghostSprite = ghosts.get(ghost);
					if(!loop) {
						selTexture = mVulGhost1TextureRegion;
					} else {
						if(ghostSprite.getTextureRegion() == mVulGhost1TextureRegion)
							selTexture =  mVulGhost2TextureRegion;
						else if(ghostSprite.getTextureRegion() == mVulGhost2TextureRegion)
							selTexture = mVulGhost1TextureRegion;
						else 
							selTexture = ghostSprite.getTextureRegion();
					}
					if(ghostSprite.getTextureRegion() != mGhostEyesTextureRegion)
						ghostSprite.setTextureRegion(selTexture);
				}
			/*}
		});*/
		
		if(loop) {
			new Timer().schedule(new TimerTask(){
				@Override
				public void run() {
	    			blinkLoop(true);
				}
			}, 1000);
		}
	}

	public void onBackPressed() {
		if(!mScene.hasChildScene()) 
			mScene.setChildScene(createPopupScene(),false,true,true);
	}
	

	public MenuScene createPopupScene() {
		final boolean hapticFeedback = System.getInt(this.getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED, 0) != 0;
		
		soundPool.autoPause();
		MenuScene popup = new MenuScene(mCamera);
		popup.setBackgroundEnabled(false);
		int sceneWidth = (int) (mPopupBackgroundTextureRegion.getWidth());
		int sceneHeight = (int) (mPopupBackgroundTextureRegion.getHeight());
		
		int baseX = (int) (MenuActivity.SCREENWIDTH/2f - sceneWidth/2f);
		int baseY = (int) (MenuActivity.SCREENHEIGHT/2f - sceneHeight/2f);
		Rectangle blackBG = new Rectangle(0,0,MenuActivity.SCREENWIDTH,MenuActivity.SCREENHEIGHT,this.getVertexBufferObjectManager());
		blackBG.setColor(0,0,0);
		blackBG.setAlpha(0.7f);
		popup.attachChild(blackBG);
		
		Sprite bg = new Sprite(baseX,baseY,mPopupBackgroundTextureRegion,this.getVertexBufferObjectManager());
		bg.setAlpha(0.95f);
		popup.attachChild(bg);
		
		Text pauseText = new Text(0,baseY+100,font.get(66, Color.WHITE),"PAUSE",this.getVertexBufferObjectManager());
		pauseText.setX(baseX + (sceneWidth/2f - pauseText.getWidth()/2f));
		popup.attachChild(pauseText);
		
		resumeText = new Text(0,baseY+290,font.get(48, Color.WHITE),"RESUME",this.getVertexBufferObjectManager()) {
			@Override
		    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
				if ((pSceneTouchEvent.isActionDown())||(pSceneTouchEvent.isActionMove()))
		        	this.setColor(1f,1f,0f);
		        else if(pSceneTouchEvent.isActionUp()) {
		        	if(hapticFeedback) {
			        	Vibrator v = (Vibrator) GameActivity.this.getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
			        	v.vibrate(100);
		        	}

					calibrationX = -999;
					soundPool.autoResume();
			        mScene.back();
		        	this.setColor(1f,1f,1f);
		        }
		        return true;
		    }
		};
		resumeText.setX(baseX + (sceneWidth/2f - resumeText.getWidth()/2f));
		popup.attachChild(resumeText);
		popup.registerTouchArea(resumeText);
		
		restartText = new Text(0,baseY+450,font.get(48, Color.WHITE),"RESTART",this.getVertexBufferObjectManager()) {
			@Override
		    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
				if ((pSceneTouchEvent.isActionDown())||(pSceneTouchEvent.isActionMove()))
		        	this.setColor(1f,1f,0f);
		        else if(pSceneTouchEvent.isActionUp()) {
		        	if(hapticFeedback) {
			        	Vibrator v = (Vibrator) GameActivity.this.getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
			        	v.vibrate(100);
		        	}
		        	
					Intent intent = new Intent(GameActivity.this, GameActivity.class);
		    		final SharedPreferences settings = getSharedPreferences(MenuActivity.PREFS_NAME, 0);
		        	intent.putExtra(MenuActivity.LEVEL, settings.getInt("startLevel", 1));
			    	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					finish();
		        	this.setColor(1f,1f,1f);
		        }
		        return true;
		    }
		};
		restartText.setX(baseX + (sceneWidth/2f - restartText.getWidth()/2f));
		popup.attachChild(restartText);
		popup.registerTouchArea(restartText);
		
		menuText = new Text(0,baseY+610,font.get(48, Color.WHITE),"MENU",this.getVertexBufferObjectManager()){
			@Override
		    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {		
				if ((pSceneTouchEvent.isActionDown())||(pSceneTouchEvent.isActionMove()))
		        	this.setColor(1f,1f,0f);
		        else if(pSceneTouchEvent.isActionUp()) {
		        	if(hapticFeedback) {
			        	Vibrator v = (Vibrator) GameActivity.this.getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
			        	v.vibrate(100);
		        	}
		        	
		        	Intent intent = new Intent(GameActivity.this, MenuActivity.class);
			    	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivityForResult(intent, 1);
					finish();
		        	this.setColor(1f,1f,1f);
		        }
		        return true;
		    }
		};
		menuText.setX(baseX + (sceneWidth/2f - menuText.getWidth()/2f));
		popup.attachChild(menuText);

		popup.registerTouchArea(menuText);
		popup.setOnSceneTouchListener(new IOnSceneTouchListener() {
			@Override
			public boolean onSceneTouchEvent(Scene pScene,
					TouchEvent pSceneTouchEvent) {
				menuText.setColor(1f,1f,1f);
				restartText.setColor(1f,1f,1f);
				resumeText.setColor(1f,1f,1f);
				return false;
			}
		});
		return popup;
	}
	
	static void canonicalToScreenOrientation(int displayRotation, float[] canVec, float[] screenVec) 
	{ 
	    final int axisSwap[][] = { 
	    {  1,  1,  0,  1  },     // ROTATION_0 
	    {-1,  1,  1,  0  },     // ROTATION_90 
	    {-1,    -1,  0,  1  },     // ROTATION_180 
	    {  1,    -1,  1,  0  }  }; // ROTATION_270 

	    final int[] as = axisSwap[displayRotation]; 
	    screenVec[0]  =  (float)as[0] * canVec[ as[2] ]; 
	    screenVec[1]  =  (float)as[1] * canVec[ as[3] ]; 
	} 
}


