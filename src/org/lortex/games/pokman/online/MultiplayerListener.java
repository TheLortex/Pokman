package org.lortex.games.pokman.online;

import org.lortex.games.pokman.common.Packets.PlayerDataUpdate;

public interface MultiplayerListener {
	void onSetupServer(boolean isHost, long seed);
	void onSensorUpdate(int id, float x, float y);
	
	void onNewPlayer(int id);
	void onNewPokman(int entityId, int pokId, int ownerId, float x, float y);
	void onNewGhost (int entityId, int ghostId, int ownerId, int focusId, float x, float y);
	void onNewPoint (int entityId, float x, float y);
	void onNewBonus (int entityId, float x, float y);
	


	void onReady(int id);
	void onLeftRoom();
	void onPlayerDataUpdate(PlayerDataUpdate o);
	void onEntityUpdate(int id, float x, float y, float rotation);
	void updatePing(float f);
}
