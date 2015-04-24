package org.lortex.games.pokman.online;

import java.io.Serializable;

public class OnlinePackets {


	public static class Init implements Serializable {
		private static final long serialVersionUID = -2999374940976167646L;
		
		public long seed;
	}
	
	public static class SensorUpdate implements Serializable {
		private static final long serialVersionUID = -5973029460730540782L;
		
		public float  x;
		public float  y;
		public int tick;
	}
	
	public static class ArmedAndReady implements Serializable {
		private static final long serialVersionUID = -321746570978151191L;
		public int gluc;
	}

	public static class Ping implements Serializable {
		private static final long serialVersionUID = -7964781116534116878L;
		public long time;
	}

	
	public static class NewPokman implements Serializable {
		private static final long serialVersionUID = 4575912018457022362L;

		
		public int entityId;
		public int pokId;
		public int ownerId;
		public float x;
		public float y;
		
		
		public NewPokman(int entityId_, int pokId_, int id_, float x_, float y_) {
			entityId = entityId_;pokId = pokId_;ownerId = id_;x = x_;y = y_;
		}	
	}
	
	public static class NewGhost implements Serializable {
		private static final long serialVersionUID = -5378156242542984091L;
		public int entityId;
		public int ghostId;
		public int ownerId;
		public int focusId;
		public float x;
		public float y;
		
		public NewGhost(int entityId_, int ghostId_, int ownerId_, int focusId_, float x_, float y_) {
			entityId = entityId_;ghostId = ghostId_;ownerId = ownerId_; focusId = focusId_;x = x_;y = y_;
		}
	}

	public static class EntityUpdate implements Serializable {
		private static final long serialVersionUID = -5102630089763135021L;
		
		public int id;
		public float x;
		public float y;
		public float rotation;

		public EntityUpdate(int id_, float x_, float y_, float rotation_) {
			id = id_;
			x = x_;
			y = y_;
			rotation = rotation_;
		}


	}
	
	public static class NewPlayer implements Serializable {
		private static final long serialVersionUID = -3554325536705813806L;

		public int id;
		
		public NewPlayer(int id_) {
			id = id_;
		}

	}

}
