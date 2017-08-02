package org.lortex.games.pokman.server;


public class IDPool {

	private int mCount;
	public IDPool() {
		mCount = 0;
	}
	
	public int generate() {
		mCount++;

		return mCount;
	}
}
