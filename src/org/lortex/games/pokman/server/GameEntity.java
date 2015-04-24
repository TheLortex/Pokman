package org.lortex.games.pokman.server;

import com.badlogic.gdx.physics.box2d.Body;

public class GameEntity {
	public Body body;
	public int type;
	public int id;
	public int data;
	protected GameEntity focus;
}
