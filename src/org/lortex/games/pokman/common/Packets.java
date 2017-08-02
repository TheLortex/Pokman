package org.lortex.games.pokman.common;

import java.io.Serializable;

public class Packets {
	// ----- Server responses -----
		public static class TakeThisId {
			public int id;
		}
		
		public static class HiHisNameIs {
			public int id;
			public String name;
		}
		
		public static class MapInfoPacket {
			public int x;
			public int y;
			public Element data;
		}

		public static class NewMap {
			public int height;
			public int width;
		}
		
		public static class BonusActivated {public int id;}
		public static class BonusNearEnd {public int id;}
		public static class BonusEnded {public int id;}
		
		public static class VulnerableGhost 		{public int data_ghost;}
		public static class VulnerableGhostNearEnd 	{public int data_ghost;}
		public static class VulnerableGhostEnded 	{public int data_ghost;}
		
		
		public static class NewMapEnd {}
		
		public static class NewEntity implements Serializable {
			private static final long serialVersionUID = 9048074233597636888L;
			public int data;
			public int id;
			public int type;
			public float x;
			public float y;
			public float rotation;
		}
		
		public static class EntityMoved implements Serializable {
			private static final long serialVersionUID = -4896812007346940594L;
			public float y;
			public int id;
			public float x;
			public float rotation;
		}

		public static class EntityDeleted implements Serializable {
			private static final long serialVersionUID = -6981756759679973760L;
			public int id;
			public int id_cause;
		}
		
		public static class PlayerDataUpdate implements Serializable {
			private static final long serialVersionUID = -6895019484008400599L;
			public int id;
			public int score;
			public int nlifes;
		}

		public static class GhostDeathPacket {
			public int data;
		}
		
		public static class GhostWillReviveSoonPacket {
			public int data;
		}
		
		public static class GhostRevivePacket {
			public int data;
		}
		
		// ------ Client requests
		public static class HiMyNameIs {
			public int id;
			public String name;
		}
		
		public static class SensorChanged {
			public float x;
			public float y;
			
			public int id;
		}
}
