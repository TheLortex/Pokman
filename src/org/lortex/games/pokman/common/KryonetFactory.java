package org.lortex.games.pokman.common;

import org.lortex.games.pokman.common.Packets.BonusActivated;
import org.lortex.games.pokman.common.Packets.BonusEnded;
import org.lortex.games.pokman.common.Packets.BonusNearEnd;
import org.lortex.games.pokman.common.Packets.EntityDeleted;
import org.lortex.games.pokman.common.Packets.EntityMoved;
import org.lortex.games.pokman.common.Packets.GhostDeathPacket;
import org.lortex.games.pokman.common.Packets.GhostRevivePacket;
import org.lortex.games.pokman.common.Packets.GhostWillReviveSoonPacket;
import org.lortex.games.pokman.common.Packets.HiHisNameIs;
import org.lortex.games.pokman.common.Packets.HiMyNameIs;
import org.lortex.games.pokman.common.Packets.MapInfoPacket;
import org.lortex.games.pokman.common.Packets.NewEntity;
import org.lortex.games.pokman.common.Packets.NewMap;
import org.lortex.games.pokman.common.Packets.NewMapEnd;
import org.lortex.games.pokman.common.Packets.PlayerDataUpdate;
import org.lortex.games.pokman.common.Packets.SensorChanged;
import org.lortex.games.pokman.common.Packets.TakeThisId;
import org.lortex.games.pokman.common.Packets.VulnerableGhost;
import org.lortex.games.pokman.common.Packets.VulnerableGhostEnded;
import org.lortex.games.pokman.common.Packets.VulnerableGhostNearEnd;

import com.esotericsoftware.kryo.Kryo;

public final class KryonetFactory {
	public static void setup(Kryo kryo) {
		if(kryo == null)
			return;
		kryo.register(TakeThisId.class);
		kryo.register(HiMyNameIs.class);
		kryo.register(HiHisNameIs.class);
		kryo.register(NewMap.class);
		kryo.register(NewMapEnd.class);
		kryo.register(MapInfoPacket.class);
		kryo.register(SensorChanged.class);
		kryo.register(Element.class);
		kryo.register(NewEntity.class);
		kryo.register(EntityMoved.class);
		kryo.register(EntityDeleted.class);
		kryo.register(BonusActivated.class);
		kryo.register(BonusNearEnd.class);
		kryo.register(BonusEnded.class);
		kryo.register(PlayerDataUpdate.class);
		kryo.register(GhostDeathPacket.class);
		kryo.register(GhostWillReviveSoonPacket.class);
		kryo.register(GhostRevivePacket.class);
		kryo.register(VulnerableGhost.class);
		kryo.register(VulnerableGhostNearEnd.class);
		kryo.register(VulnerableGhostEnded.class);
	}
}
