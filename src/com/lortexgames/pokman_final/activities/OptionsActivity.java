package com.lortexgames.pokman_final.activities;

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

import com.lortexgames.pokman_final.R;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Vibrator;
import android.provider.Settings;
import android.provider.Settings.System;
import android.widget.Toast;


public class OptionsActivity extends SimpleBaseGameActivity {

	public final static int IU_NEVER = 0;
	public final static int IU_WIFI_ONLY = 1;
	public final static int IU_ALWAYS = 2;
	
	private int mInternetUsage;
	
	private Camera camera;
	private Font mFont;
	private Font mFontBig;
	private Text percentageGfxVolum;
	private Sprite slidingGfx;
	private TextureRegion mBtnTexture;
	private Rectangle fixSliderGfx;

	private int percentageGfx;
	private boolean calibration;
	private IFont mFontReturn;
	private Text returnButton;
	private Text internetUsageText;
	/*private Text anonymousData;
	private boolean mAnonymousData;*/


	@Override
	public EngineOptions onCreateEngineOptions() {
		camera = new Camera(0, 0,MenuActivity.getWidth(), MenuActivity.getHeight() );
		return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(MenuActivity.getWidth(),MenuActivity.getHeight()), camera);
	}

	@Override
	protected void onCreateResources() {
		FontFactory.setAssetBasePath("font/");
		final ITexture fontTextureBig = new BitmapTextureAtlas(this.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
		final ITexture fontTexture = new BitmapTextureAtlas(this.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
		final ITexture fontReturnTexture = new BitmapTextureAtlas(this.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
		
		this.mFontBig = FontFactory.createFromAsset(this.getFontManager(), fontTextureBig, this.getAssets(), "police.ttf", 60f, true, Color.WHITE);
		this.mFontBig.load();
		
		this.mFont = FontFactory.createFromAsset(this.getFontManager(), fontTexture, this.getAssets(), "police.ttf", 36f, true, Color.WHITE);
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
		Text titleOptions = new Text(0, 50, mFontBig, getResources().getString(R.string.title_settings), this.getVertexBufferObjectManager());
		titleOptions.setX((float) (0.5*MenuActivity.getWidth() - titleOptions.getWidth()/2.0));
		
		Text gfxVolum = new Text(30, 550, mFont, getResources().getString(R.string.settings_sfx), this.getVertexBufferObjectManager());
		returnButton = new Text(0, 0, mFontReturn, getResources().getString(R.string.btn_return), this.getVertexBufferObjectManager()){
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
		
		returnButton.setX((float) (MenuActivity.getWidth() - returnButton.getWidth() - 70));
		returnButton.setY((float) (MenuActivity.getHeight() - returnButton.getHeight() - 70));

		scene.attachChild(titleOptions);
		//scene.attachChild(musicVolum);
		scene.attachChild(gfxVolum);
		scene.attachChild(returnButton);
		scene.registerTouchArea(returnButton);
		
		// Sliders
		percentageGfxVolum = new Text(0, gfxVolum.getY() + gfxVolum.getHeight() + 50, mFont, "100%", this.getVertexBufferObjectManager());
		percentageGfxVolum.setX(MenuActivity.getWidth() - percentageGfxVolum.getWidth() - 30);
		percentageGfxVolum.setY((float) (percentageGfxVolum.getY() - percentageGfxVolum.getHeight()/4.0));
		scene.attachChild(percentageGfxVolum);
		
		fixSliderGfx = new Rectangle(gfxVolum.getX(), gfxVolum.getY() + gfxVolum.getHeight() + 50, MenuActivity.getWidth() - gfxVolum.getX() * 2 - percentageGfxVolum.getWidth() - 30, 15, this.getVertexBufferObjectManager());
		fixSliderGfx.setColor(0.8f, 0.8f, 0.8f);
		scene.attachChild(fixSliderGfx);
		
		
		slidingGfx = new Sprite((float) (MenuActivity.getWidth() / 2.0),(float) (fixSliderGfx.getHeight() / 2.0 +fixSliderGfx.getY() - mBtnTexture.getHeight() / 2.0),mBtnTexture,this.getVertexBufferObjectManager()){

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
		

		scene.attachChild(slidingGfx);
		scene.registerTouchArea(slidingGfx);
		restorePrefs();
		
		internetUsageText = new Text(30, 350, mFont, "XXXXXXXXXXXXXXXXXXXXXXXXX", this.getVertexBufferObjectManager()) {
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
				if ((pSceneTouchEvent.isActionDown())||(pSceneTouchEvent.isActionMove()))
		        	this.setColor(1f,1f,0f);
		        else if(pSceneTouchEvent.isActionUp()) {
		        	if(hapticFeedback) {
			        	Vibrator v = (Vibrator) OptionsActivity.this.getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
			        	v.vibrate(100);
		        	}
		        	
		        	OptionsActivity.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							new AlertDialog.Builder(OptionsActivity.this)
				            .setSingleChoiceItems(R.array.settings_internet_usage_items,
				            		mInternetUsage,
				                    new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int selectedItem) {
											mInternetUsage=selectedItem;
											updateIUText();
											dialog.dismiss();
										}
				                    })
				           .show();
						}
		        		
		        	});
		        	
		        	this.setColor(1f,1f,1f);
		        }
	        return true;
			}
		};
		
		updateIUText();
		
		scene.attachChild(internetUsageText);
		scene.registerTouchArea(internetUsageText);
		
	/*	anonymousData = new Text(30, 650, mFont, mAnonymousData ? R.string.settings_anon_data_on : R.string.settings_anon_data_off, this.getVertexBufferObjectManager()) {
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
				if ((pSceneTouchEvent.isActionDown())||(pSceneTouchEvent.isActionMove()))
		        	this.setColor(1f,1f,0f);
		        else if(pSceneTouchEvent.isActionUp()) {
		        	
		        	
		        	this.setColor(1f,1f,1f);
		        }
	        return true;
			}
		};
		*/
		scene.setOnSceneTouchListener(new IOnSceneTouchListener() {
			@Override
			public boolean onSceneTouchEvent(Scene pScene,
					TouchEvent pSceneTouchEvent) {
				returnButton.setColor(1f,1f,1f);
				internetUsageText.setColor(1f,1f,1f);
				//anonymousData.setColor(1f,1f,1f);
				return false;
			}
		});
		return scene;
	}
	
	private void updateIUText() {
		if((mInternetUsage>=0)&&(mInternetUsage<=2))
			internetUsageText.setText(getResources().getStringArray(R.array.settings_internet_usage_array)[mInternetUsage]);
		
	}
	
	public void onBackPressed() {
		saveAndFinish();
	}

	private void saveAndFinish() {
		SharedPreferences settings = getSharedPreferences(MenuActivity.PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
	    editor.putInt("sfx", percentageGfx);
		editor.putInt("internetUsage", mInternetUsage);
	//	editor.putBoolean("anonymousData", mAnonymousData);

	    editor.putBoolean("calibration", calibration);
		editor.commit();
		this.toastOnUIThread(getResources().getString(R.string.settings_saved), Toast.LENGTH_SHORT);
		finish();
	}
	
	private void restorePrefs() {
		SharedPreferences settings = getSharedPreferences(MenuActivity.PREFS_NAME, 0);

		percentageGfx = settings.getInt("sfx", 100);
		calibration = settings.getBoolean("calibration", true);

		float placeSliderGfx = (float) (fixSliderGfx.getX() + (percentageGfx/100.0)*fixSliderGfx.getWidth() - mBtnTexture.getWidth()/2.0); 

		slidingGfx.setX(placeSliderGfx);

		percentageGfxVolum.setText(String.format("%03d", percentageGfx)+"%");

		mInternetUsage= settings.getInt("internetUsage", 1);
		//mAnonymousData= settings.getBoolean("anonymousData", true);

	}
	
}
