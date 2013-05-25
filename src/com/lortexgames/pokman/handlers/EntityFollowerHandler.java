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
	private int 					mHudHeight;
	
	public EntityFollowerHandler(SimpleBaseGameActivity ctx, ZoomCamera camera, int hudHeight) {
		mCtx    	= ctx;
		mCamera 	= camera;
		mFollowingSprites = new HashMap<Sprite,Sprite>();
		mHudHeight = hudHeight;
	}
	
	public void addSprite(Sprite toFollow) {
		Sprite icon = new Sprite(0, 0, toFollow.getTextureRegion(), mCtx.getVertexBufferObjectManager());
		icon.setVisible(false);
		icon.setAlpha(0.5f);
		icon.setZIndex(-1);
		
		mCamera.getHUD().attachChild(icon);
		mFollowingSprites.put(toFollow, icon);
		mCamera.getHUD().sortChildren();
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

		while(keys.hasNext()) {
			Sprite curSprite     = keys.next();
			final Sprite curSpriteIcon = mFollowingSprites.get(curSprite);
			
			if(curSpriteIcon.getTextureRegion() != curSprite.getTextureRegion()) 
				curSpriteIcon.setTextureRegion(curSprite.getTextureRegion());
			
			if(curSpriteIcon.getScaleX() != mCamera.getZoomFactor())
				curSpriteIcon.setScale(mCamera.getZoomFactor());
			
			float sx=curSprite.getX(), sy=curSprite.getY();
			float cxmin=mCamera.getXMin(), cymin=mCamera.getYMin()+mHudHeight/2f, cxmax=mCamera.getXMax(), cymax=mCamera.getYMax();
			
			float sprH = curSprite.getHeight(), sprW = curSprite.getWidth();

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
				if(sy+sprH < cymin)
					posY = mHudHeight;
				else if(sy > cymax)
					posY =MenuActivity.getHeight() - sprH;
				else
					posY = ((sy-cymin)/mCamera.getHeight())*MenuActivity.getHeight()+mHudHeight;
				
				if(sx+sprW < cxmin)
					posX = 0;
				else if(sx > cxmax)
					posX = MenuActivity.getWidth() - sprW;
				else
					posX = ((sx-cxmin)/mCamera.getWidth())*MenuActivity.getWidth();
				
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
