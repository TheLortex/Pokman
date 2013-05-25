package com.lortexgames.pokman.handlers;

import java.util.HashMap;
import java.util.Iterator;

import org.andengine.engine.camera.ZoomCamera;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.debug.Debug;

import com.lortexgames.pokman.activities.GameActivity;
import com.lortexgames.pokman.activities.MenuActivity;


public class EntityFollowerHandler {

	private ZoomCamera  			mCamera;
	private SimpleBaseGameActivity 	mCtx;
	private HashMap<Sprite,Sprite>  mFollowingSprites;
	private boolean 				mPaused=false;
	
	public EntityFollowerHandler(SimpleBaseGameActivity ctx, ZoomCamera camera) {
		mCtx    	= ctx;
		mCamera 	= camera;
		mFollowingSprites = new HashMap<Sprite,Sprite>();
	}
	
	public void addSprite(Sprite toFollow) {
		Sprite icon = new Sprite(0, 0, toFollow.getTextureRegion(), mCtx.getVertexBufferObjectManager());
		icon.setVisible(false);
		icon.setAlpha(0.5f);
		icon.setZIndex(-1);
		
		mCamera.getHUD().attachChild(icon);
		mFollowingSprites.put(toFollow, icon);
	}
	
	public void removeSprite(final Sprite toRemove) {
		mCtx.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mFollowingSprites.get(toRemove).detachSelf();
				mFollowingSprites.remove(toRemove);
			}
		});

	}
	
	public void pause(boolean isPaused)  {
		mPaused = isPaused;
	}
	
	public synchronized void update() {
		Iterator<Sprite> keys = mFollowingSprites.keySet().iterator();
		Debug.e("Bite "+mFollowingSprites.size());
		while(keys.hasNext()) {
			Sprite curSprite     = keys.next();
			final Sprite curSpriteIcon = mFollowingSprites.get(curSprite);
			
			if(curSpriteIcon.getTextureRegion() != curSprite.getTextureRegion()) 
				curSpriteIcon.setTextureRegion(curSprite.getTextureRegion());
			
			if(curSpriteIcon.getScaleX() != mCamera.getZoomFactor())
				curSpriteIcon.setScale(mCamera.getZoomFactor());
			
			float sx=curSprite.getX(), sy=curSprite.getY();
			float cxmin=mCamera.getXMin(), cymin=mCamera.getYMin()+90, cxmax=mCamera.getXMax(), cymax=mCamera.getYMax();
			
			float sprH = curSprite.getHeight()/2f, sprW = curSprite.getWidth()/2f;

			if(((sx+sprW >= cxmin)&&(sy+sprH >= cymin)&&(sx <= cxmax)&&(sy <= cymax))||(mPaused)) {
				if(curSpriteIcon.isVisible()) {
					mCtx.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							curSpriteIcon.setVisible(false);
						}
					});
				}
			} else {
				curSpriteIcon.setVisible(true);
				final float posX, posY;
				
				if(sy < cymin)
					posY = GameActivity.HUD_HEIGHT-sprH;
				else if(sy > cymax)
					posY =MenuActivity.getHeight() - sprH;
				else
					posY = ((sy-cymin)/mCamera.getHeight())*MenuActivity.getHeight()+GameActivity.HUD_HEIGHT-sprH;
				
				if(sx < cxmin)
					posX = -sprW;
				else if(sx > cxmax)
					posX = MenuActivity.getWidth() - sprW;
				else
					posX = ((sx-cxmin)/mCamera.getWidth())*MenuActivity.getWidth()+sprW;
				
				mCtx.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						curSpriteIcon.setVisible(true);
						curSpriteIcon.setX(posX);
						curSpriteIcon.setY(posY);
					}
				});
			}
		}
	}
}
