package org.lortex.games.pokman.client;

import org.lortex.games.pokman.online.MultiplayerHandler;

import android.app.Application;

import com.google.example.games.basegameutils.GameHelper;


public class AppInterface extends Application {

	private GameHelper mGH;
	private MultiplayerHandler mMH;
	public AppInterface() {}
	

	public void setGameHelper(GameHelper gh) {
		mGH = gh;
	}
	
	public void setMultiplayerHandler(MultiplayerHandler mh) {
		mMH = mh;
	}
	
	public GameHelper getGameHelper() {
		return mGH;
	}


	public MultiplayerHandler getMultiplayerHandler() {
		return mMH;
	}
}