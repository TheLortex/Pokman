package org.lortex.games.pokman.server;

import java.util.Vector;

import com.badlogic.gdx.math.Vector2;

public class Player {
	public int id;
	public Vector<GameEntity> controlledEntities;
	public Vector2 sensorData;
	public String name;
	
	public Player() {
		controlledEntities = new Vector<GameEntity>();
		sensorData = new Vector2();
	}
}
