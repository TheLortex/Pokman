package com.lortexgames.pokman.activities;

import com.lortexgames.pokman.Element;
import com.lortexgames.pokman.FontManager;
import com.lortexgames.pokman.MazeGenerator;
import com.lortexgames.pokman.R;
import com.lortexgames.pokman.TileMapping;
import com.lortexgames.pokman.R.array;
import com.lortexgames.pokman.R.string;
import com.lortexgames.pokman.addons.Paginator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.bitmap.BitmapTexture;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.adt.io.in.IInputStreamOpener;
import org.andengine.util.debug.Debug;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.provider.Settings.System;
import android.util.SparseArray;
import android.view.Display;
import android.view.WindowManager;

public class TutorialActivity  extends SimpleBaseGameActivity  implements SensorEventListener{
	private final int nPages=5;
	final short CATEGORY_PACMAN = 0x0001;
	final short CATEGORY_GHOST =0x0002;
	final short CATEGORY_SCENERY = 0x0004;
    final short CATEGORY_GHOSTEYES = 0x0008;
    
    
	int currentPage = 1;
	
	private TileMapping tileMapper;
	private MazeGenerator mazeGen;
	private int nRow;
	private int nCol;

	private Camera mCamera;
	private FontManager font;
	private TextureRegion mPacmanMangeTextureRegion;
	private TextureRegion mRedGhostTextureRegion;
	private TextureRegion mVulGhost2TextureRegion;
	private TextureRegion mVulGhost1TextureRegion;
	private TextureRegion mBonusPointTextureRegion;
	private TextureRegion mPointTextureRegion;
	private TextureRegion mGhostEyesInvTextureRegion;
	private TextureRegion mGhostEyesTextureRegion;
	private TextureRegion mPacmanTextureRegion;
	private Scene mScene;
	private SensorManager sensorManager;
	private Sensor accelerometer;
	private PhysicsWorld mPhysicsWorld;
	
	private boolean monsterDead=false;

	
	private SparseArray<Vector<Body>> bodies = new SparseArray<Vector<Body>>();
	
	private Vector2 gravity=new Vector2();
	private FixtureDef wallFixtureDef;
	
	private float[] rawValues = {0,0};
	private float[] realValues= {0,0};
	
	private Vector<Sprite> bonus_points=new Vector<Sprite>();
	private Vector<Sprite> points=new Vector<Sprite>();

	Vector<Body> ghostBodies = new Vector<Body>();
	Vector<Sprite> ghostSprites = new Vector<Sprite>();
	
	Vector<Sprite> pacmanSprites = new Vector<Sprite>();
	Vector<Body> pacmanBodies = new Vector<Body>();
	
	protected int mBonusActivated=0;
	protected boolean allPointsEatenToast=false;
	private Text playText;
	private Paginator pagination;

	public static final int axisSwap[][] = { 
    {  1,  1,  0,  1  },     // ROTATION_0 
    {-1,  1,  1,  0  },     // ROTATION_90 
    {-1,    -1,  0,  1  },     // ROTATION_180 
    {  1,    -1,  1,  0  }  }; // ROTATION_270 
	private static int[] as;
	
	@Override
	public EngineOptions onCreateEngineOptions() {
		//Debug.i("Creating engine");
		font = new FontManager(this);
		mCamera = new Camera(0, 0, MenuActivity.getWidth(), MenuActivity.getHeight());

		nRow = 7;
		nCol = 7;

		mazeGen = new MazeGenerator(nRow,nCol,0);
		
		tileMapper = new TileMapping(mazeGen,this);
		EngineOptions engineOptions =  new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(MenuActivity.getWidth(),MenuActivity.getHeight()), mCamera);
		return engineOptions;
	}


	@Override
	protected void onCreateResources() {
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
		try {
			TextureOptions opt = TextureOptions.BILINEAR_PREMULTIPLYALPHA;
			
			ITexture pacmanMangeTexture = new BitmapTexture(this.getTextureManager(),new IInputStreamOpener() { @Override public InputStream open() throws IOException {return getAssets().open("gfx/newgfx/pacman_mange.png");}},opt);
			ITexture redGhostTexture = new BitmapTexture(this.getTextureManager(),new IInputStreamOpener() { @Override public InputStream open() throws IOException {return getAssets().open("gfx/newgfx/monster_red_mange.png");}},opt);
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
			mBonusPointTextureRegion = TextureRegionFactory.extractFromTexture(bonusPointTexture);
			mVulGhost1TextureRegion = TextureRegionFactory.extractFromTexture(vulGhost1Texture);
			mVulGhost2TextureRegion = TextureRegionFactory.extractFromTexture(vulGhost2Texture);
			mGhostEyesTextureRegion = TextureRegionFactory.extractFromTexture(ghostEyesTexture);
			mGhostEyesInvTextureRegion = TextureRegionFactory.extractFromTexture(ghostEyesInvTexture);
			mPointTextureRegion = TextureRegionFactory.extractFromTexture(pointTexture);
			mPacmanTextureRegion = TextureRegionFactory.extractFromTexture(pacmanTexture);
			
			
		} catch (IOException e1) {
			Debug.e(e1);
		}
		
		font.load(30, Color.WHITE);
		font.load(60, Color.WHITE);
		font.load(70, Color.WHITE);
		
		try {
			tileMapper.loadTextures();
		} catch (IOException e) {
			Debug.e(e);
		}
	}

	@Override
	protected Scene onCreateScene() {
		mScene = new Scene();
		
		wallFixtureDef = PhysicsFactory.createFixtureDef(1, 0f, 1f);
		wallFixtureDef.filter.categoryBits = CATEGORY_SCENERY;
		wallFixtureDef.filter.maskBits = -1;
		
		this.mPhysicsWorld  = new FixedStepPhysicsWorld(24, 1, new Vector2(0f, 9.8f), false, 8, 1);
		mScene.registerUpdateHandler(this.mPhysicsWorld);

		for(int i=1;i<=nPages;i++)
			bodies.put(i, new Vector<Body>());
		
		drawStep1(0);
		drawStep2(MenuActivity.getWidth());
		drawStep3(MenuActivity.getWidth()*2);
		drawStep4(MenuActivity.getWidth()*3);
		drawStep5(MenuActivity.getWidth()*4);

		mScene.setOnSceneTouchListener(new IOnSceneTouchListener() {
			private float xcoor;

			@Override
			public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
				playText.setColor(1f,1f,1f);
				if(pSceneTouchEvent.isActionDown()) {
					xcoor = pSceneTouchEvent.getX();
				} else if(pSceneTouchEvent.isActionMove()) {
					mCamera.offsetCenter(xcoor - pSceneTouchEvent.getX(), 0);
					pagination.setX(pagination.getX()+xcoor - pSceneTouchEvent.getX());
				} else if(pSceneTouchEvent.isActionUp()) {
					for(int i=1;i<=nPages;i++) {
						if(mCamera.getCenterX() < MenuActivity.getWidth()*i) {
							mCamera.setCenter(MenuActivity.getWidth()/2f, MenuActivity.getHeight()/2f);
							mCamera.offsetCenter((i-1)*MenuActivity.getWidth(), 0);
							pagination.setX((i-1)*MenuActivity.getWidth());
							currentPage=i;
							i=nPages+1;
						}
					}
					
					if(mCamera.getCenterX()>MenuActivity.getWidth()*nPages) {
						mCamera.setCenter(MenuActivity.getWidth()/2f, MenuActivity.getHeight()/2f);
						mCamera.offsetCenter((nPages-1)*MenuActivity.getWidth(), 0);
						pagination.setX((nPages-1)*MenuActivity.getWidth());
					}
					updateGravity();
					pagination.setPage(currentPage);
				}
				return false;
			}
		});

		updateGravity();
		mScene.sortChildren();
		
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
				
				if(((bodyA == ghostBodies.get(0))||(bodyB ==  ghostBodies.get(0)))&&((bodyA == pacmanBodies.get(2))||(bodyB ==  pacmanBodies.get(2)))) {
					TutorialActivity.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							pacmanSprites.get(2).setVisible(false);

				    		Filter newFilter = new Filter();
				    		newFilter.categoryBits = CATEGORY_GHOSTEYES;
				    		newFilter.maskBits= CATEGORY_SCENERY | CATEGORY_GHOSTEYES;
				    		pacmanBodies.get(2).getFixtureList().get(0).setFilterData(newFilter);
						}
					});
					
					TutorialActivity.this.toastOnUIThread(getResources().getString(R.string.tuto_toast_pokman_eaten));
				} else if(((bodyA == ghostBodies.get(1))||(bodyB ==  ghostBodies.get(1)))&&((bodyA == pacmanBodies.get(3))||(bodyB ==  pacmanBodies.get(3)))&&(mBonusActivated>0)) {
					final Body target = ghostBodies.get(1);
					final Sprite targetSprite = ghostSprites.get(1);
					
					targetSprite.setTextureRegion(mGhostEyesTextureRegion);
		    		Filter newFilter = new Filter();
		    		newFilter.categoryBits = CATEGORY_GHOSTEYES;
		    		newFilter.maskBits= CATEGORY_SCENERY | CATEGORY_GHOSTEYES;
		    		target.getFixtureList().get(0).setFilterData(newFilter);
		    		
		    		monsterDead=true;
		    		
					new Timer().schedule(new TimerTask() {
						@Override
						public void run() {
				    		Filter newFilter = new Filter();
				    		newFilter.categoryBits = CATEGORY_GHOST;
				    		newFilter.maskBits= CATEGORY_SCENERY | CATEGORY_PACMAN | CATEGORY_GHOST;
				    		target.getFixtureList().get(0).setFilterData(newFilter);
				    		target.setGravityScale(0.9f);
				    		targetSprite.setTextureRegion(mRedGhostTextureRegion); // mGhostEyesInvTextureRegion
						}
		    		}, 10000);
		    		
		    		new Timer().schedule(new TimerTask() {
						@Override
						public void run() {
							boolean tic=false;
							
							for(int i=0;i<5;i++) {
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
		    		
					TutorialActivity.this.toastOnUIThread(getResources().getString(R.string.tuto_toast_ghost_eaten)); 
				
				}	
			}
		});
		mScene.registerUpdateHandler(new IUpdateHandler() {
			@Override
			public void onUpdate(float pSecondsElapsed) {
				for(int i=0;i<bonus_points.size();i++) {
					if(bonus_points.get(i).collidesWith(pacmanSprites.get(3))) {
						bonus_points.get(i).detachSelf();
						bonus_points.remove(i);
						
						mBonusActivated++;
						
						ghostSprites.get(1).setTextureRegion(mVulGhost1TextureRegion);
						
						new Timer().schedule(new TimerTask(){
							@Override
							public void run() {
								mBonusActivated--;
								if(mBonusActivated == 0) {
									ghostSprites.get(1).setTextureRegion(mRedGhostTextureRegion);
								}
							}
						}, 10000);
						
						new Timer().schedule(new TimerTask(){
							@Override
							public void run() {
								boolean tic=false;
								while((mBonusActivated == 1)&&(!monsterDead)) {
									if(tic) 
										ghostSprites.get(1).setTextureRegion(mVulGhost1TextureRegion);
									else
										ghostSprites.get(1).setTextureRegion(mVulGhost2TextureRegion);
									tic = !tic;
									
									try {
										Thread.sleep(500, 0);
									} catch (InterruptedException e) {
										Debug.e(e);
									}
								}
							}
						}, 7000);
					}
				}
				
				for(int i=0;i<points.size();i++) {
					if(points.get(i).collidesWith(pacmanSprites.get(1))) {
						points.get(i).detachSelf();
						points.remove(i);
						
	        			pacmanSprites.get(1).setTextureRegion(mPacmanMangeTextureRegion);
	        			
	    				new Timer().schedule(new TimerTask(){
							@Override
							public void run() {
								pacmanSprites.get(1).setTextureRegion(mPacmanTextureRegion);
							}
	    				}, 150);
					}
				}
			
				
				if((points.isEmpty())&&(!allPointsEatenToast)) {
					TutorialActivity.this.toastOnUIThread(getResources().getString(R.string.tuto_toast_welldone));
					allPointsEatenToast=true;
				}
			}	

			@Override
			public void reset() {}
		});
		
		pagination = new Paginator(20, MenuActivity.getHeight()-20, MenuActivity.getWidth()-40, 15, 5, 1, this.getVertexBufferObjectManager());
		mScene.attachChild(pagination);
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
	
	

	private void updateGravity() {
		for(int i=1;i<=nPages;i++) {
			if(i==currentPage) {
				for(int b=0;b<bodies.get(i).size();b++) {
					boolean flag=false;
					for(int k=0;k<ghostBodies.size()&&!flag;k++) {
						if(bodies.get(i).get(b)==ghostBodies.get(k)) {
							ghostBodies.get(k).setGravityScale(0.3f);
							flag = true;
						}
					}
					
					if(!flag)
						bodies.get(i).get(b).setGravityScale(1f);
				}
			}else {
				for(int b=0;b<bodies.get(i).size();b++) {
					bodies.get(i).get(b).setGravityScale(0f);
					bodies.get(i).get(b).setLinearVelocity(0, 0);
					bodies.get(i).get(b).setAngularVelocity(0f);
				}
			}
		}
	}
	
	private void drawStep1(int offsetX) {
		Text title = new Text(0,100,font.get(60, Color.WHITE),getResources().getStringArray(R.array.tuto_1)[0],this.getVertexBufferObjectManager());
		title.setX(offsetX+MenuActivity.getWidth()/2f-title.getWidth()/2f);
		
		Text text1 = new Text(0,0,font.get(30, Color.WHITE),getResources().getStringArray(R.array.tuto_1)[1],this.getVertexBufferObjectManager());
		text1.setX(offsetX+MenuActivity.getWidth()/2f-text1.getWidth()/2f);
		text1.setY(MenuActivity.getHeight()-200);
		Text text2 = new Text(0,0,font.get(30, Color.WHITE),getResources().getStringArray(R.array.tuto_1)[2],this.getVertexBufferObjectManager());
		text2.setX(offsetX+MenuActivity.getWidth()/2f-text2.getWidth()/2f);
		text2.setY(MenuActivity.getHeight()-150);

		Element mur = Element.MUR;
		Element pac = Element.SPAWNPAC;
		Element vid = Element.VIDE;
		
		Element map[][] =  {{mur,mur,mur,mur,mur,mur,mur},
							{mur,vid,vid,vid,vid,vid,mur},
							{mur,vid,vid,vid,vid,vid,mur},
							{mur,vid,vid,pac,vid,vid,mur},
							{mur,vid,vid,vid,vid,vid,mur},
							{mur,vid,vid,vid,vid,vid,mur},
							{mur,mur,mur,mur,mur,mur,mur}};
		
		mazeGen.customMap(map);
		
		drawMaze(1);
		
		mScene.attachChild(title);
		mScene.attachChild(text1);
		mScene.attachChild(text2);
	}


	private void drawStep2(int offsetX) {
		Text title = new Text(0,100,font.get(60, Color.WHITE),getResources().getStringArray(R.array.tuto_2)[0],this.getVertexBufferObjectManager());
		title.setX(offsetX+MenuActivity.getWidth()/2f-title.getWidth()/2f);
		
		Text text1 = new Text(0,0,font.get(30, Color.WHITE),getResources().getStringArray(R.array.tuto_2)[1],this.getVertexBufferObjectManager());
		text1.setX(offsetX+MenuActivity.getWidth()/2f-text1.getWidth()/2f);
		text1.setY(MenuActivity.getHeight()-200);
		Text text2 = new Text(0,0,font.get(30, Color.WHITE),getResources().getStringArray(R.array.tuto_2)[2],this.getVertexBufferObjectManager());
		text2.setX(offsetX+MenuActivity.getWidth()/2f-text2.getWidth()/2f);
		text2.setY(MenuActivity.getHeight()-150);

		Element mur = Element.MUR;
		Element pac = Element.SPAWNPAC;
		Element pts = Element.POINT;
		
		Element map[][] =  {{mur,mur,mur,mur,mur,mur,mur},
							{mur,pts,pts,pts,pts,pts,mur},
							{mur,pts,pts,pts,pts,pts,mur},
							{mur,pts,pts,pac,pts,pts,mur},
							{mur,pts,pts,pts,pts,pts,mur},
							{mur,pts,pts,pts,pts,pts,mur},
							{mur,mur,mur,mur,mur,mur,mur}};
		
		mazeGen.customMap(map);
		
		drawMaze(2);
		
		mScene.attachChild(title);
		mScene.attachChild(text1);
		mScene.attachChild(text2);
	}
	
	private void drawStep3(int offsetX) {
		Text title = new Text(0,100,font.get(60, Color.WHITE),getResources().getStringArray(R.array.tuto_3)[0],this.getVertexBufferObjectManager());
		title.setX(offsetX+MenuActivity.getWidth()/2f-title.getWidth()/2f);
		
		Text text1 = new Text(0,0,font.get(30, Color.WHITE),getResources().getStringArray(R.array.tuto_3)[1],this.getVertexBufferObjectManager());
		text1.setX(offsetX+MenuActivity.getWidth()/2f-text1.getWidth()/2f);
		text1.setY(MenuActivity.getHeight()-200);
		Text text2 = new Text(0,0,font.get(30, Color.WHITE),getResources().getStringArray(R.array.tuto_3)[2],this.getVertexBufferObjectManager());
		text2.setX(offsetX+MenuActivity.getWidth()/2f-text2.getWidth()/2f);
		text2.setY(MenuActivity.getHeight()-150);

		Element mur = Element.MUR;
		Element pac = Element.SPAWNPAC;
		Element vid = Element.VIDE;
		Element gst = Element.SPAWNGHOST;
		
		Element map[][] =  {{mur,mur,mur,mur,mur,mur,mur},
				{mur,vid,vid,gst,vid,vid,mur},
				{mur,vid,vid,vid,vid,vid,mur},
				{mur,vid,vid,vid,vid,vid,mur},
				{mur,vid,vid,vid,vid,vid,mur},
				{mur,vid,vid,pac,vid,vid,mur},
				{mur,mur,mur,mur,mur,mur,mur}};
		
		mazeGen.customMap(map);
		
		drawMaze(3);
		
		mScene.attachChild(title);
		mScene.attachChild(text1);
		mScene.attachChild(text2);
	}
	
	private void drawStep4(int offsetX) {
		Text title = new Text(0,100,font.get(60, Color.WHITE),getResources().getStringArray(R.array.tuto_4)[0],this.getVertexBufferObjectManager());
		title.setX(offsetX+MenuActivity.getWidth()/2f-title.getWidth()/2f);
		
		Text text1 = new Text(0,0,font.get(30, Color.WHITE),getResources().getStringArray(R.array.tuto_4)[1],this.getVertexBufferObjectManager());
		text1.setX(offsetX+MenuActivity.getWidth()/2f-text1.getWidth()/2f);
		text1.setY(MenuActivity.getHeight()-200);
		Text text2 = new Text(0,0,font.get(30, Color.WHITE),getResources().getStringArray(R.array.tuto_4)[2],this.getVertexBufferObjectManager());
		text2.setX(offsetX+MenuActivity.getWidth()/2f-text2.getWidth()/2f);
		text2.setY(MenuActivity.getHeight()-150);

		Element mur = Element.MUR;
		Element pac = Element.SPAWNPAC;
		Element vid = Element.VIDE;
		Element gst = Element.SPAWNGHOST;
		Element bns = Element.BONUS;
		
		Element map[][] =  {{mur,mur,mur,mur,mur,mur,mur},
				{mur,vid,vid,gst,vid,vid,mur},
				{mur,vid,vid,vid,vid,vid,mur},
				{mur,vid,vid,bns,vid,vid,mur},
				{mur,vid,vid,vid,vid,vid,mur},
				{mur,vid,vid,pac,vid,vid,mur},
				{mur,mur,mur,mur,mur,mur,mur}};
		
		mazeGen.customMap(map);
		
		drawMaze(4);
		
		mScene.attachChild(title);
		mScene.attachChild(text1);
		mScene.attachChild(text2);
	}
	
	private void drawStep5(int offsetX) {
		Text title = new Text(0,100,font.get(60, Color.WHITE),getResources().getStringArray(R.array.tuto_5)[0],this.getVertexBufferObjectManager());
		title.setX(offsetX+MenuActivity.getWidth()/2f-title.getWidth()/2f);
		
		Text text1 = new Text(0,0,font.get(30, Color.WHITE),getResources().getStringArray(R.array.tuto_5)[1],this.getVertexBufferObjectManager());
		text1.setX(offsetX+MenuActivity.getWidth()/2f-text1.getWidth()/2f);
		text1.setY(MenuActivity.getHeight()-200);
		Text text2 = new Text(0,0,font.get(30, Color.WHITE),getResources().getStringArray(R.array.tuto_5)[2],this.getVertexBufferObjectManager());
		text2.setX(offsetX+MenuActivity.getWidth()/2f-text2.getWidth()/2f);
		text2.setY(MenuActivity.getHeight()-150);

		playText = new Text(0,0,font.get(70, Color.WHITE),getResources().getString(R.string.startgame),this.getVertexBufferObjectManager()) {
			@Override
		    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
				if ((pSceneTouchEvent.isActionDown())||(pSceneTouchEvent.isActionMove()))
		        	this.setColor(1f,1f,0f);
		        else if(pSceneTouchEvent.isActionUp()) {
		        	boolean hapticFeedback = System.getInt(TutorialActivity.this.getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED, 0) != 0;;
		        	if(hapticFeedback) {
			        	Vibrator v = (Vibrator) TutorialActivity.this.getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
			        	v.vibrate(100);
		        	}
		        	Intent intent = new Intent(TutorialActivity.this, GameActivity.class);
                	intent.putExtra(MenuActivity.LEVEL, 1);
                	intent.putExtra(GameActivity.NVIES, 3);
					startActivity(intent);
					
		        	this.setColor(1f,1f,1f);
		        }
		        return true;
		    }
		};
		playText.setX(offsetX+MenuActivity.getWidth()/2f-playText.getWidth()/2f);
		playText.setY(MenuActivity.getHeight()/2f-playText.getHeight()/2f);

		
		mScene.attachChild(title);
		mScene.attachChild(text1);
		mScene.attachChild(text2);
		
		mScene.attachChild(playText);
		mScene.registerTouchArea(playText);
	}

	private void drawMaze(int page) {
		int mazeWidth = 7*40;
		int offsetX = (int) (MenuActivity.getWidth()*(page-1) + MenuActivity.getWidth()/2f - mazeWidth/2f);
		int offsetY = 400;
		
		for(int y=0;y<7;y++) {
			for(int x=0;x<7;x++) {
				if(mazeGen.value(x, y) == Element.SPAWNPAC){
					Rectangle pacspwn = new Rectangle(offsetX + x*40, offsetY+y*40,40, 40,this.getVertexBufferObjectManager());
					pacspwn.setColor(0.1f,0.1f,0.1f);
					mScene.attachChild(pacspwn);

					Body pacman = createPacman(mScene,offsetX + x*40, offsetY+y*40);
					bodies.get(page).add(pacman);
					pacmanBodies.add(pacman);
					
					//pacman.setGravityScale(0f);
				} else if(mazeGen.value(x, y) == Element.POINT) {
					addPoint(mScene,offsetX + x*40 + 40/2, offsetY+y*40 + 40/2);
				} else if(mazeGen.value(x, y) == Element.BONUS) {
					addBonusPoint(mScene,offsetX + x*40 + 40/2, offsetY+y*40 + 40/2);
				}else if(mazeGen.value(x, y) == Element.SPAWNGHOST) {
					Sprite ghostSprite = new Sprite(offsetX+x*40,offsetY+ y*40,mRedGhostTextureRegion,this.getVertexBufferObjectManager());
					ghostSprite.setZIndex(42);
					Body curBody = createGhostBody(mScene,ghostSprite);
					bodies.get(page).add(curBody);
					ghostBodies.add(curBody);
					ghostSprites.add(ghostSprite);
				} else if(mazeGen.value(x, y) == Element.MUR) {
					Sprite wall = tileMapper.getWallSprite(offsetX + x*40, 400+y*40,x,y, mScene);
					PhysicsFactory.createBoxBody(this.mPhysicsWorld, wall, BodyType.StaticBody, wallFixtureDef);

					mScene.attachChild(wall);
				}
			}
		}
	}
	
	private Body createPacman(Scene mScene2, int x, int y) {
		Sprite pacmanSprite = new Sprite(x, y,this.mPacmanTextureRegion,this.getVertexBufferObjectManager());
		pacmanSprite.setZIndex(42);
		FixtureDef objectFixtureDef = PhysicsFactory.createFixtureDef(1f, 0, 0.2f);
		objectFixtureDef.filter.categoryBits= CATEGORY_PACMAN;
		objectFixtureDef.filter.maskBits= CATEGORY_GHOST | CATEGORY_SCENERY;
		pacmanSprites.add(pacmanSprite);
		
		final Body body = PhysicsFactory.createCircleBody(this.mPhysicsWorld, pacmanSprite, BodyType.DynamicBody, objectFixtureDef);
		
        this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(pacmanSprite, body, true, true));
        
        pacmanSprite.setUserData(body);
        body.setGravityScale(1f);
        
        mScene.registerTouchArea(pacmanSprite);
        mScene.attachChild(pacmanSprite);
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
		points.add(point);

		
		scene.attachChild(point);
	}
	
	private void addBonusPoint(Scene scene,int x, int y) {
		Sprite point = new Sprite(x-mBonusPointTextureRegion.getWidth()/2, y-mBonusPointTextureRegion.getHeight()/2,this.mBonusPointTextureRegion,this.getVertexBufferObjectManager());
		point.setZIndex(0);
		
		bonus_points.add(point);
        scene.registerTouchArea(point);
		scene.attachChild(point);
	}
	
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {}

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
			
			gravity.set(-x, y);
			gravity.mul(5);
			
		
			this.mPhysicsWorld.setGravity(gravity);
		}
	}
	static void canonicalToScreenOrientation(int displayRotation, float[] canVec, float[] screenVec) 
	{ 
	    as = axisSwap[displayRotation]; 
	    screenVec[0]  =  (float)as[0] * canVec[ as[2] ]; 
	    screenVec[1]  =  (float)as[1] * canVec[ as[3] ]; 
	} 
}
