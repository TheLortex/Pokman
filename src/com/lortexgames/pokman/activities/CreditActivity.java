package com.lortexgames.pokman.activities;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.text.Text;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.ui.activity.SimpleBaseGameActivity;

import android.graphics.Color;

public class CreditActivity extends SimpleBaseGameActivity {

	private Font mFontBig;
	private Font mFont30;
	private Font mFont26;
	private Camera camera;

	@Override
	public EngineOptions onCreateEngineOptions() {
		camera = new Camera(0, 0, MenuActivity.getWidth(), MenuActivity.getHeight());
		return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(MenuActivity.getWidth(), MenuActivity.getHeight()), camera);
	}

	@Override
	protected void onCreateResources() {
		FontFactory.setAssetBasePath("font/");
		final ITexture fontTextureBig = new BitmapTextureAtlas(this.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
		final ITexture fontTexture = new BitmapTextureAtlas(this.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
		final ITexture fontTexture30 = new BitmapTextureAtlas(this.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
		
		this.mFontBig = FontFactory.createFromAsset(this.getFontManager(), fontTextureBig, this.getAssets(), "police.ttf", 60f, true, Color.WHITE);
		this.mFontBig.load();
		
		this.mFont26 = FontFactory.createFromAsset(this.getFontManager(), fontTexture, this.getAssets(), "police.ttf", 27f, true, Color.WHITE);
		this.mFont26.load();
		
		this.mFont30 = FontFactory.createFromAsset(this.getFontManager(), fontTexture30, this.getAssets(), "police.ttf", 36f, true, Color.WHITE);
		this.mFont30.load();
	}

	@Override
	protected Scene onCreateScene() {
		Scene scene = new Scene(); 
		scene.setBackground(new Background(0,0,0));
		
		Text titleText = new Text(0,70,mFontBig,"CREDITS",this.getVertexBufferObjectManager());
		titleText.setX(MenuActivity.getWidth()/2f - titleText.getWidth()/2f);
		scene.attachChild(titleText);

		Text devText1 = new Text(15,200 + titleText.getHeight(),mFont30,"Main developer:",this.getVertexBufferObjectManager());

		scene.attachChild(devText1);
		Text devText2 = new Text(0,devText1.getY() + devText1.getHeight() + 10,mFont30,"Lucas Pluvinage",this.getVertexBufferObjectManager());
		devText2.setX(MenuActivity.getWidth() - 20 - devText2.getWidth());
		scene.attachChild(devText2);
		
		Text sfxText1 = new Text(15,500,mFont26,"Thanks R�mi Dupr� who let",this.getVertexBufferObjectManager());
		scene.attachChild(sfxText1);
		Text sfxText2 = new Text(16,530,mFont26,"his voice and beta tested",this.getVertexBufferObjectManager());
		scene.attachChild(sfxText2);
		Text sfxText3 = new Text(16,560,mFont26,"the game",this.getVertexBufferObjectManager());
		scene.attachChild(sfxText3);
		
		Rectangle touchListener = new Rectangle(0,0,MenuActivity.getWidth(), MenuActivity.getHeight(),this.getVertexBufferObjectManager()) {
			@Override
		    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
		        if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_UP) {
		        	finish();
		        } 
		        return true;
		    }
		};
		touchListener.setAlpha(0f);

		scene.attachChild(touchListener);
		scene.registerTouchArea(touchListener);
		return scene;
	}

}
