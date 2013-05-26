package com.lortexgames.pokman_final.handlers;

import java.util.Vector;


import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class PlayerHandler {
	private Vector<Body> mPokmans = new Vector<Body>();
	private Vector<Body> mGhosts  = new Vector<Body>();
	
	private Vector2 mGravity = new Vector2(0,0);
	private int mScore=0;
	
	public PlayerHandler() {
		
	}

	public void addPokman(Body pokpok) {
		mPokmans.add(pokpok);
	}
	
	public void addGhost(Body ghost) {
		mGhosts.add(ghost);
	}

	public Vector<Body> getGhosts() {
		return mGhosts;
	}
	
	public Vector<Body> getPokmans() {
		return mPokmans;
	}

	public void newGravity(float newX, float newY) {
		mGravity.set(newX, newY);
		mGravity.mul(5);
		update();
	}
	
	public void update() {
		for(int i=0;i<mPokmans.size();i++)
			mPokmans.get(i).applyForce(mGravity.x, mGravity.y, mPokmans.get(i).getWorldCenter().x, mPokmans.get(i).getWorldCenter().y);
			
		for(int i=0;i<mGhosts.size();i++)
			mGhosts.get(i).applyForce(mGravity.x, mGravity.y, mGhosts.get(i).getWorldCenter().x, mGhosts.get(i).getWorldCenter().y);
	}

	public Vector2 getGravity() {
		return mGravity;
	}

	public void updateScore(int i) {
		mScore += i;
	}
	
	public int getScore() {
		return mScore;
	}
}
