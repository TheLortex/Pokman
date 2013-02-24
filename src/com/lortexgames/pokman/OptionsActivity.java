package com.lortexgames.pokman;

import java.io.IOException;
import java.io.InputStream;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.font.IFont;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.bitmap.BitmapTexture;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.adt.io.in.IInputStreamOpener;

import android.app.Service;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Vibrator;
import android.provider.Settings;
import android.provider.Settings.System;
import android.widget.Toast;


public class OptionsActivity extends SimpleBaseGameActivity {

	private Camera camera;
	private Font mFont;
	private Font mFontBig;
	//private Text percentageMusicVolum;
	private Text percentageGfxVolum;
	//private Sprite slidingMusic;
	private Sprite slidingGfx;
	private TextureRegion mBtnTexture;
	//private Rectangle fixSliderMusic;
	private Rectangle fixSliderGfx;

	private int percentageGfx;
	private boolean calibration;
	private IFont mFontReturn;
	private Text returnButton;


	@Override
	public EngineOptions onCreateEngineOptions() {
		camera = new Camera(0, 0,MenuActivity.SCREENWIDTH, MenuActivity.SCREENHEIGHT );
		return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(MenuActivity.SCREENWIDTH,MenuActivity.SCREENHEIGHT), camera);
	}

	@Override
	protected void onCreateResources() {
		FontFactory.setAssetBasePath("font/");
		final ITexture fontTextureBig = new BitmapTextureAtlas(this.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
		final ITexture fontTexture = new BitmapTextureAtlas(this.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
		final ITexture fontReturnTexture = new BitmapTextureAtlas(this.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
		
		this.mFontBig = FontFactory.createFromAsset(this.getFontManager(), fontTextureBig, this.getAssets(), "police.ttf", 60f, true, Color.WHITE);
		this.mFontBig.load();
		
		this.mFont = FontFactory.createFromAsset(this.getFontManager(), fontTexture, this.getAssets(), "police.ttf", 30f, true, Color.WHITE);
		this.mFont.load();
		this.mFontReturn = FontFactory.createFromAsset(this.getFontManager(), fontReturnTexture, this.getAssets(), "police.ttf", 42f, true, Color.WHITE);
		this.mFontReturn.load();
		
		try {
			ITexture btnSliderTexture;
			btnSliderTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
			    @Override
			    public InputStream open() throws IOException {
			        return getAssets().open("gfx/btn-slider.png");
			    }
			});
			btnSliderTexture.load();
			this.mBtnTexture = TextureRegionFactory.extractFromTexture(btnSliderTexture);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected Scene onCreateScene() {
		Scene scene = new Scene(); 

		final boolean hapticFeedback = System.getInt(this.getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED, 0) != 0;
		
		// Texts
		Text titleOptions = new Text(0, 50, mFontBig, "SETTINGS", this.getVertexBufferObjectManager());
		titleOptions.setX((float) (0.5*MenuActivity.SCREENWIDTH - titleOptions.getWidth()/2.0));
		
		//Text musicVolum = new Text(30, 350, mFont, "MUSIC", this.getVertexBufferObjectManager());
		
		Text gfxVolum = new Text(30, 550, mFont, "SFX", this.getVertexBufferObjectManager());
		returnButton = new Text(0, 0, mFontReturn, "RETURN", this.getVertexBufferObjectManager()){
			@Override
		    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
				if ((pSceneTouchEvent.isActionDown())||(pSceneTouchEvent.isActionMove()))
		        	this.setColor(1f,1f,0f);
		        else if(pSceneTouchEvent.isActionUp()) {
		        	if(hapticFeedback) {
			        	Vibrator v = (Vibrator) OptionsActivity.this.getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
			        	v.vibrate(100);
		        	}

		        	saveAndFinish();
		        	this.setColor(1f,1f,1f);
		        }
		        return true;
		    }
		};
		
		returnButton.setX((float) (MenuActivity.SCREENWIDTH - returnButton.getWidth() - 70));
		returnButton.setY((float) (MenuActivity.SCREENHEIGHT - returnButton.getHeight() - 70));

		scene.attachChild(titleOptions);
		//scene.attachChild(musicVolum);
		scene.attachChild(gfxVolum);
		scene.attachChild(returnButton);
		scene.registerTouchArea(returnButton);
		
		// Sliders
		//percentageMusicVolum = new Text(0, musicVolum.getY() + musicVolum.getHeight() + 50, mFont, "100%", this.getVertexBufferObjectManager());
	//	percentageMusicVolum.setX(MenuActivity.SCREENWIDTH - percentageMusicVolum.getWidth() - 30);
	//	percentageMusicVolum.setY((float) (percentageMusicVolum.getY() - percentageMusicVolum.getHeight()/4.0)); 
		percentageGfxVolum = new Text(0, gfxVolum.getY() + gfxVolum.getHeight() + 50, mFont, "100%", this.getVertexBufferObjectManager());
		percentageGfxVolum.setX(MenuActivity.SCREENWIDTH - percentageGfxVolum.getWidth() - 30);
		percentageGfxVolum.setY((float) (percentageGfxVolum.getY() - percentageGfxVolum.getHeight()/4.0));
		//scene.attachChild(percentageMusicVolum);
		scene.attachChild(percentageGfxVolum);
		
	//	fixSliderMusic = new Rectangle(musicVolum.getX(), musicVolum.getY() + musicVolum.getHeight() + 50, MenuActivity.SCREENWIDTH - musicVolum.getX() * 2 - percentageMusicVolum.getWidth() - 30, 15, this.getVertexBufferObjectManager());
		//fixSliderMusic.setColor(0.8f, 0.8f, 0.8f);
	//	scene.attachChild(fixSliderMusic);
		fixSliderGfx = new Rectangle(gfxVolum.getX(), gfxVolum.getY() + gfxVolum.getHeight() + 50, MenuActivity.SCREENWIDTH - gfxVolum.getX() * 2 - percentageGfxVolum.getWidth() - 30, 15, this.getVertexBufferObjectManager());
		fixSliderGfx.setColor(0.8f, 0.8f, 0.8f);
		scene.attachChild(fixSliderGfx);
		
		
	/*	slidingMusic = new Sprite((float) (MenuActivity.SCREENWIDTH / 2.0),(float) (fixSliderMusic.getHeight() / 2.0 +fixSliderMusic.getY() - mBtnTexture.getHeight() / 2.0),mBtnTexture,this.getVertexBufferObjectManager()){
			@Override
		    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
		        float xTouchEvent = pSceneTouchEvent.getX();
				if(xTouchEvent<fixSliderMusic.getX()+10)
					xTouchEvent = fixSliderMusic.getX()+10;
				else if(xTouchEvent>fixSliderMusic.getX()+fixSliderMusic.getWidth()-10)
					xTouchEvent = fixSliderMusic.getX()+fixSliderMusic.getWidth()-10;
					
				slidingMusic.setX(xTouchEvent - mBtnTexture.getWidth()/2 );
				
				percentageMusic = Math.round(((xTouchEvent-fixSliderMusic.getX()-10)/(fixSliderMusic.getWidth()-20))*100);
				percentageMusicVolum.setText(String.format("%03d", percentageMusic)+"%");
			    return true;
		    }
		};*/
		slidingGfx = new Sprite((float) (MenuActivity.SCREENWIDTH / 2.0),(float) (fixSliderGfx.getHeight() / 2.0 +fixSliderGfx.getY() - mBtnTexture.getHeight() / 2.0),mBtnTexture,this.getVertexBufferObjectManager()){

			@Override
		    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
		        float xTouchEvent = pSceneTouchEvent.getX();
				if(xTouchEvent<fixSliderGfx.getX()+10)
					xTouchEvent = fixSliderGfx.getX()+10;
				else if(xTouchEvent>fixSliderGfx.getX()+fixSliderGfx.getWidth()-10)
					xTouchEvent = fixSliderGfx.getX()+fixSliderGfx.getWidth()-10;
					
				slidingGfx.setX(xTouchEvent - mBtnTexture.getWidth()/2 );
				
				percentageGfx = Math.round(((xTouchEvent-fixSliderGfx.getX()-10)/(fixSliderGfx.getWidth()-20))*100);
				percentageGfxVolum.setText(String.format("%03d", percentageGfx)+"%");
			    return true;
		    }
		};
		

		//scene.attachChild(slidingMusic);
		scene.attachChild(slidingGfx);

		//scene.registerTouchArea(slidingMusic);
		scene.registerTouchArea(slidingGfx);
		
		restorePrefs();
		
		/*calibrationText = new Text(30, 850, mFont, "AUTO-CALIBRATION: "+((calibration)?"ON":"OFF"), this.getVertexBufferObjectManager()) {
			@Override
		    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
				if(pSceneTouchEvent.isActionDown()) {
					calibration = !calibration;
					calibrationText.setText("AUTO-CALIBRATION: "+((calibration)?"ON":"OFF"));
					return true;
				}
				return false;
			}
		};
		scene.attachChild(calibrationText);
		scene.registerTouchArea(calibrationText);*/
		
		scene.setOnSceneTouchListener(new IOnSceneTouchListener() {
			@Override
			public boolean onSceneTouchEvent(Scene pScene,
					TouchEvent pSceneTouchEvent) {
				returnButton.setColor(1f,1f,1f);
				return false;
			}
		});
		return scene;
	}
	
	public void onBackPressed() {
		saveAndFinish();
	}

	private void saveAndFinish() {
		SharedPreferences settings = getSharedPreferences(MenuActivity.PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
	    editor.putInt("sfx", percentageGfx);
	  //  editor.putInt("music", percentageMusic);
	    editor.putBoolean("calibration", calibration);
		editor.commit();
		this.toastOnUIThread("Settings saved", Toast.LENGTH_SHORT);
		finish();
	}
	
	private void restorePrefs() {
		SharedPreferences settings = getSharedPreferences(MenuActivity.PREFS_NAME, 0);
	//	percentageMusic = settings.getInt("music", 100);
		percentageGfx = settings.getInt("sfx", 100);
		calibration = settings.getBoolean("calibration", true);

		float placeSliderGfx = (float) (fixSliderGfx.getX() + (percentageGfx/100.0)*fixSliderGfx.getWidth() - mBtnTexture.getWidth()/2.0); 
	//	float placeSliderMusic = (float) (fixSliderMusic.getX() + (percentageMusic/100.0)*fixSliderMusic.getWidth() - mBtnTexture.getWidth()/2.0); 
	//	slidingMusic.setX(placeSliderMusic);
		slidingGfx.setX(placeSliderGfx);

		percentageGfxVolum.setText(String.format("%03d", percentageGfx)+"%");
	//	percentageMusicVolum.setText(String.format("%03d", percentageMusic)+"%");
		
	}
	
}
