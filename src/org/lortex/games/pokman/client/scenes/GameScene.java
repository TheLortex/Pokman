package org.lortex.games.pokman.client.scenes;

import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.menu.MenuScene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.shader.ShaderProgram;
import org.lortex.games.pokman.client.FontManager;
import org.lortex.games.pokman.client.GameSceneManager;
import org.lortex.games.pokman.client.SceneManagerActivity;
import org.lortex.games.pokman.client.TextureHandler;
import org.lortex.games.pokman.common.GameMode;
import org.lortex.games.pokman.common.MazeGenerator;
import org.lortex.games.pokman.common.Type;
import org.lortex.games.pokman.common.Packets.PlayerDataUpdate;
import org.lortex.games.pokman.server.PokmanServer;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.minlog.Log;

import android.util.SparseArray;



public class GameScene extends PScene {

	private TextureHandler textureHandler;
	private FontManager    fm;
	public int mon_id;
	
	private GameMode m_gm;
	
	private SceneManagerActivity m_context;
	
	private GameSceneManager scenemgr;
	private Scene scene;
	
	
	private MazeGenerator mazegen;
	
	private PokmanServer server;
	
	private long mSeed;
	
	
	private SparseArray<Sprite> entities;
	private Client client;
	
	public GameScene(SceneManagerActivity sceneManagerActivity, GameMode gm, boolean b, String string, long seed) {
		textureHandler = new TextureHandler(sceneManagerActivity);
		fm 			   = new FontManager(sceneManagerActivity);
		
		m_gm = gm;
		m_context = sceneManagerActivity;
		
		mSeed = seed;
		
		mon_id = 1;
		
		entities = new SparseArray<Sprite>();
		
	}

	public SceneManagerActivity getContext() {
		return m_context;
	}

	public TextureHandler getTextureHandler() {
		return textureHandler;
	}

	public FontManager getPokmanFontManager() {
		return fm;
	}

	public GameMode getGameMode() {
		return m_gm;
	}

	public void readyToStart() {
		
	}

	public ShaderProgram getCustomColorShaderProgram() {
		
		return null;
	}
	
	
	@Override
	public void onLoadResources() {
		textureHandler.load("point", "gfx/point.png");
		
		
	}

	@Override
	public void onCreateScene() {

		scene = new Scene();
		scenemgr = new GameSceneManager(this,scene);
		
		
		mazegen = new MazeGenerator(m_gm.level_width, m_gm.level_height, m_gm.level,mSeed);
		
		mazegen.randomize();
		
		scenemgr.renderMap(mazegen);
		
	
		server = new PokmanServer(m_context, m_gm,mSeed);
		server.initialize();
		

		
		client = new Client();
		
		client.connect(arg0, arg1, arg2, arg3);
		
	}

	@Override
	public Scene getScene() {
		return scene;
	}

	@Override
	public void onResume() {}

	@Override
	public void onPause() {	
	}

	@Override
	public void onDestroyScene() {
		scene.detachSelf();	
	}

	@Override
	public void onSignInFailed() {}

	@Override
	public void onSignInSucceeded() {}

	@Override
	public void onBackPressed() {
		m_context.setScene(new NewMenuScene(m_context));
	}

	@Override
	public void onSetupServer(boolean isHost, long seed) {
	}
	
	
	@Override
	public void onSensorUpdate(int id, float x, float y) {
		
	}

	@Override
	public void onReady(int id) {
		
	}

	@Override
	public void onPlayerDataUpdate(PlayerDataUpdate o) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNewPlayer(int id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNewPokman(int entityId, int pokId, int ownerId, float x, float y) {
		Sprite p = scenemgr.pleaseAddThisEntityMyDear(Type.POKMAN, x, y, 0, pokId);
		entities.append(entityId, p);
		scene.attachChild(p);
	}

	@Override
	public void onNewGhost(int entityId, int ghostId, int ownerId, int focusId,float x, float y) {
		Sprite p = scenemgr.pleaseAddThisEntityMyDear(Type.GHOST, x, y, 0, ghostId);
		entities.append(entityId, p);
		scene.attachChild(p);
		
	}

	@Override
	public void onNewPoint(int entityId, float x,float y) {
		
		Sprite p = scenemgr.pleaseAddThisEntityMyDear(Type.POINT, x, y, 0, entityId);
		entities.append(entityId, p);
		scene.attachChild(p);
	}

	@Override
	public void onNewBonus(int entityId, float x, float y) {
		Sprite p = scenemgr.pleaseAddThisEntityMyDear(Type.BONUS, x, y, 0, entityId);
		entities.append(entityId, p);
		scene.attachChild(p);
	}

	@Override
	public void onEntityUpdate(int id, float x, float y, float rotation) {
		scenemgr.pleaseMoveThisEntityMyDear(entities.get(id), x, y, rotation);
	}

	@Override
	public void onPingUpdate(float ping) {}

}
