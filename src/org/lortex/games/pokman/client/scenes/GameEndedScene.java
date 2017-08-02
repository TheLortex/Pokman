package org.lortex.games.pokman.client.scenes;

import javax.microedition.khronos.opengles.GL10;

import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.menu.MenuScene;
import org.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.andengine.entity.scene.menu.animator.InstantMenuSceneAnimator;
import org.andengine.entity.scene.menu.item.IMenuItem;
import org.andengine.entity.scene.menu.item.TextMenuItem;
import org.andengine.entity.scene.menu.item.decorator.ColorMenuItemDecorator;
import org.andengine.entity.text.Text;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.util.adt.align.HorizontalAlign;
import org.andengine.util.adt.align.VerticalAlign;
import org.andengine.util.adt.color.Color;
import org.lortex.games.pokman.R;
import org.lortex.games.pokman.client.SceneManagerActivity;
import org.lortex.games.pokman.common.GameMode;

import com.google.android.gms.games.Games;


public class GameEndedScene extends PScene implements IOnMenuItemClickListener {
	SceneManagerActivity mContext;
	private static final int MENU_RESTART = 0;
	private static final int MENU_LEADERBOARD = 1;
	private static final int MENU_BACK = 2;

	protected Scene mMainScene;

    private Font menuPlayFont;
    private Font menuQuitFont;
    private Font menuTitleFont;

	private int score;
	private int level;

	private MenuScene mMenuScene;
	
	
	public GameEndedScene(SceneManagerActivity context, int score_p, int level_p) {
		mContext = context;
		score = score_p;
		level = level_p;
	}
	
	@Override
	public void onLoadResources() {
		FontFactory.setAssetBasePath("font/");

		this.menuPlayFont = FontFactory.createFromAsset(mContext.getFontManager(), mContext.getTextureManager(), 512, 512, TextureOptions.BILINEAR, mContext.getAssets(), "police.ttf", 54, true, android.graphics.Color.WHITE);
		this.menuPlayFont.load();
		
		this.menuQuitFont = FontFactory.createFromAsset(mContext.getFontManager(), mContext.getTextureManager(), 256, 256, TextureOptions.BILINEAR, mContext.getAssets(), "police.ttf", 48, true, android.graphics.Color.WHITE);
		this.menuQuitFont.load();
		
		this.menuTitleFont = FontFactory.createFromAsset(mContext.getFontManager(), mContext.getTextureManager(), 512, 512, TextureOptions.BILINEAR, mContext.getAssets(), "police.ttf", 80, true, android.graphics.Color.YELLOW);
		this.menuTitleFont.load();
	}

	@Override
	public void onCreateScene() {
		this.mMainScene = new Scene();
		this.mMainScene.getBackground().setColor(0, 0, 0);
		
		Text title = new Text(SceneManagerActivity.getWidth(mContext)/2.0f, SceneManagerActivity.getHeight()-100, menuTitleFont, "YOU DIED", mContext.getVertexBufferObjectManager());
		mMainScene.attachChild(title);
		
		Text scoreText = new Text(SceneManagerActivity.getWidth(mContext)/2.0f, SceneManagerActivity.getHeight()-400, menuPlayFont, "With a score \nof "+score+"\n at level "+level, mContext.getVertexBufferObjectManager());
		mMainScene.attachChild(scoreText);

		createMenuScene();
		this.mMainScene.setChildScene(this.mMenuScene, false, true, true);
	}


	protected void createMenuScene() {
		this.mMenuScene = new MenuScene(mContext.getCamera());
	
		
		final IMenuItem  reloadLevelMenuItem = new ColorMenuItemDecorator (new TextMenuItem(MENU_RESTART, this.menuPlayFont, " REJOUER\nLE NIVEAU",mContext.getVertexBufferObjectManager()), new Color(0.9f,0.5f,0.1f), new Color(1,1,1));
		reloadLevelMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		this.mMenuScene.addMenuItem(reloadLevelMenuItem);
	
		
		if(mContext.getGameHelper().isSignedIn()) {
			final IMenuItem  leaderBoardMenuItem = new ColorMenuItemDecorator (new TextMenuItem(MENU_LEADERBOARD, this.menuPlayFont, " VOIR  LE\nCLASSEMENT",mContext.getVertexBufferObjectManager()), new Color(0.9f,0.5f,0.1f), new Color(1,1,1));
			leaderBoardMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			this.mMenuScene.addMenuItem(leaderBoardMenuItem);
		}
			
		final IMenuItem  backMenuItem = new ColorMenuItemDecorator (new TextMenuItem(MENU_BACK, this.menuQuitFont, "RETOUR",mContext.getVertexBufferObjectManager()), new Color(0.9f,0.5f,0.1f), new Color(1,1,1));
		backMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		this.mMenuScene.addMenuItem(backMenuItem);
	
	
		this.mMenuScene.setBackgroundEnabled(false);
		InstantMenuSceneAnimator lremv = new InstantMenuSceneAnimator();
		lremv.setMenuItemSpacing(200);
		lremv.setHorizontalAlign(HorizontalAlign.CENTER);
		lremv.setVerticalAlign(VerticalAlign.CENTER);
		lremv.setOffsetY(-300);
		this.mMenuScene.setMenuSceneAnimator(lremv);
		this.mMenuScene.buildAnimations();

		this.mMenuScene.setOnMenuItemClickListener(this);
	}

	@Override
	public boolean onMenuItemClicked(MenuScene pMenuScene, IMenuItem pMenuItem, float pMenuItemLocalX, float pMenuItemLocalY) {
		switch(pMenuItem.getID()) {
			case MENU_RESTART:
				GameMode gm = new GameMode(GameMode.SINGLEPLAYER, level);
				mContext.setScene(new GameScene(mContext, gm, true, "127.0.0.1", 0));
				break;
			case MENU_LEADERBOARD:
				if(mContext.getGameHelper().isSignedIn())
					mContext.startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mContext.getGameHelper().getApiClient(),mContext.getResources().getString(R.string.leaderboard_high_scores)), NewMenuScene.REQUEST_LEADERBOARD);
				else
					mContext.getGameHelper().beginUserInitiatedSignIn();
				break;
			case MENU_BACK:
				mContext.setScene(new NewMenuScene(mContext));
				break;
		}
		return true;
	}
	
	@Override
	public Scene getScene() {
		return mMainScene;
	}

	@Override
	public void onDestroyScene() {
		mMainScene.detachChildren();
	}
	
	@Override
	public void onBackPressed() {
		mContext.setScene(new NewMenuScene(mContext));
	}
}
