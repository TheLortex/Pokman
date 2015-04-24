package org.lortex.games.pokman.online;

import org.lortex.games.pokman.client.SceneManagerActivity;

import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.GameHelper;

public class AchievementsManager {
	private SceneManagerActivity mContext;
	private GameHelper mGameHelper;
	
	private boolean rush;
	private boolean master;
	
	public AchievementsManager(SceneManagerActivity context) {
		mContext = context;
		mGameHelper = mContext.getGameHelper();
	}
	
	public void setRush(boolean r) {
		rush = r;
	}
	
	public void setMaster(boolean m) {
		master = m;
	}
	
	public boolean isRush() {
		return rush;
	}
	
	public boolean isMaster() {
		return master;
	}
	
	public void unlock(int rId) {
		unlock(mContext.getResources().getString(rId));
	}
	
	private void unlock(String id) {
		if(mGameHelper.isSignedIn()) {
			Games.Achievements.unlock(mGameHelper.getApiClient(), id);
		} else { //TODO: save and unlock later
			
		}
	}
	
	public void increment(int rId, int by) {
		increment(mContext.getResources().getString(rId),by);
	}

	private void increment(String id, int by) {
		if(mGameHelper.isSignedIn()) {
			Games.Achievements.increment(mGameHelper.getApiClient(), id, by);
		} else { //TODO: save and unlock later
			
		}
	}
}
