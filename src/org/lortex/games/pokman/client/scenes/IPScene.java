package org.lortex.games.pokman.client.scenes;

import org.andengine.entity.scene.Scene;
import org.lortex.games.pokman.common.Packets.EntityDeleted;
import org.lortex.games.pokman.common.Packets.EntityMoved;
import org.lortex.games.pokman.common.Packets.NewEntity;
import org.lortex.games.pokman.common.Packets.PlayerDataUpdate;

public interface IPScene {
	
	public void onLoadResources();
	public void onCreateScene();
	public Scene getScene();
	
	public void onResume();
	public void onPause();
	
	public void onDestroyScene();
	
	public void onSignInFailed();
	public void onSignInSucceeded();
	public void onBackPressed();
	
	public void onSetupServer(boolean isHost, long seed);
	public void onSensorUpdate(int id, float x, float y);

	public void onReady(int id);
	public void onPlayerDataUpdate(PlayerDataUpdate o);
	public void onNewPlayer(int id);
	public void onNewPokman(int entityId, int pokId, int ownerId, float x, float y);
	public void onNewGhost(int entityId, int ghostId, int ownerId, int focusId, float x, float y);
	public void onNewPoint(int entityId, float x, float y);
	public void onNewBonus(int entityId, float x, float y);
	public void onEntityUpdate(int id, float x, float y, float rotation);
	public void onPingUpdate(float ping);
}
