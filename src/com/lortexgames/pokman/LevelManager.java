package com.lortexgames.pokman;

import android.util.SparseArray;

public class LevelManager {
	private SparseArray<LevelSet> values;
	public final static int MAX_LEVEL=10;
	
	public LevelManager() {
		values = new SparseArray<LevelSet>();
		loadValues();
	}
	
	public void loadValues() {
		LevelSet level1 = new LevelSet();
		level1.maxWallLenght=7;
		level1.ghostAttractDivFactor=10000f;
		level1.ghostGravityScale=0.4f;
		level1.pacmanGravityScale=1.1f;
		
		LevelSet level2 = new LevelSet();
		level2.maxWallLenght=5;
		level2.ghostAttractDivFactor=10000f;
		level2.ghostGravityScale=0.7f;
		level2.pacmanGravityScale=1.2f;
		
		LevelSet level3 = new LevelSet();
		level3.maxWallLenght=9;
		level3.ghostAttractDivFactor=100f;
		level3.ghostGravityScale=0.8f;
		level3.pacmanGravityScale=1.5f;
		
		LevelSet level4 = new LevelSet();
		level4.maxWallLenght=5;
		level4.ghostAttractDivFactor=15f;
		level4.ghostGravityScale=0.9f;
		level4.pacmanGravityScale=1.1f;
		
		LevelSet level5 = new LevelSet();
		level5.maxWallLenght=3;
		level5.ghostAttractDivFactor=10f;
		level5.ghostGravityScale=0.2f;
		level5.pacmanGravityScale=1.1f;
		
		LevelSet level6 = new LevelSet();
		level6.maxWallLenght=11;
		level6.ghostAttractDivFactor=5f;
		level6.ghostGravityScale=1f;
		level6.pacmanGravityScale=1.2f;
		
		LevelSet level7 = new LevelSet();
		level7.maxWallLenght=5;
		level7.ghostAttractDivFactor=20f;
		level7.ghostGravityScale=1.2f;
		level7.pacmanGravityScale=1f;
		
		LevelSet level8 = new LevelSet();
		level8.maxWallLenght=7;
		level8.ghostAttractDivFactor=3f;
		level8.ghostGravityScale=0.75f;
		level8.pacmanGravityScale=1f;
		
		LevelSet level9 = new LevelSet();
		level9.maxWallLenght=5;
		level9.ghostAttractDivFactor=2f;
		level9.ghostGravityScale=0.65f;
		level9.pacmanGravityScale=1f;
		
		LevelSet level10 = new LevelSet();
		level10.maxWallLenght=3;
		level10.ghostAttractDivFactor=1f;
		level10.ghostGravityScale=0.2f;
		level10.pacmanGravityScale=1.5f;
		
		
		

		values.put(1, level1);
		values.put(2, level2);
		values.put(3, level3);
		values.put(4, level4);
		values.put(5, level5);
		values.put(6, level6);
		values.put(7, level7);
		values.put(8, level8);
		values.put(9, level9);
		values.put(10, level10);
	}
	
	public LevelSet get(int index) {
		if(index > MAX_LEVEL) 
			index = MAX_LEVEL;

		return values.get(index);
	}
	
	public int getMaxLevel() {
		return MAX_LEVEL;
	}
}
