package org.lortex.games.pokman.server;

import java.util.Vector;

import org.lortex.games.pokman.common.GameMode;
import org.lortex.games.pokman.common.Type;

import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;

import android.os.Handler;
import android.os.Looper;
import android.util.SparseIntArray;



public class GameEventsManager {
	private PokmanServer mContext;
	private SparseIntArray status = new SparseIntArray();
	private int mBonusStack = 0;
	
	private LevelSet current_level;
	
	public static final int OKAY=0;
	public static final int VULNERABLE=1;
	public static final int DEAD=2;
	
	public GameEventsManager(PokmanServer context) {
		mContext = context;

		current_level = (new LevelManager()).get(mContext.getLevel());
	}
	
	public int getStatusOfEntity(int id) {
		return status.get(id, OKAY);
	}
	
	public boolean entityDied(final GameEntity who, final GameEntity by) {
		if(who.type == Type.BONUS) {
			mContext.entityDied(who, by);
			Vector<GameEntity> affectedGhosts = mContext.bonusActivated(by.id);
			for(GameEntity ge : affectedGhosts) {
				if(status.get(ge.id, OKAY) == OKAY) {
					status.put(ge.id, VULNERABLE);
					mContext.vulnerableGhost(ge.id);
				}
			}
			mBonusStack++;
			
			new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
				@Override
				public void run() {
					if(mContext != null) {
						if(mContext.getContext() != null) {
							mContext.getContext().runOnUpdateThread(new Runnable() {
								@Override
								public void run() {
									if(mBonusStack == 1) {
										mContext.bonusNearEnd(by.id);
										for(int i=0;i<status.size();i++) {
											if(status.valueAt(i) == VULNERABLE) {
												mContext.vulnerableGhostNearEnd(status.keyAt(i));
											}
										}
									}
								}
							});
						}
					}
				}
			},current_level.blinkTime);
			

			new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
				@Override
				public void run() {
					if(mContext != null)
						if(mContext.getContext() != null)
							mContext.getContext().runOnUpdateThread(new Runnable() {
								@Override
								public void run() {
									if(mBonusStack == 1) {
										mContext.bonusEnded(by.id);
										
										for(int i=0;i<status.size();i++) {
											if(status.valueAt(i) == VULNERABLE) {
												mContext.vulnerableGhostEnded(status.keyAt(i));
												status.put(status.keyAt(i), OKAY);
											}
										}
									}
									mBonusStack--;
								}
							});
				}
			},current_level.vulDuration);
			
			return true;
		} else if(who.type == Type.POINT) {
			mContext.entityDied(who, by);
			mContext.pointEaten(by.id);
			return true;
		} else if(who.type == Type.POKMAN) {
			mContext.entityDied(who, by);
			return true;
		} else if(who.type == Type.GHOST) {
			if(status.get(who.id) == DEAD)
				return false;
			
			status.put(who.id, DEAD); 
			
			
			mContext.ghostDeath(who.data, by.id);
			
			for(Fixture f : who.body.getFixtureList()) {
				Filter filter = f.getFilterData();
				filter.maskBits 	= GameWorld.MASKBITS_GHOST_DEAD;
				filter.categoryBits = GameWorld.CATEGORYBIT_GHOST_DEAD;
				f.setFilterData(filter);
			}
			
			new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
				@Override
				public void run() {
					if(mContext != null)
						if(mContext.getContext() != null)
							mContext.getContext().runOnUpdateThread(new Runnable() {
								@Override
								public void run() {
									mContext.ghostWillReviveSoon(who.data);
								}
							});
				}
			},27000);
			
			new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
				@Override
				public void run() {
					if(mContext != null)
						if(mContext.getContext() != null)
							mContext.getContext().runOnUpdateThread(new Runnable() {
								@Override
								public void run() {
									mContext.ghostRevive(who.data);
									status.put(who.id, OKAY);
									
									for(Fixture f : who.body.getFixtureList()) {
										Filter filter = f.getFilterData();
										filter.maskBits 	= GameWorld.MASKBITS_GHOST;
										filter.categoryBits = GameWorld.CATEGORYBIT_GHOST;
										f.setFilterData(filter);
									}
								}
							});
				}
			},30000);
			return false;
		}
		return true;
	}
	
	public GameEntity whoShouldDie(GameEntity a, GameEntity b) {
		GameEntity pokman = (a.type == Type.POKMAN) ? a : b;
		GameEntity ghost  = (a.type == Type.GHOST)  ? a : b;
		
		if(status.get(ghost.id, OKAY) == DEAD)
			return null;
		
		if(status.get(ghost.id, OKAY) == VULNERABLE) {
			return ghost;
		} else {
			return pokman;
		}
	}
	
	public GameEntity whoShouldNotDie(GameEntity a, GameEntity b) {
		GameEntity pokman = (a.type == Type.POKMAN) ? a : b;
		GameEntity ghost  = (a.type == Type.GHOST)  ? a : b;

		if(status.get(ghost.id, OKAY) == DEAD)
			return null;
		
		if(status.get(ghost.id, OKAY) == VULNERABLE){
			return pokman;
		} else {
			return ghost;
		}
	}
}
