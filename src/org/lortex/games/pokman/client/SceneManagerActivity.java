package org.lortex.games.pokman.client;

import org.andengine.engine.camera.SmoothCamera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.lortex.games.pokman.R;
import org.lortex.games.pokman.client.scenes.GameScene;
import org.lortex.games.pokman.client.scenes.IPScene;
import org.lortex.games.pokman.client.scenes.NewMenuScene;
import org.lortex.games.pokman.common.GameMode;
import org.lortex.games.pokman.common.Packets.PlayerDataUpdate;
import org.lortex.games.pokman.online.AchievementsManager;
import org.lortex.games.pokman.online.MultiplayerHandler;
import org.lortex.games.pokman.online.MultiplayerListener;

import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.example.games.basegameutils.GameHelper;
import com.google.example.games.basegameutils.GameHelper.GameHelperListener;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;

public class SceneManagerActivity extends SimpleBaseGameActivity implements GameHelperListener, MultiplayerListener{
	private IPScene currentScene;
	private SmoothCamera mCamera;
	private static int mScreenHeight=1280;

	private GameHelper mGameHelper;
	private MultiplayerHandler mMultiplayerHandler;
	
	private boolean mGamesAutoConnect;
	
	private long timeBegin;
	
	private AchievementsManager mAchievementsManager;
	
	@Override 
	public void onCreate(Bundle b) {
		super.onCreate(b);
		
		mGameHelper = new GameHelper(this, GameHelper.CLIENT_GAMES);
		mGameHelper.setup(this);

		mMultiplayerHandler = new MultiplayerHandler(this, mGameHelper, this);
		
		mAchievementsManager = new AchievementsManager(this);
		currentScene = new NewMenuScene(this);
	}
	
	@Override
    protected void onStart() {
        super.onStart();
        mGameHelper.onStart(this);

        SharedPreferences settings = this.getSharedPreferences("org.lortex.games.pokman", MODE_PRIVATE);
        mGamesAutoConnect = settings.getBoolean("gamesautoconnect", false);
        
        
    } 
	
	@Override
    protected void onStop() {
        super.onStop();
        mMultiplayerHandler.onStop();
        mGameHelper.onStop();
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		currentScene.onResume();
		if(!mGameHelper.isSignedIn() && mGamesAutoConnect)
			mGameHelper.beginUserInitiatedSignIn();
		
		timeBegin = System.nanoTime();
	}
	
	@Override 
	protected void onPause() {
		super.onPause();
		currentScene.onPause();
		SharedPreferences settings = this.getSharedPreferences("org.lortex.games.pokman", MODE_PRIVATE);
        Editor editor = settings.edit();
        editor.putBoolean("gamesautoconnect", mGamesAutoConnect);
        editor.commit();
        
        int timeElapsedMillis = (int) ((System.nanoTime() - timeBegin)/1000000);
        if(mGameHelper.isSignedIn())
        	Games.Events.increment(mGameHelper.getApiClient(), this.getResources().getString(R.string.event_temps_pass_en_jeu), timeElapsedMillis);
        
	}
	
	@Override
	public EngineOptions onCreateEngineOptions() {
		this.mCamera = new SmoothCamera(0, 0, getWidth(), getHeight(),2500,2500, 10);
		return new EngineOptions(true, ScreenOrientation.PORTRAIT_SENSOR, new RatioResolutionPolicy(getWidth(), getHeight()), this.mCamera);
	}

	@Override
	public void onCreateResources() {
		currentScene.onLoadResources();
	}
	
	public void showMenuScene() {
		if(!(currentScene instanceof NewMenuScene))
			setScene(new NewMenuScene(this));
	}
	
	public void setScene(final IPScene newScene) {
		this.runOnUpdateThread(new Runnable() {
			@Override
			public void run() {
				currentScene.onPause();
				currentScene.onDestroyScene();
				currentScene = newScene;
				//TODO: Loading bar;
				currentScene.onLoadResources();
				currentScene.onCreateScene();
				mEngine.setScene(currentScene.getScene());
				currentScene.onResume();
			}
		});
	}
	
	public void onBackPressed() {
		currentScene.onBackPressed();
	}

	@Override
	public Scene onCreateScene() {
		currentScene.onCreateScene();
		return currentScene.getScene(); 
	}
	
	@Override
	public void onDestroy() {
		currentScene.onDestroyScene();
		super.onDestroy();
	}
	
	public int getWidth() {
		DisplayMetrics metrics = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(metrics);		
		return mScreenHeight*metrics.widthPixels/metrics.heightPixels;
	}
	
	public static int getWidth(Activity ctx) {
		DisplayMetrics metrics = new DisplayMetrics();
		ctx.getWindowManager().getDefaultDisplay().getMetrics(metrics);		
		return mScreenHeight*metrics.widthPixels/metrics.heightPixels;
	}
	
	public static int getHeight() {
		return mScreenHeight;
	}
	

	public GameHelper getGameHelper() {
		return mGameHelper;
	}

	public MultiplayerHandler getMultiplayerHandler() {
		return mMultiplayerHandler;
	}

	public SmoothCamera getCamera() {
		return mCamera;
	}

	@Override
	public void onSignInFailed() {
		currentScene.onSignInFailed();
	}

	@Override
	public void onSignInSucceeded() {
		currentScene.onSignInSucceeded();
	}
	@Override
    protected void onActivityResult(int request, int response, Intent data) {
        super.onActivityResult(request, response, data);
        mGameHelper.onActivityResult(request, response, data);
        
        switch (request) {
        case MultiplayerHandler.RC_SELECT_PLAYERS:
            mMultiplayerHandler.handleSelectPlayersResult(response, data);
            break;
        case MultiplayerHandler.RC_INVITATION_INBOX:
        	mMultiplayerHandler.handleInvitationInboxResult(response, data);
            break;
        case MultiplayerHandler.RC_WAITING_ROOM:
            if (response == Activity.RESULT_OK) {
                mMultiplayerHandler.handleStartingGame();
            } else if (response == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
            	mMultiplayerHandler.leaveRoom();
            	showMenuScene();
            } else if (response == Activity.RESULT_CANCELED) {
            	mMultiplayerHandler.leaveRoom();
            	showMenuScene();
            }
            break;
    }
    }
	@Override
	public void onSetupServer(boolean isHost, long seed) {
		currentScene.onSetupServer(isHost, seed);
		
		unlockMultiplayerAchievements();
		GameMode gm = new GameMode(GameMode.ONLINE_MULTIPLAYER_TEST, 1);
		gm.authoritative = isHost;
		setScene(new GameScene(this, gm,true,"127.0.0.1",seed));
	}

	public void unlockMultiplayerAchievements() {
		mAchievementsManager.unlock(R.string.achievement_plus_on_est_de_fous_plus_on_rit);
		mAchievementsManager.increment(R.string.achievement_comptitif, 1);
	}

	@Override
	public void onSensorUpdate(int id, float x, float y) {
		currentScene.onSensorUpdate(id, x, y);
	}

	public void setAutoConnect(boolean b) {
		mGamesAutoConnect = b;
	}
	
	public boolean getAutoConnect() {
		return mGamesAutoConnect;
	}
	
	@Override
	public void onReady(final int id) {
		Log.i("LORTEX", "SceneManager::onReady("+id+")");
		if(currentScene instanceof GameScene) {
			currentScene.onReady(id);
		} else {
			new Thread(new Runnable() {
				@Override
				public void run() {
					while(true) {
						if(currentScene instanceof GameScene) {
							currentScene.onReady(id);
							return;
						}
						
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							e.printStackTrace();
							return;
						}
					}
				}
			}).start();
		}
	}

	@Override
	public void onLeftRoom() {
    	showMenuScene();
	}

	@Override
	public void onPlayerDataUpdate(PlayerDataUpdate o) {
		currentScene.onPlayerDataUpdate(o);
	}
	
	public AchievementsManager getAchievementsManager() {
		return mAchievementsManager;
	}

	@Override
	public void onNewPlayer(int id) {
		currentScene.onNewPlayer(id);
	}

	@Override
	public void onNewPokman(int entityId, int pokId, int ownerId, float x, float y) {
		currentScene.onNewPokman(entityId, pokId, ownerId, x, y);
	}

	@Override
	public void onNewGhost(int entityId, int ghostId, int ownerId, int focusId,
			float x, float y) {
		currentScene.onNewGhost(entityId, ghostId, ownerId, focusId, x , y);
	}

	@Override
	public void onNewPoint(final int entityId, final float x, final float y) {
		this.runOnUpdateThread(new Runnable() {
			@Override
			public void run() {
				currentScene.onNewPoint(entityId, x, y);
			}
		});
	}

	@Override
	public void onNewBonus(int entityId, float x, float y) {
		currentScene.onNewBonus(entityId, x, y);
	}

	@Override
	public void onEntityUpdate(int id, float x, float y, float rotation) {
		currentScene.onEntityUpdate(id, x, y, rotation);
	}
	
	@Override
	public void updatePing(float ping) {
		currentScene.onPingUpdate(ping);
	}
}
