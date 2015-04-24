package org.lortex.games.pokman.server;

import java.io.IOException;
import java.util.Collections;
import java.util.Vector;

import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
import org.andengine.util.debug.Debug;
import org.lortex.games.pokman.R;
import org.lortex.games.pokman.client.SceneManagerActivity;
import org.lortex.games.pokman.client.TileMapping;
import org.lortex.games.pokman.client.scenes.GameScene;
import org.lortex.games.pokman.common.Element;
import org.lortex.games.pokman.common.GameMode;
import org.lortex.games.pokman.common.KryonetFactory;
import org.lortex.games.pokman.common.MazeGenerator;
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
import org.lortex.games.pokman.common.Type;
import org.lortex.games.pokman.online.OnlinePackets;

import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

public class PokmanServer implements IUpdateHandler {

	private SparseArray<Player> players;
	private Vector<Vector<Element>> map;
	private Vector<GameEntity> points;
	private Vector<GameEntity> bonus;
	
	private Server mServer;
	private Vector<Vector2> playersSpawnPoints;
	private Vector<Vector2> ghostSpawnPoints;
	
	private SceneManagerActivity mContext;
	
	private GameWorld mGameWorld;
	
	private EntityMoved theRequest = new EntityMoved();

	private IDPool id ;
	private IDPool ghost_number;
	private IDPool pokman_number;
	private IDPool player_id;

	private SparseIntArray nLifes;
	private SparseIntArray scores;
	
	private GameMode mGamemode;
	private LevelSet current_level;
	
	public long mSeed;
	
	private int nGhostsKilled = 0;
	private int mGhostStreak = 0;

	private long timeBegin;
	
	private SparseArray<GameEntity> entities;
	
	private Vector<Integer> mPlayersToAdd = new Vector<Integer>();
	
	private boolean isAuthoritative = true;
	private boolean mReady = false;
	private boolean mAllReady = false;
	private Vector<Integer> mPendingPlayers;
	
	public PokmanServer(SceneManagerActivity mContext2, GameMode gamemode){
		this(mContext2, gamemode, System.currentTimeMillis());
	}
	
	
	public PokmanServer(SceneManagerActivity context, GameMode gamemode, long seed) {
		mSeed = seed;
		mContext = context;
		mGamemode = gamemode;
		isAuthoritative = mGamemode.authoritative;
		
		current_level = new LevelManager().get(mGamemode.level);
		
		initialize();
		mServer = new Server();
		mServer.addListener(new NetworkListener());
		KryonetFactory.setup(mServer.getKryo());
		mServer.start();
		try {
			mServer.bind(45567,45568);
			Debug.i("LORTEX", "[Server] Serveur démarré avec succès!");
		} catch (IOException e) {
			Debug.e("LORTEX", "[Server] Problème lors du lancement du serveur.");
			e.printStackTrace();
		}
		
	}


	public void initialize() {
		points = new Vector<GameEntity>();
		bonus = new Vector<GameEntity>();
		
		playersSpawnPoints = new Vector<Vector2>();
		ghostSpawnPoints = new Vector<Vector2>();
		
		players = new SparseArray<Player>();
		
		id = new IDPool();
		ghost_number = new IDPool();
		pokman_number = new IDPool();
		player_id = new IDPool();

		nLifes = new SparseIntArray();
		scores = new SparseIntArray();
		
		entities = new SparseArray<GameEntity>();
		
		MazeGenerator mazeGen = new MazeGenerator(mGamemode.level_width,mGamemode.level_height,mGamemode.level, mSeed);
		if(mGamemode.mode == GameMode.SINGLEPLAYER || mGamemode.mode == GameMode.ONLINE_MULTIPLAYER_TEST) {
			mazeGen.randomize();
		} else if(mGamemode.mode == GameMode.CHOOSE_LEVEL) { //todo: create file
			Element[][] levelSelectMap ={{Element.WALL, Element.WALL, Element.WALL, Element.WALL, Element.WALL, Element.WALL, Element.WALL, Element.WALL, Element.WALL, Element.WALL, Element.WALL, Element.WALL, Element.WALL, Element.WALL, Element.WALL, Element.WALL, Element.WALL, Element.WALL, Element.WALL, Element.WALL, Element.WALL},
							  {Element.WALL,Element.EMPTY, Element.WALL,Element.EMPTY, Element.WALL,Element.EMPTY, Element.WALL,Element.EMPTY,Element.WALL, Element.EMPTY, Element.WALL,Element.EMPTY, Element.WALL,Element.EMPTY, Element.WALL,Element.EMPTY, Element.WALL,Element.EMPTY, Element.WALL,Element.EMPTY, Element.WALL},
						 	  {Element.WALL,Element.EMPTY,Element.EMPTY,Element.EMPTY,Element.EMPTY,Element.EMPTY,Element.EMPTY,Element.EMPTY,Element.EMPTY,Element.EMPTY,Element.EMPTY,Element.EMPTY,Element.EMPTY,Element.EMPTY,Element.EMPTY,Element.EMPTY,Element.EMPTY,Element.EMPTY,Element.EMPTY,Element.EMPTY,Element.WALL},
						 	  {Element.WALL, Element.WALL, Element.WALL, Element.WALL, Element.WALL, Element.WALL, Element.WALL, Element.WALL, Element.WALL, Element.WALL, Element.WALL, Element.WALL, Element.WALL, Element.WALL, Element.WALL, Element.WALL, Element.WALL, Element.WALL, Element.WALL, Element.WALL, Element.WALL}};
			mazeGen.customMap(levelSelectMap);
		}
		map = mazeGen.getMap();

		mGameWorld = new GameWorld(this);
		mGameWorld.putWalls(mazeGen);
		
		for(int x=0;x<mazeGen.getWidth();x++) {
			for(int y=0;y<mazeGen.getHeight();y++) {
				if(mazeGen.value(x, y) == Element.SPAWNPAC) {
					playersSpawnPoints.add(new Vector2(x,y));
				} else if(mazeGen.value(x, y) == Element.SPAWNGHOST) {
					ghostSpawnPoints.add(new Vector2(x,y));
					spawnPoint(x, y);
				} else if(mazeGen.value(x, y) == Element.POINT) {
					spawnPoint(x, y);
				} else if(mazeGen.value(x, y) == Element.BONUS) {
					spawnBonus(x, y);
				}
			}
		}

		Collections.shuffle(points);
	}
	
	
	@Override
	public void onUpdate(float pSecondsElapsed) {
		synchronized(mPlayersToAdd) {
			for(int i : mPlayersToAdd) {
				addPlayer(i);
				Log.w("LORTEX","Adding player: "+i);
			}
			mPlayersToAdd.clear();
		}
		
		if(mAllReady) {
			onlineAllReady();
			mAllReady = false;
		}
	}
	
	
	public GameEntity spawnPokman(Player owner) {
		if(playersSpawnPoints.size() == 0)
			return null;
		
		Log.w("LORTEX", "New pokman for "+owner.id);
		
		int x = (int) playersSpawnPoints.get(0).x;
		int y = (int) playersSpawnPoints.get(0).y;
		
		return spawnPokmanAt(owner, x, y, id.generate(),pokman_number.generate());
	}
	
	public GameEntity spawnPokmanAt(Player owner, float x, float y, int entityId, int pokId) {
		Log.w("LORTEX","[Server] Adding new pokman at "+x+"/"+y);
		
		Body pokpok = mGameWorld.addAPokman(x, y);
		GameEntity datPokman = new GameEntity();
		datPokman.body = pokpok;
		datPokman.id = entityId;
		datPokman.data = pokId;
		
		datPokman.type = Type.POKMAN;

		
		pokpok.setUserData(datPokman);
		
		synchronized (owner.controlledEntities) {
			owner.controlledEntities.add(datPokman);
		}

		if(shouldSendOnline()) {
			mContext.getMultiplayerHandler().broadcast(new OnlinePackets.NewPokman(entityId, pokId, owner.id, x, y), true);
		}

		NewEntity lol = new NewEntity();
		lol.id  = datPokman.id;
		lol.rotation = 0;
		lol.type = datPokman.type;
		lol.data = datPokman.data;
		lol.x = x*TileMapping.TILE_SIZE;
		lol.y = y*TileMapping.TILE_SIZE;
		
		mServer.sendToAllTCP(lol);
		
		entities.append(datPokman.id, datPokman);
		
		return datPokman;
	}
	
	public boolean shouldSendOnline() {
		return isAuthoritative && mGamemode.mode == GameMode.ONLINE_MULTIPLAYER_TEST && mReady;
	}
	
	public float distance(Vector2 a, Vector2 b) {
		return (float) Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
	}
	
	public Vector2 chooseSpawnPoint(Vector<Vector2> availableSpawnPoints) {
		float maxDistance = 0;
		Vector2 result = null;
		Log.i("LORTEX", "[Server] Sélection spawn point");
		
		for(Vector2 test : availableSpawnPoints) {
			float testDistance = 0;
			for(int j=0;j<players.size();j++) {
				Player p = players.valueAt(j);
				synchronized(p.controlledEntities) {
					for(int i=0;i<p.controlledEntities.size();i++) {
						GameEntity ge = p.controlledEntities.get(i);
						Vector2 v2 = new Vector2(ge.body.getWorldCenter().x*PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT/TileMapping.TILE_SIZE, ge.body.getWorldCenter().y*PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT/TileMapping.TILE_SIZE);
						testDistance += distance(test,v2);
					}
				}
			}
			
			if(testDistance >= maxDistance) {
				maxDistance = testDistance;
				result = test;
			}
		}
		
		return result;
	}
	
	public GameEntity spawnGhost(Player owner, GameEntity focus) {
		if(ghostSpawnPoints.size() == 0)
			return null;
		
		Vector2 spawn = chooseSpawnPoint(ghostSpawnPoints);
		
		int x = (int) spawn.x;
		int y = (int) spawn.y;
		
		return spawnGhostAt(owner, focus, x, y, id.generate(), ghost_number.generate());
	}
	
	public GameEntity spawnGhostAt(Player owner, GameEntity focus, float x, float y, int entityId, int ghostId) {
		Log.w("LORTEX","[Server] Adding new ghost at "+x+"/"+y);
		
		Body ghost = mGameWorld.addAGhost(x, y);
		GameEntity datGhost = new GameEntity();
		datGhost.body = ghost;
		datGhost.id = entityId;
		
		datGhost.type = Type.GHOST;
		datGhost.data = ghostId;
		datGhost.focus = focus;

		
		ghost.setUserData(datGhost);
		
		synchronized(owner.controlledEntities) {
			owner.controlledEntities.add(datGhost);
		}
		
		if(shouldSendOnline()) {
			mContext.getMultiplayerHandler().broadcast(new OnlinePackets.NewGhost(entityId, ghostId, owner.id, focus.id, x, y), true);
		}


		NewEntity lol = new NewEntity();
		lol.id  = datGhost.id;
		lol.rotation = 0;
		lol.type = datGhost.type;
		lol.x = x*TileMapping.TILE_SIZE;
		lol.y = y*TileMapping.TILE_SIZE;
		lol.data = datGhost.data;
		
		mServer.sendToAllTCP(lol);
		
		entities.append(datGhost.id, datGhost);
		
		return datGhost;
	}
	
	public void spawnPoint(int pid, float x, float y) {
		GameEntity ge = new GameEntity();
		ge.body = mGameWorld.addPoint(x, y);
		ge.id = pid;
		ge.type = Type.POINT;
		ge.body.setUserData(ge);
		points.add(ge);
		
		entities.append(ge.id, ge);
	}
	
	public void spawnPoint(int x, int y) {
		spawnPoint(id.generate(), x, y);
	}
	
	public void spawnBonus(int bid, float x, float y) {
		Log.w("LORTEX","Server spawning bonus ");
		GameEntity ge = new GameEntity();
		ge.body = mGameWorld.addPoint(x, y);
		ge.id = bid;
		ge.type = Type.BONUS;
		ge.body.setUserData(ge);
		bonus.add(ge);
		
		entities.append(ge.id, ge);
	}
	
	public void spawnBonus(int x, int y) {
		spawnBonus(id.generate(), x, y);
	}
	
	
	public void sendBodyData(Body buddy) {
		
		theRequest.id = ((GameEntity) buddy.getUserData()).id;
		theRequest.x = buddy.getWorldCenter().x*PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
		theRequest.y = buddy.getWorldCenter().y*PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
		theRequest.rotation = buddy.getAngle();
	
		
		mServer.sendToAllUDP(theRequest);
		
		if(shouldSendOnline()) {
			mContext.getMultiplayerHandler().broadcast(new OnlinePackets.EntityUpdate(theRequest.id, buddy.getWorldCenter().x, buddy.getWorldCenter().y, theRequest.rotation), false);
		}
	}

	public Player getOwner(int id_ge) {
		for(int j=0;j<players.size();j++) {
			Player p = players.valueAt(j);
			synchronized(p.controlledEntities) {
				for(GameEntity ge : p.controlledEntities) {
					if(ge.id == id_ge)
						return p;
				}
			}
		}
		return new Player();
	}
	
	public void entityDied(GameEntity ge,GameEntity ge_cause) {
		EntityDeleted packet = new EntityDeleted();
		packet.id = ge.id;
		packet.id_cause = ge_cause.id;
		mServer.sendToAllTCP(packet);
		
		if(ge.type == Type.POKMAN) {
			if(mGamemode.mode == GameMode.SINGLEPLAYER) {
				mContext.getAchievementsManager().setMaster(false);
			}
			
			Player owner =  getOwner(ge.id);
			int pid = owner.id;
			owner.controlledEntities.remove(ge);
			
			nLifes.put(pid, nLifes.get(pid) - 1);
			sendDataUpdate(pid);
			if(nLifes.get(pid) > 0) {
				spawnPokman(owner);
			}
		} else if(ge.type == Type.POINT) {
			points.remove(ge);
			
			if(mGamemode.mode == GameMode.SINGLEPLAYER) {
				mContext.getAchievementsManager().increment(R.string.achievement_la_reponse, 1);

				mContext.getAchievementsManager().increment(R.string.achievement_goinfre, 1);
			}
			
			if(points.size() <= 0) {
				Debug.e("This is the end...");
				this.onLevelDone();
			}
				
		} else if(ge.type == Type.BONUS) {
			bonus.remove(ge);
		} 
		
		entities.remove(ge.id);
	}
	
	public void pointEaten(int pok_id) {
		int pid = getOwner(pok_id).id;
		scores.put(pid, scores.get(pid) + 5);
		sendDataUpdate(pid);
	}

	public Vector<GameEntity> bonusActivated(int id) {
		BonusActivated packet = new BonusActivated();
		packet.id = id;
		mServer.sendToAllTCP(packet);

		int pid = getOwner(id).id;
		scores.put(pid, scores.get(pid) + 42);
		sendDataUpdate(pid);
		
		Vector<GameEntity> vec = new Vector<GameEntity>();
		for(int j=0;j<players.size();j++) {
			Player p = players.valueAt(j);
			for(GameEntity ge : p.controlledEntities)
				if(ge.type == Type.GHOST)
					vec.add(ge);
		}
		return vec;
	}
	
	public void bonusNearEnd(int id) {
		BonusNearEnd packet = new BonusNearEnd();
		packet.id = id;
		mServer.sendToAllTCP(packet);
	}
	
	public void bonusEnded(int id) {
		BonusEnded packet = new BonusEnded();
		packet.id = id;
		mServer.sendToAllTCP(packet);
		mGhostStreak = 0;
	}


	private int getDataById(int id) {
		int data = 0;
		for(int j=0;j<players.size();j++) {
			Player p = players.valueAt(j);
			synchronized(p.controlledEntities) {
				for(GameEntity ge : p.controlledEntities) {
					if(ge.id == id)
						data = ge.data;
				}
			}
		}
		return data;
	}
	
	public void vulnerableGhost(int id_ghost) {
		VulnerableGhost packet = new VulnerableGhost();
		packet.data_ghost = getDataById(id_ghost);
		mServer.sendToAllTCP(packet);
	}


	public void vulnerableGhostNearEnd(int id_ghost) {
		VulnerableGhostNearEnd packet = new VulnerableGhostNearEnd();
		packet.data_ghost = getDataById(id_ghost);
		mServer.sendToAllTCP(packet);
	}


	public void vulnerableGhostEnded(int id_ghost) {
		VulnerableGhostEnded packet = new VulnerableGhostEnded();
		packet.data_ghost = getDataById(id_ghost);
		mServer.sendToAllTCP(packet);
	}

	
	public void sendDataUpdate(int id) {
		PlayerDataUpdate up = new PlayerDataUpdate();
		up.id = id;
		up.nlifes = nLifes.get(id);
		up.score = scores.get(id);
		mServer.sendToAllTCP(up);
	}

	
	public void ghostDeath(int data, int id_cause) {
		if(mGamemode.mode == GameMode.SINGLEPLAYER) {
			nGhostsKilled++;
			mContext.getAchievementsManager().unlock(R.string.achievement_premier_sang);
			mContext.getAchievementsManager().increment(R.string.achievement_tueur_en_srie, 1);
			
			mGhostStreak ++;
			if(mGhostStreak == 4)
				mContext.getAchievementsManager().unlock(R.string.achievement_ace);
		}
		
		int pokpokid = getOwner(id_cause).id;
		scores.put(pokpokid, scores.get(pokpokid) + 200);
		sendDataUpdate(pokpokid);

		GhostDeathPacket packet = new GhostDeathPacket();
		packet.data = data;
		mServer.sendToAllTCP(packet);
	}
	
	public void ghostWillReviveSoon(int data) {
		GhostWillReviveSoonPacket packet = new GhostWillReviveSoonPacket();
		packet.data = data;
		mServer.sendToAllTCP(packet);
	}
	
	public void  ghostRevive(int data) {
		GhostRevivePacket packet = new GhostRevivePacket();
		packet.data = data;
		mServer.sendToAllTCP(packet);
	}


	/*
	 * 
	 * In case of non authoritative server
	 * 
	 */
	
	public void updateEntity(int id, float x, float y, float angle) {
		synchronized(entities) {
			GameEntity ge = entities.get(id);
			if(ge != null) {
				//ge.body.setTransform(x, y, angle);
				ge.body.setLinearVelocity((x-ge.body.getWorldCenter().x)*10, (y-ge.body.getWorldCenter().y)*10);
				sendBodyData(ge.body);
				//players.get(ge).sensorData.x
			}
		}
	}
	
	
	
	/*
	 * 
	 * 
	 * 
	 * 
	 */
	
	
	
	
	public void stopServer() {
		if(mServer != null)
			mServer.stop();
	}

	private void sendMapToMyFriend(Connection connection) {
		MapInfoPacket mip = new MapInfoPacket();
		NewMap nm = new NewMap();
		nm.width = mGamemode.level_width;
		nm.height = mGamemode.level_height;
		connection.sendTCP(nm);
		synchronized (map) {
			for (int x = 0; x < map.size(); x++) {
				for (int y = 0; y < map.get(x).size(); y++) {
					mip.x = x;
					mip.y = y;
					mip.data = map.get(x).get(y);
					connection.sendTCP(mip);
				}
			}
		}
		connection.sendTCP(new NewMapEnd());
		
		NewEntity packet = new NewEntity();
		packet.type = Type.POINT;
		packet.rotation = 0;

		synchronized (points) {
			for(GameEntity ge : points) {
				packet.id = ge.id;
				packet.x = ge.body.getWorldCenter().x*PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
				packet.y = ge.body.getWorldCenter().y*PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
				connection.sendTCP(packet);
				
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		synchronized (bonus) {
			for(GameEntity ge : bonus) {
				packet.id = ge.id;
				packet.type = Type.BONUS;
				packet.x = ge.body.getWorldCenter().x*PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
				packet.y = ge.body.getWorldCenter().y*PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
				connection.sendTCP(packet);
			}
		}
		NewEntity lol = new NewEntity();
		
		synchronized(players) {
			for(int j=0;j<players.size();j++) {
				Player p = players.valueAt(j);
				synchronized(p.controlledEntities) {
					for(GameEntity ge : p.controlledEntities) {
						lol.id  = ge.id;
						lol.rotation = ge.body.getAngle();
						lol.type = ge.type;
						lol.x = ge.body.getWorldCenter().x*PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
						lol.y = ge.body.getWorldCenter().y*PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
						lol.data = ge.data;
						
						connection.sendTCP(lol);
					}
				}
			}
		}
	}
	
	public SceneManagerActivity getContext() {return mContext;}
	public SparseArray<Player> getPlayers() {return players;}
	public void setPlayers(SparseArray<Player> players) {this.players = players;}
	public GameWorld getGameWorld() {return mGameWorld;}


	public void onLevelDone() {
		if(mGamemode.mode == GameMode.SINGLEPLAYER) {
			if(mGamemode.level == 1) {
				mContext.getAchievementsManager().unlock(R.string.achievement_un_bon_debut);
			} else if(mGamemode.level == 5) {
				mContext.getAchievementsManager().unlock(R.string.achievement_ca_se_corse);
			} else if(mGamemode.level == 10) {
				mContext.getAchievementsManager().unlock(R.string.achievement_professionel);
			}
			
			if(mGamemode.level == 10 && mContext.getAchievementsManager().isRush()) {
				mContext.getAchievementsManager().unlock(R.string.achievement_like_a_boss);
				if(mContext.getAchievementsManager().isMaster())
					mContext.getAchievementsManager().unlock(R.string.achievement_maitre_badass_ultime);
			}
			
			if(nGhostsKilled == 0)
				mContext.getAchievementsManager().unlock(R.string.achievement_pacifiste);
			else if(nGhostsKilled == 16)
				mContext.getAchievementsManager().unlock(R.string.achievement_loptimisation_cest_ma_passion);
			

	        int timeElapsedMillis = (int) ((System.nanoTime() - timeBegin)/1000000);
	        if(timeElapsedMillis < 60000)
	        	mContext.getAchievementsManager().unlock(R.string.achievement_le_grand_rush);
		}

		mGamemode.level = mGamemode.level+1;
		for(int i=0;i<nLifes.size();i++) {
			int index = nLifes.keyAt(i);
			nLifes.put(index, nLifes.get(index) + 1);
		}
		mGamemode.stat_nlifes = nLifes;
		
		for(int i=0;i<scores.size();i++) {
			int index = scores.keyAt(i);
			scores.put(index, scores.get(index) + current_level.winPoints);
		}
		mGamemode.stat_scores = scores;
		stopServer();

		
		mContext.setScene(new GameScene(mContext, mGamemode, true, "127.0.0.1", 0));
	}


	private class NetworkListener extends Listener {
		@Override
		public void connected(Connection connection) {
			TakeThisId rep = new TakeThisId();
			rep.id = player_id.generate();
			connection.sendTCP(rep);
			sendMapToMyFriend(connection);
		}
		
		@Override
		public void received (Connection connection, Object object) {
			if(object instanceof HiMyNameIs) {
				HiMyNameIs req = (HiMyNameIs) object;
				addPlayerOnUpdateThread(req.id);

				timeBegin = System.nanoTime();
			} else if(object instanceof SensorChanged) {
				SensorChanged req = (SensorChanged) object;
				sensorUpdate(req.id, req.x, req.y);
				
			}
		}

		@Override
		public void disconnected(Connection connection) {}
	}
	
	
	

	public int getLevel() {
		return mGamemode.level;
	}
	
	public void addPlayerOnUpdateThread(int id) {
		synchronized(mPlayersToAdd) {
			mPlayersToAdd.add(id);
		}
	}


	public void addPlayer(int id) {
		Log.i("LORTEX","Server adding player "+id);
		final Player p = new Player();
		p.id = id;
		p.name = "";
		players.put(id,p);
		
		HiHisNameIs resp = new HiHisNameIs();
		resp.id = p.id;
		resp.name = p.name;
		mServer.sendToAllTCP(resp);

		nLifes.append(p.id, mGamemode.stat_nlifes.get(p.id, mGamemode.stat_nlifes_default));
		scores.append(p.id, mGamemode.stat_scores.get(p.id, mGamemode.stat_score_default));
		
		if(shouldSendOnline()) {
			mContext.getMultiplayerHandler().broadcast(new OnlinePackets.NewPlayer(id), true);
		}
		
		sendDataUpdate(p.id);
		
		if(isAuthoritative) {
			if(mGamemode.mode == GameMode.SINGLEPLAYER) {
				GameEntity pok = spawnPokman(p);
				spawnGhost(p,pok);
				spawnGhost(p,pok);
				spawnGhost(p,pok);
				spawnGhost(p,pok);
			} else if(mGamemode.mode == GameMode.MULTIPLAYER_TEST) {
				spawnPokman(p);
				spawnGhost(p,null);
			} else if(mGamemode.mode == GameMode.ONLINE_MULTIPLAYER_TEST) {
				spawnPokman(p);
			}
		}
	}


	public void sensorUpdate(int id, float x, float y) {
		if(players.get(id)!=null) {
			players.get(id).sensorData.x = x;
			players.get(id).sensorData.y = y;
		}
	}


	


	@Override
	public void reset() {}


	public Player getPlayerById(int ownerId) {
		return players.get(ownerId);
	}


	public GameEntity getEntityById(int focusId) {
		return entities.get(focusId);
	}


	public void onlineAllReady() {
		mReady  = true;
		
		Log.w("LORTEX","[Server] Everybody is ready");
		
		
		synchronized(players) {
			for(int j=0;j<players.size();j++) {
				Player p = players.valueAt(j);
				synchronized(p.controlledEntities) {
					for(GameEntity ge : p.controlledEntities) {						
						Log.w("LORTEX","[Server] Sending new ghost/pokman");
						
						if(ge.type == Type.GHOST)
							mContext.getMultiplayerHandler().broadcast(new OnlinePackets.NewGhost(ge.id, ge.data , p.id, ge.focus.id, ge.body.getWorldCenter().x, ge.body.getWorldCenter().y), true);
						if(ge.type == Type.POKMAN)
							mContext.getMultiplayerHandler().broadcast(new OnlinePackets.NewPokman(ge.id, ge.data , p.id, ge.body.getWorldCenter().x, ge.body.getWorldCenter().y), true);
						
					}
				}
			}
		}
	}


	public void allReadyFlag(Vector<Integer> pendingPlayers) {
		mAllReady  = true;
		mPendingPlayers = pendingPlayers; 
	}



}
