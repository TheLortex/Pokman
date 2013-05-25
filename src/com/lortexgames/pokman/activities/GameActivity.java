package com.lortexgames.pokman.activities;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.andengine.engine.Engine;
import org.andengine.engine.camera.ZoomCamera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.extension.input.touch.controller.MultiTouch;
import org.andengine.extension.input.touch.controller.MultiTouchController;
import org.andengine.extension.input.touch.detector.PinchZoomDetector;
import org.andengine.extension.input.touch.detector.PinchZoomDetector.IPinchZoomDetectorListener;
import org.andengine.extension.input.touch.exception.MultiTouchException;
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
import org.andengine.entity.sprite.batch.SpriteGroup;
import org.andengine.entity.text.Text;
import org.andengine.entity.util.FPSLogger;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
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
import com.lortexgames.pokman.Element;
import com.lortexgames.pokman.FontManager;
import com.lortexgames.pokman.LevelManager;
import com.lortexgames.pokman.LevelSet;
import com.lortexgames.pokman.MazeGenerator;
import com.lortexgames.pokman.R;
import com.lortexgames.pokman.TileMapping;
import com.lortexgames.pokman.R.raw;
import com.lortexgames.pokman.R.string;
import com.lortexgames.pokman.addons.MaxStepPhysicsWorld;
import com.lortexgames.pokman.handlers.EntityFollowerHandler;
import com.lortexgames.pokman.handlers.PlayerHandler;

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

public class GameActivity extends SimpleBaseGameActivity  implements SensorEventListener, IPinchZoomDetectorListener, IOnSceneTouchListener {

	private static int[] as;
	
	private ZoomCamera mCamera;
	public static final int HUD_HEIGHT=150;
	
	public static final String WIN = "com.lortexgames.pokman.WIN";
	public static final String SCORE = "com.lortexgames.pokman.SCORE";
	public static final String NVIES = "com.lortexgames.pokman.NVIES";
	

	public static final int axisSwap[][] = { 
    {  1,  1,  0,  1  },     // ROTATION_0 
    {-1,  1,  1,  0  },     // ROTATION_90 
    {-1,    -1,  0,  1  },     // ROTATION_180 
    {  1,    -1,  1,  0  }  }; // ROTATION_270 

	final short CATEGORY_PACMAN = 0x0001;
	final short CATEGORY_GHOST =0x0002;
	final short CATEGORY_SCENERY = 0x0004;
    final short CATEGORY_GHOSTEYES = 0x0008;
    
    final boolean SHOW_FPS = true;
	
	private int TILE_SIZE = 40;
	private int nRow, nCol;
	private int marginLeft, marginTop;

	private int nVies = 3;
	private int mScore = 0;
	private int mPointValue = 10;

	private int spawnPacX, spawnPacY;
	private int spawnGhostX=-1, spawnGhostY=-1;
	
	private MazeGenerator mazeGen;
	
    //private FixedStepPhysicsWorld mPhysicsWorld;
	private MaxStepPhysicsWorld mPhysicsWorld;
	
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
	private Sprite readyTextBackground;
	private Sprite startArrowSprite=null;
	
	private TextureRegion mGhostEyesInvTextureRegion;
	private boolean autoCalibration;

	private Text resumeText;
	private Text restartText;
	private Text menuText;
	private FontManager font;
	private FPSLogger fpsLogger;
	private Text fpsText;
	protected float mGhostGravityScale;
	private float mPacmanGravityScale;
	private float mGhostAttractDivFactor;
	private int mWinPoints;
	private TextureRegion mStartTextBackgroundTextureRegion;
	
	private float[] rawValues = {0,0};
	private float[] realValues= {0,0};

	private Vector2 gravity;
	private Vector2 force;

	private int mVulDuration;

	private int mBlinkTime;

	private PointCollisionThread pointCollisionDetector;

	private PinchZoomDetector mPinchZoomDetector;
	private float mPinchZoomStartedCameraZoomFactor;

	private HUD mHud;

	private SpriteGroup sprPoints;
	private EntityFollowerHandler efh;
	
	
	@Override
	public EngineOptions onCreateEngineOptions() {
		//Debug.i("Creating engine");
		font = new FontManager(this);
		
		Intent intent = getIntent();
		mLevel = intent.getIntExtra(MenuActivity.LEVEL,1);
		mScore = intent.getIntExtra(SCORE,0);
		nVies = intent.getIntExtra(NVIES,3);

		gravity = new Vector2();
		force = new Vector2();
		
		SharedPreferences settings = getSharedPreferences(MenuActivity.PREFS_NAME, 0);
		//spercentageMusic = settings.getInt("music", 100)/100f;
		percentageGfx = settings.getInt("sfx", 100)/100f;
		autoCalibration = settings.getBoolean("calibration", true);
		autoCalibration = false;
		
		
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		

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

		mCamera = new ZoomCamera(0, 0, MenuActivity.getWidth(), MenuActivity.getHeight());
		
		int gameH, gameL;
		gameH = MenuActivity.getHeight()-HUD_HEIGHT;
		gameL =  MenuActivity.getWidth();

		nRow = (int) Math.floor(gameH/ ((double) TILE_SIZE));
		nRow = nRow%2==0 ? nRow - 1 : nRow;
		nCol = (int) Math.floor(gameL  / ((double) TILE_SIZE));
		nCol = nCol%2==0 ? nCol - 1 : nCol;
		
		nRow = 25;
		nCol = 15;
		
		//marginLeft = (gameL - nCol * (TILE_SIZE)) / 2;
		//marginTop = HUD_HEIGHT;
		marginLeft = 0;
		marginTop = 0;
		
		ghosts = new HashMap<Body,Sprite> ();
		points = new Vector<SparseArray<Sprite>> ();
		bonus_points = new Vector<Sprite> ();
		pacLives = new Vector<Sprite> ();
		
		mazeGen = new MazeGenerator(nCol,nRow,mLevel);
		mazeGen.randomize();
		
		tileMapper = new TileMapping(mazeGen,this);
		EngineOptions engineOptions =  new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(MenuActivity.getWidth(),MenuActivity.getHeight()), mCamera);
		engineOptions.setUpdateThreadPriority(android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);
		
		nbKill=0;
		
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
	public void onPause() {
	    super.onPause();
		if((!mScene.hasChildScene()) && (!gameEnded))
			mScene.setChildScene(createPopupScene(),false,true,true);
		
	}
	
	@Override
	public void onResume() {	
		super.onResume();
	}
	
	@Override
	public void onDestroy() {	
		super.onDestroy();
		mScene.detachChildren();
		mScene.reset();
		tileMapper.release();
		
		mPacmanMangeTextureRegion.getTexture().unload();
		mPacmanMangeTextureRegion.getTexture().unload();
		mRedGhostTextureRegion.getTexture().unload();
		mBlueGhostTextureRegion.getTexture().unload();
		mPinkGhostTextureRegion.getTexture().unload();
		mOrangeGhostTextureRegion.getTexture().unload();
		mBonusPointTextureRegion.getTexture().unload();
		mVulGhost1TextureRegion.getTexture().unload();
		mVulGhost2TextureRegion.getTexture().unload();
		mGhostEyesTextureRegion.getTexture().unload();
		mGhostEyesInvTextureRegion.getTexture().unload();
		mPointTextureRegion.getTexture().unload();
		mPacmanTextureRegion.getTexture().unload();
		mPopupBackgroundTextureRegion.getTexture().unload();
		mStartTextBackgroundTextureRegion.getTexture().unload();
		
		soundPool.release();
		pointCollisionDetector.continuer=false;
	}
	
	
	@Override
	protected void onCreateResources() {

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
		try {
			TextureOptions opt = TextureOptions.BILINEAR_PREMULTIPLYALPHA;
			
			ITexture pacmanMangeTexture = new BitmapTexture(this.getTextureManager(),new IInputStreamOpener() { @Override public InputStream open() throws IOException {return getAssets().open("gfx/newgfx/pacman_mange.png");}},opt);
			ITexture redGhostTexture = new BitmapTexture(this.getTextureManager(),new IInputStreamOpener() { @Override public InputStream open() throws IOException {return getAssets().open("gfx/newgfx/monster_red_mange.png");}},opt);
			ITexture blueGhostTexture = new BitmapTexture(this.getTextureManager(),new IInputStreamOpener() { @Override public InputStream open() throws IOException {return getAssets().open("gfx/newgfx/monster_blue_mange.png");}},opt);
			ITexture pinkGhostTexture = new BitmapTexture(this.getTextureManager(),new IInputStreamOpener() { @Override public InputStream open() throws IOException {return getAssets().open("gfx/newgfx/monster_pink_mange.png");}},opt);
			ITexture orangeGhostTexture = new BitmapTexture(this.getTextureManager(),new IInputStreamOpener() { @Override public InputStream open() throws IOException {return getAssets().open("gfx/newgfx/monster_orange_mange.png");}},opt);
			ITexture bonusPointTexture = new BitmapTexture(this.getTextureManager(),new IInputStreamOpener() { @Override public InputStream open() throws IOException {return getAssets().open("gfx/bonus_point.png");}},opt);
			ITexture vulGhost1Texture = new BitmapTexture(this.getTextureManager(),new IInputStreamOpener() { @Override public InputStream open() throws IOException {return getAssets().open("gfx/newgfx/monster_vulnerable.png");}},opt);
			ITexture vulGhost2Texture = new BitmapTexture(this.getTextureManager(),new IInputStreamOpener() { @Override public InputStream open() throws IOException {return getAssets().open("gfx/newgfx/monster_vulnerable2.png");}},opt);
			ITexture ghostEyesTexture = new BitmapTexture(this.getTextureManager(),new IInputStreamOpener() { @Override public InputStream open() throws IOException {return getAssets().open("gfx/newgfx/monster_death.png");}},opt);
			ITexture ghostEyesInvTexture = new BitmapTexture(this.getTextureManager(),new IInputStreamOpener() { @Override public InputStream open() throws IOException {return getAssets().open("gfx/newgfx/monster_death2.png");}},opt);
			ITexture pointTexture = new BitmapTexture(this.getTextureManager(),new IInputStreamOpener() { @Override public InputStream open() throws IOException {return getAssets().open("gfx/point.png");}},opt);
			ITexture pacmanTexture = new BitmapTexture(this.getTextureManager(),new IInputStreamOpener() { @Override public InputStream open() throws IOException {return getAssets().open("gfx/newgfx/pacman.png");}},opt);
			ITexture popupBackgroundTexture = new BitmapTexture(this.getTextureManager(),new IInputStreamOpener() { @Override public InputStream open() throws IOException {return getAssets().open("gfx/bg_popup.png");}},opt);
			ITexture startTextBackgroundTexture = new BitmapTexture(this.getTextureManager(),new IInputStreamOpener() { @Override public InputStream open() throws IOException {return getAssets().open("gfx/bg_start_text.png");}});
			
			
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
			startTextBackgroundTexture.load();
			
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
			mStartTextBackgroundTextureRegion = TextureRegionFactory.extractFromTexture(startTextBackgroundTexture);
			
			
		} catch (IOException e1) {
			Debug.e(e1);
		}
		
		font.load(30, Color.WHITE);
		font.load(31, Color.YELLOW);
		font.load(66, Color.WHITE);
		font.load(48, Color.WHITE);
		font.load(40, Color.WHITE);
		
		mCamera.setBounds(0, -HUD_HEIGHT, nCol*TILE_SIZE, nRow*TILE_SIZE);
		mCamera.setBoundsEnabled(true);
		
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
		this.mVulDuration=values.vulDuration;
		this.mBlinkTime=values.blinkTime;
	}

	@Override
	protected Scene onCreateScene() {
		AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		float actualVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		volume = actualVolume / maxVolume;
		
		MediaPlayer audio = MediaPlayer.create(getApplicationContext(), R.raw.start);
		int duration = audio.getDuration() + 100;
		audio.reset();
		audio.release();

		SharedPreferences settings = this.getSharedPreferences(MenuActivity.PREFS_NAME, 0);

		
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				startGame();
			}
		}, duration);
		
		
		
		mScene = new Scene();
		if(SHOW_FPS) {
			fpsLogger = new FPSLogger();
			mScene.registerUpdateHandler(fpsLogger);
		}
		
		mScene.setBackground(new Background(0f,0f,0f));
		

		//this.mPhysicsWorld  = new FixedStepPhysicsWorld(24, 1, new Vector2(0f, 9.8f), false, 8, 1);
		this.mPhysicsWorld  = new MaxStepPhysicsWorld(60, new Vector2(0f, 9.8f), false,8 ,1);
		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(1, 0f, 1f);
		wallFixtureDef.filter.categoryBits = CATEGORY_SCENERY;
		wallFixtureDef.filter.maskBits = -1;
		
		int nbGhosts = 0;
		
		SpriteBatch sprWall = new SpriteBatch(tileMapper.getTexture(),mazeGen.getCountWalls(),this.getVertexBufferObjectManager());
		sprPoints = new SpriteGroup(mPointTextureRegion.getTexture(), 1000, this.getVertexBufferObjectManager());

		mCamera.setHUD(setupHUD());
		efh = new EntityFollowerHandler(this, mCamera,HUD_HEIGHT);
		
		for(int y=0;y<nRow;y++) {
			for(int x=0;x<nCol;x++) {
				points.add(new SparseArray<Sprite>());
				
				if(mazeGen.value(x, y) == Element.POINT) {
					sprPoints.attachChild(createPoint(marginLeft + x*(TILE_SIZE) + TILE_SIZE/2, marginTop+y*(TILE_SIZE) + TILE_SIZE/2));
				} else if(mazeGen.value(x, y) == Element.BONUS) {
					addBonusPoint(mScene,marginLeft + x*(TILE_SIZE) + TILE_SIZE/2, marginTop+y*(TILE_SIZE) + TILE_SIZE/2);
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
					
					Sprite ghostSprite = new Sprite(marginLeft+x*TILE_SIZE,marginTop+ y*TILE_SIZE,selTexture,this.getVertexBufferObjectManager());
					ghostSprite.setZIndex(42);
					efh.addSprite(ghostSprite);
					ghosts.put(createGhostBody(mScene,ghostSprite), ghostSprite);
					
				} else if(mazeGen.value(x, y) == Element.SPAWNPAC){
					Rectangle pacspwn = new Rectangle(marginLeft + x*TILE_SIZE, marginTop+y*TILE_SIZE,TILE_SIZE, TILE_SIZE,this.getVertexBufferObjectManager());
					pacspwn.setColor(0.1f,0.1f,0.1f);
					mScene.attachChild(pacspwn);

					pacman = createPacman(mScene,x,y);
					pacman.setGravityScale(0f);
					
					spawnPacX = x;
					spawnPacY = y;
				} else {
					Sprite wall = tileMapper.getWallSprite(marginLeft + x*(TILE_SIZE), marginTop+y*(TILE_SIZE),x,y, mScene);
					PhysicsFactory.createBoxBody(this.mPhysicsWorld, wall, BodyType.StaticBody, wallFixtureDef);
					sprWall.draw(wall);
					//mScene.attachChild(wall);
				}
			}
		}
		sprWall.submit();
		sprPoints.submit();
		mScene.attachChild(sprWall);
		mScene.attachChild(sprPoints);
		updateHUD();
		
		
		this.mPhysicsWorld.setContactListener(new ContactListener() {
			@Override
			public void postSolve(Contact contact, ContactImpulse impulse) {}
            @Override
            public void endContact(final Contact pContact) {}
			@Override
			public void preSolve(Contact contact, Manifold oldManifold) {}

			 @Override
	         public void beginContact(final Contact contact) {
							
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
						    		
						    		updateHUD();
						    		
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
											boolean tic=false;
											
											for(int i=0;i<6;i++) {
												if(tic) 
													targetSprite.setTextureRegion(mGhostEyesTextureRegion);
												else 
													targetSprite.setTextureRegion(mGhostEyesInvTextureRegion);
												
												tic = ! tic;
												try {
													Thread.sleep(500);
												} catch (InterruptedException e) {
													e.printStackTrace();
												}
											}
										}
						    		}, 7000);
						    		
						    	} else if(ghosts.get(other).getTextureRegion() != mGhostEyesTextureRegion){ // Pacman is killed by a non-dead ghost
									nVies--;
									pacmanShape.detachSelf();
									other.getWorld().destroyBody(pacman);
									
									if(nVies > 0) {
										updateHUD();
										createPacman(mScene,spawnPacX,spawnPacY);
										Vector<Body> toRemove = new Vector<Body>();
										Vector<Pair<Body,Sprite>>toAdd = new Vector<Pair<Body,Sprite>>();
										Iterator<Body> it=ghosts.keySet().iterator(); // on cr�e un Iterator pour parcourir notre HashSet
										while(it.hasNext()) // tant qu'on a un suivant
										{
											Body body = it.next();
											Sprite ghost = ghosts.get(body);
											int ghostX = (int) ((ghost.getX() - marginLeft)/TILE_SIZE);
											int ghostY = (int) ((ghost.getY() - marginTop)/TILE_SIZE);
	
											
											if(((ghostX >= spawnPacX - 1)&&(ghostX <= spawnPacX + 1)&&(ghostY >= spawnPacY -1)&&(ghostY <= spawnPacY+1))&&(ghost.getTextureRegion() != mGhostEyesTextureRegion)&&(ghost.getTextureRegion() != mGhostEyesInvTextureRegion)) {
												Sprite ghostSprite = new Sprite(marginLeft+spawnGhostX*TILE_SIZE,marginTop+ spawnGhostY*TILE_SIZE,ghost.getTextureRegion(),GameActivity.this.getVertexBufferObjectManager());
												ghostSprite.setZIndex(42);
												ghost.detachSelf();
												toRemove.add(body);
												mPhysicsWorld.destroyBody(body);
												
												toAdd.add(new Pair<Body,Sprite>(createGhostBody(mScene,ghostSprite), ghostSprite));
												toAdd.lastElement().first.setGravityScale(mGhostGravityScale);
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

    	if(settings.getInt("giftizMissionStatus", 0)==1) {// First victory 
    		//GiftizSDK.missionComplete(this); //TODO:
    		Editor editor2 = settings.edit();
    		editor2.putInt("giftizMissionStatus", 2);
    		editor2.commit();
    	}
		
    	if(SHOW_FPS) {
			fpsText = new Text(0,10,font.get(30, Color.WHITE),String.format("%03d", 0),getVertexBufferObjectManager());
			fpsText.setX(MenuActivity.getWidth() - fpsText.getWidth() - 15);
			mHud.attachChild(fpsText);
    	}
		
		mScene.registerUpdateHandler(this.mPhysicsWorld);
		this.getEngine().registerUpdateHandler(new IUpdateHandler() {
            @Override
            public void reset() { }

            @Override
            public void onUpdate(final float pSecondsElapsed) {
            	if(SHOW_FPS) {
	            	if(fpsText != null) {
	        			fpsText.setText(String.format("%03d", (int)fpsLogger.getFPS()));
	        			//fpsLogger.reset();
	        		}
            	}
            	
            	//tickLoop();
            }
		});
		
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

		pointCollisionDetector = new PointCollisionThread();
		pointCollisionDetector.start();
		
		return mScene;
	}
	protected void startGame() {
		Iterator<Body> it=ghosts.keySet().iterator(); 
		while(it.hasNext()) 
			it.next().setGravityScale(mGhostGravityScale);
		
		updateHUD();
		
		readyText.setVisible(false);
		readyTextBackground.setVisible(false);
		pacman.setGravityScale(1f);

		/*if((percentageMusic>0)&&(!paused))
			soundPool.play(idSiren, percentageMusic*volume, percentageMusic*volume, 1, -1, 1f);
		*/
		
		if(startArrowSprite!=null)
			startArrowSprite.setAlpha(0f);
		
		update=true;
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
	public void onSensorChanged(SensorEvent event) {
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
		
		Sprite pacmanSprite = new Sprite(marginLeft+x*TILE_SIZE, marginTop+y*TILE_SIZE,this.mPacmanTextureRegion,this.getVertexBufferObjectManager());
		pacmanSprite.setZIndex(42);
		pacmanShape = pacmanSprite;
		final Body body = PhysicsFactory.createCircleBody(this.mPhysicsWorld, pacmanSprite, BodyType.DynamicBody, objectFixtureDef);
		
        this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(pacmanSprite, body, true, true));
        
        
        pacmanSprite.setUserData(body);
        body.setGravityScale(1f);
        
        scene.registerTouchArea(pacmanSprite);
		scene.attachChild(pacmanSprite);
		

		mCamera.setChaseEntity(pacmanShape);
		body.setBullet(true);
		body.setLinearDamping(1.5f);
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
	
	private Sprite createPoint(int x, int y) {
		/*float pX = x-mPointTextureRegion.getWidth()/2;
		float pY = y-mPointTextureRegion.getHeight()/2;
		pointSpriteBatch.draw((ITextureRegion)mPointTextureRegion, pX, pY, mPointTextureRegion.getWidth(), mPointTextureRegion.getHeight(),ColorUtils.convertRGBAToARGBPackedFloat(1f, 1f, 1f, 0f));
		*/
		Sprite point = new Sprite(x-mPointTextureRegion.getWidth()/2, y-mPointTextureRegion.getHeight()/2,this.mPointTextureRegion,this.getVertexBufferObjectManager());
		point.setZIndex(0);
		point.setCullingEnabled(true);
		points.get((x-marginLeft)/TILE_SIZE).put((y-marginTop)/TILE_SIZE, point);
		return point;
	}
	
	private void addBonusPoint(Scene scene,int x, int y) {
		Sprite point = new Sprite(x-mBonusPointTextureRegion.getWidth()/2, y-mBonusPointTextureRegion.getHeight()/2,this.mBonusPointTextureRegion,this.getVertexBufferObjectManager());
		point.setZIndex(0);
		
		bonus_points.add(point);
        scene.registerTouchArea(point);
		scene.attachChild(point);
	}
	
	private HUD setupHUD() {
		mHud = new HUD();
		float w = MenuActivity.getWidth();
		float h = HUD_HEIGHT;
		
		Rectangle background = new Rectangle(0,0,w,h,this.getVertexBufferObjectManager());
		background.setColor(0, 0, 0);
		mHud.attachChild(background);
		
		fScoreText = new Text(100+3*(mPacmanTextureRegion.getWidth()+10),50,font.get(30, Color.WHITE),"SCORE",getVertexBufferObjectManager());
		mHud.attachChild(fScoreText);
	
	
		vScoreText = new Text(100+3*(mPacmanTextureRegion.getWidth()+10),80,font.get(30, Color.WHITE),String.format("%05d", mScore),6,getVertexBufferObjectManager());
		mHud.attachChild(vScoreText);
		
		levelText = new Text(0,65,this.font.get(31, Color.YELLOW),"LEVEL "+mLevel,getVertexBufferObjectManager());
		levelText.setX((float) ((fScoreText.getWidth() + fScoreText.getX()) + ((MenuActivity.getWidth() - fScoreText.getWidth() - fScoreText.getX())/2.0) - levelText.getWidth()/2.0));
		
		mHud.attachChild(levelText);
	

		String readyTextMsg="GET READY";
		
		readyText = new Text(0,0,font.get(66),readyTextMsg,getVertexBufferObjectManager());
		readyText.setColor(1f,1f,0f);
		readyText.setX(MenuActivity.getWidth()/2f - readyText.getWidth()/2f);
		readyText.setY(MenuActivity.getHeight()/2f - readyText.getHeight()/2f);
		
		
		readyText.setVisible(false);
		readyText.setZIndex(1500);
		
		readyTextBackground = new Sprite(readyText.getX()+readyText.getWidth()/2f-mStartTextBackgroundTextureRegion.getWidth()/2f,readyText.getY()+readyText.getHeight()/2f-mStartTextBackgroundTextureRegion.getHeight()/2f,this.mStartTextBackgroundTextureRegion,this.getVertexBufferObjectManager());

		readyTextBackground.setVisible(false);
		readyTextBackground.setZIndex(1000);
		mHud.attachChild(readyTextBackground);
		mHud.attachChild(readyText);
		mHud.sortChildren();
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				readyTextBackground.setVisible(true);
				readyText.setVisible(true);
			}
		}, 500);
		return mHud;
	}
	
	private void updateHUD() {
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
				mHud.attachChild(liveIcon);
				pacLives.add(liveIcon);
			}
		} else if(pacLives.size() > nVies) {
			while ((pacLives.size() > nVies)&&(nVies>=0)) {
				Sprite spr = pacLives.lastElement();
				spr.detachSelf();
				pacLives.remove(pacLives.size()-1);
			}
		}
		
		// Score screen
		vScoreText.setText(String.format("%05d", mScore));
		
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
	
	public synchronized void tickLoop() {
		if(!update)
			return;

		efh.update();
		
		Iterator<Body> it=ghosts.keySet().iterator(); // on cr�e un Iterator pour parcourir notre HashSet
		while(it.hasNext()) // tant qu'on a un suivant
		{
			Body ghost = it.next();
			float forceX = pacman.getPosition().x - ghost.getPosition().x;
			float forceY = pacman.getPosition().y - ghost.getPosition().y;
			
			if((ghosts.get(ghost).getTextureRegion() == mVulGhost1TextureRegion)||(ghosts.get(ghost).getTextureRegion()==mVulGhost2TextureRegion)||(ghosts.get(ghost).getTextureRegion()==mGhostEyesTextureRegion)) {
				int mulX = (forceX > 0) ? 1 : -1;
				int mulY = (forceY > 0) ? 1 : -1;
				
				
				force.set((forceX*mulX < 5.0) ? mulX*-5 : 0,(forceY*mulY < 5.0) ? mulY*-5 : 0);
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
    			
    			final Sprite toRemove = bonus_points.get(i);
    			mScene.postRunnable(new Runnable() {
					@Override
					public void run() {
						toRemove.detachSelf();
					}
    			});
    			
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
				}, mVulDuration);
				
				new Timer().schedule(new TimerTask(){
					@Override
					public void run() {
						blinkLoop(true);
					}
				}, mBlinkTime);
			}
		}
		
		int pacManX = (int) ((pacmanShape.getX() - marginLeft + pacmanShape.getWidth()/2f)/TILE_SIZE);
    	int pacManY = (int) ((pacmanShape.getY() - marginTop + pacmanShape.getHeight()/2f)/TILE_SIZE);
    	
    	if((pacManX < 0)||(pacManX > this.nCol))
    		pacManX = 0;
    	
    	if((pacManY < 0)||(pacManY > this.nRow))
    		pacManY = 0;

    	final int x = pacManX;
    	final int y = pacManY;
    	
    	if(points.get(pacManX).get(pacManY) != null) {
    		if(points.get(pacManX).get(pacManY).collidesWith(pacmanShape)) {
    			final Sprite toRemove = points.get(x).get(y);
    			mScene.postRunnable(new Runnable() {
					@Override
					public void run() {
						toRemove.detachSelf();
					}
    			});

    			points.get(x).remove(y);
    			
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
    			updateHUD();
    			boolean flag_allempty = true;
    			for(int i=0;i<points.size() && flag_allempty;i++)
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
		Iterator<Body> it=ghosts.keySet().iterator(); // on cr�e un Iterator pour parcourir notre HashSet
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
		Iterator<Body> it=ghosts.keySet().iterator(); // on cr�e un Iterator pour parcourir notre HashSet
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
		if(mBonusActivated == 0)
			return;
		
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
			if((ghostSprite.getTextureRegion() != mGhostEyesTextureRegion)&&(ghostSprite.getTextureRegion() != mGhostEyesInvTextureRegion))
				ghostSprite.setTextureRegion(selTexture);
		}
		
		if(mBonusActivated > 1)
			return;
		
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
		efh.pause(true);
		MenuScene popup = new MenuScene(mCamera);
		popup.setBackgroundEnabled(false);
		int sceneWidth = (int) (mPopupBackgroundTextureRegion.getWidth());
		int sceneHeight = (int) (mPopupBackgroundTextureRegion.getHeight());
		
		int baseX = (int) (MenuActivity.getWidth()/2f - sceneWidth/2f);
		int baseY = (int) (MenuActivity.getHeight()/2f - sceneHeight/2f);
		Rectangle blackBG = new Rectangle(0,0,MenuActivity.getWidth(),MenuActivity.getHeight(),this.getVertexBufferObjectManager());
		blackBG.setColor(0,0,0);
		blackBG.setAlpha(0.7f);
		popup.attachChild(blackBG);
		
		Sprite bg = new Sprite(baseX,baseY,mPopupBackgroundTextureRegion,this.getVertexBufferObjectManager());
		bg.setAlpha(0.95f);
		popup.attachChild(bg);
		
		Text pauseText = new Text(0,baseY+100,font.get(66, Color.WHITE),getResources().getString(R.string.game_pause_title),this.getVertexBufferObjectManager());
		pauseText.setX(baseX + (sceneWidth/2f - pauseText.getWidth()/2f));
		popup.attachChild(pauseText);
		
		resumeText = new Text(0,baseY+290,font.get(48, Color.WHITE),getResources().getString(R.string.game_pause_resume),this.getVertexBufferObjectManager()) {
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
					efh.pause(false);
			        mScene.back();
		        	this.setColor(1f,1f,1f);
		        }
		        return true;
		    }
		};
		resumeText.setX(baseX + (sceneWidth/2f - resumeText.getWidth()/2f));
		popup.attachChild(resumeText);
		popup.registerTouchArea(resumeText);
		
		restartText = new Text(0,baseY+450,font.get(48, Color.WHITE),getResources().getString(R.string.game_pause_restart),this.getVertexBufferObjectManager()) {
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
		
		menuText = new Text(0,baseY+610,font.get(48, Color.WHITE),getResources().getString(R.string.game_pause_menu),this.getVertexBufferObjectManager()){
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
	    as = axisSwap[displayRotation]; 
	    screenVec[0]  =  (float)as[0] * canVec[ as[2] ]; 
	    screenVec[1]  =  (float)as[1] * canVec[ as[3] ]; 
	} 
	
	private class PointCollisionThread extends Thread {
		public boolean continuer=true;
		
		public void run() {
        	while(continuer) {
        		tickLoop();
        		try {
					Thread.sleep(20);
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
    	if((zoom > 0.7)&&(zoom < 2))
			this.mCamera.setZoomFactor(zoom);
    }

    @Override
    public void onPinchZoomFinished(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent, final float pZoomFactor) {
    	float zoom = mPinchZoomStartedCameraZoomFactor * pZoomFactor;
    	if((zoom > 0.7)&&(zoom < 2))
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


