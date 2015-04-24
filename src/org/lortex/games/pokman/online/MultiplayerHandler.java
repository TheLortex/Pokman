package org.lortex.games.pokman.online;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

import org.lortex.games.pokman.client.SceneManagerActivity;
import org.lortex.games.pokman.common.Packets;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.google.example.games.basegameutils.GameHelper;

public class MultiplayerHandler implements RoomUpdateListener, RealTimeMessageReceivedListener, RoomStatusUpdateListener {

	GameHelper mGameHelper;
	MultiplayerListener mMultiplayerListener;
	SceneManagerActivity mContext;
	private String mRoomId;
	private ArrayList<Participant> mParticipants;
	private String mMyId;
	private Participant mHost;
	
	public PingTest mPing;
	
    public final static int RC_SELECT_PLAYERS = 10000;
    public final static int RC_INVITATION_INBOX = 10001;
    public final static int RC_WAITING_ROOM = 10002;
    
    /*
     * Sérialization
     */
   
	
	public MultiplayerHandler(SceneManagerActivity context, GameHelper gameHelper, MultiplayerListener listener) {
		mMultiplayerListener = listener;
		mGameHelper = gameHelper;
		mContext = context;
		
		mPing = new PingTest(this);
	}
	

    public void startQuickGame() {
    	Log.i("LORTEX", "Starting a quick game");
    	
        final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 3;
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS,
                MAX_OPPONENTS, 0);
        
        RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
        rtmConfigBuilder.setMessageReceivedListener(this);
        rtmConfigBuilder.setRoomStatusUpdateListener(this);
        rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
        keepScreenOn();

        Games.RealTimeMultiplayer.create(mGameHelper.getApiClient(), rtmConfigBuilder.build());
        
    }


    public void invitePlayers() {
    	Log.i("LORTEX", "Inviting players");
        Intent intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent(mGameHelper.getApiClient(), 1, 3);
        mContext.startActivityForResult(intent, RC_SELECT_PLAYERS);
    }
    
    public void seeInvitations() {
    	Log.i("LORTEX", "Seeing invitations");
    	Intent intent = Games.Invitations.getInvitationInboxIntent(mGameHelper.getApiClient());
        mContext.startActivityForResult(intent, RC_INVITATION_INBOX);
    }

    public void handleSelectPlayersResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w("LORTEX", "*** select players UI cancelled, " + response);
            return;
        }

        Log.d("LORTEX", "Select players UI succeeded.");

        // get the invitee list
        final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
        Log.d("LORTEX", "Invitee count: " + invitees.size());

        // get the automatch criteria
        Bundle autoMatchCriteria = null;
        int minAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
        int maxAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
        if (minAutoMatchPlayers > 0 || maxAutoMatchPlayers > 0) {
            autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                    minAutoMatchPlayers, maxAutoMatchPlayers, 0);
            Log.d("LORTEX", "Automatch criteria: " + autoMatchCriteria);
        }

        // create the room
        Log.d("LORTEX", "Creating room...");
        RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
        rtmConfigBuilder.addPlayersToInvite(invitees);
        rtmConfigBuilder.setMessageReceivedListener(this);
        rtmConfigBuilder.setRoomStatusUpdateListener(this);
        if (autoMatchCriteria != null) {
            rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
        }
        keepScreenOn();
        Games.RealTimeMultiplayer.create(mGameHelper.getApiClient(), rtmConfigBuilder.build());
        Log.d("LORTEX", "Room created, waiting for it to be ready...");
    }

    // Handle the result of the invitation inbox UI, where the player can pick an invitation
    // to accept. We react by accepting the selected invitation, if any.
    public void handleInvitationInboxResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w("LORTEX", "*** invitation inbox UI cancelled, " + response);
            return;
        }

        Log.d("LORTEX", "Invitation inbox UI succeeded.");
        Invitation inv = data.getExtras().getParcelable(Multiplayer.EXTRA_INVITATION);

        // accept invitation
        acceptInviteToRoom(inv.getInvitationId());
    }
    
 // Accept the given invitation.
    public void acceptInviteToRoom(String invId) {
        // accept the invitation
        Log.d("LORTEX", "Accepting invitation: " + invId);
        RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(this);
        roomConfigBuilder.setInvitationIdToAccept(invId)
                .setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this);
        keepScreenOn();
        Games.RealTimeMultiplayer.join(mGameHelper.getApiClient(), roomConfigBuilder.build());
    }
    
    public void onStop() {
        leaveRoom();
        stopKeepingScreenOn();
    }

    public void leaveRoom() {
        Log.d("LORTEX", "Leaving room.");

        stopKeepingScreenOn();
        if (mRoomId != null) {
            Games.RealTimeMultiplayer.leave(mGameHelper.getApiClient(), this, mRoomId);
            mRoomId = null;
        } 
    }
    
    void showWaitingRoom(Room room) {
        // minimum number of players required for our game
        // For simplicity, we require everyone to join the game before we start it
        // (this is signaled by Integer.MAX_VALUE).
        final int MIN_PLAYERS = Integer.MAX_VALUE;
        Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(mGameHelper.getApiClient(), room, MIN_PLAYERS);

        // show waiting room UI
        mContext.startActivityForResult(i, RC_WAITING_ROOM);
    }


	@Override
	public void onJoinedRoom(int statusCode, Room room) {
		if (statusCode != GamesStatusCodes.STATUS_OK) {
            return;
        }

        showWaitingRoom(room);
	}

	@Override
	public void onLeftRoom(int statusCode, String room) {}

	@Override
	public void onRoomConnected(int statusCode, Room room) {
		if (statusCode != GamesStatusCodes.STATUS_OK) {
            return;
        }
        updateRoom(room);
	}

	@Override
	public void onRoomCreated(int statusCode, Room room) {
		if (statusCode != GamesStatusCodes.STATUS_OK) {
            return;
        }

        showWaitingRoom(room);
	}

	@Override
	public void onConnectedToRoom(Room room) {
        mRoomId = room.getRoomId();
        mParticipants = room.getParticipants();
        mMyId = room.getParticipantId(Games.Players.getCurrentPlayerId(mGameHelper.getApiClient()));
    }

	@Override
	public void onDisconnectedFromRoom(Room arg0) {
        mRoomId = null;
        Toast.makeText(mContext, "Déconnecté du salon", Toast.LENGTH_SHORT).show();
        mMultiplayerListener.onLeftRoom();
    }

    void updateRoom(Room room) {
        if (room != null) {
            mParticipants = room.getParticipants();
        }
        if (mParticipants != null) {
        	if(mParticipants.size() == 1)
        		Toast.makeText(mContext, "You're alone in this room", Toast.LENGTH_LONG).show();
        }
    }



	public void handleStartingGame() {
		String hostId = "";
		for(Participant player : mParticipants) {
			if(player.getParticipantId().compareTo(hostId) > 0) {
				hostId = player.getParticipantId();
				mHost = player;
			}
		}
		
		Log.i("LORTEX", "La partie va commencer!");
		
		if(mMyId.equals(mHost.getParticipantId())) {
			Toast.makeText(mContext, "Je suis l'hôte!", Toast.LENGTH_SHORT).show();
			long seed = System.currentTimeMillis();
			mMultiplayerListener.onSetupServer(true, seed);
			
			OnlinePackets.Init seedpacket = new OnlinePackets.Init();
			seedpacket.seed = seed;

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutput out = null; 
			try {
				  out = new ObjectOutputStream(bos);   
				  out.writeObject(seedpacket);
				  byte[] packet = bos.toByteArray();

				  for (Participant p : mParticipants) {
			            if (p.getParticipantId().equals(mMyId))
			                continue;
			            if (p.getStatus() != Participant.STATUS_JOINED)
			                continue;
			               
			            Games.RealTimeMultiplayer.sendReliableMessage(mGameHelper.getApiClient(), null, packet, mRoomId, p.getParticipantId()); 
			       }
				  
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
			  try {
			    if (out != null) {
			      out.close();
			    }
			  } catch (IOException ex) {}
			  try {
			    bos.close();
			  } catch (IOException ex) {}
			}
		} else {
			Toast.makeText(mContext, "Je ne suis pas hôte :/", Toast.LENGTH_SHORT).show();
		}
	}
    
	@Override
	public void onP2PConnected(String arg0) {}

	@Override
	public void onP2PDisconnected(String arg0) {}

	@Override
	public void onPeerDeclined(Room arg0, List<String> arg1) {
        updateRoom(arg0);
    }

	@Override
	public void onPeerInvitedToRoom(Room arg0, List<String> arg1) {
        updateRoom(arg0);
        }
	
	@Override
	public void onPeerJoined(Room arg0, List<String> arg1) {
        updateRoom(arg0);
    }

	@Override
	public void onPeerLeft(Room arg0, List<String> arg1) {
        updateRoom(arg0);}

	@Override
	public void onPeersConnected(Room arg0, List<String> arg1) {
        updateRoom(arg0);}

	@Override
	public void onPeersDisconnected(Room arg0, List<String> arg1) {
        updateRoom(arg0);}

	@Override
	public void onRoomAutoMatching(Room room) {
        updateRoom(room);}

	@Override
	public void onRoomConnecting(Room room) {
        updateRoom(room);
    }
	
	private int getId(String pid) {
		for(int i=0;i<mParticipants.size();i++) 
			  if(mParticipants.get(i).getParticipantId().equals(pid))
				  return i+1;
		return -1;
	}

	@Override
	public void onRealTimeMessageReceived(RealTimeMessage realTimeMessage) {
		
		byte[] data = realTimeMessage.getMessageData();
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		ObjectInput in = null;
		try {
		  in = new ObjectInputStream(bis);
		  Object o = in.readObject(); 
		  
		  if(o instanceof OnlinePackets.Init) {
			  long seed = ((OnlinePackets.Init) o).seed;
			  mMultiplayerListener.onSetupServer(false, seed);
			  Log.i("LORTEX", "La graine a été plantée: "+seed);
		  } else if(o instanceof OnlinePackets.SensorUpdate)  {
				
			  OnlinePackets.SensorUpdate packet = (OnlinePackets.SensorUpdate)o;
			  int id = getId(realTimeMessage.getSenderParticipantId());
			  if(id != -1) {
				  mMultiplayerListener.onSensorUpdate(id, packet.x, packet.y);
			  }
		  } else if(o instanceof OnlinePackets.ArmedAndReady) {
			  Log.i("LORTEX", "Armed and ready : "+getId(realTimeMessage.getSenderParticipantId()));
			  int id = getId(realTimeMessage.getSenderParticipantId());
			  if(id != -1)
				  mMultiplayerListener.onReady(id);
		  } else if(o instanceof Packets.PlayerDataUpdate) {
			  mMultiplayerListener.onPlayerDataUpdate((Packets.PlayerDataUpdate) o);
		  } else if(o instanceof OnlinePackets.NewPlayer) {
			  OnlinePackets.NewPlayer pck = (OnlinePackets.NewPlayer) o;
			  mMultiplayerListener.onNewPlayer(pck.id);
		  } else if(o instanceof OnlinePackets.NewPokman) {
			  OnlinePackets.NewPokman pck = (OnlinePackets.NewPokman) o;
			  mMultiplayerListener.onNewPokman(pck.entityId, pck.pokId, pck.ownerId, pck.x, pck.y);
		  } else if(o instanceof OnlinePackets.NewGhost)  {
			  OnlinePackets.NewGhost pck = (OnlinePackets.NewGhost) o;
			  mMultiplayerListener.onNewGhost(pck.entityId, pck.ghostId, pck.ownerId, pck.focusId, pck.x, pck.y);
		  } else if(o instanceof OnlinePackets.EntityUpdate)  {
			  OnlinePackets.EntityUpdate pck = (OnlinePackets.EntityUpdate) o;
			  mMultiplayerListener.onEntityUpdate(pck.id, pck.x, pck.y, pck.rotation);
		  } else if(o instanceof OnlinePackets.Ping) {
			  OnlinePackets.Ping pck = (OnlinePackets.Ping) o;
			  if(isHost()) {
				  Games.RealTimeMultiplayer.sendUnreliableMessage(mGameHelper.getApiClient(), data, mRoomId, realTimeMessage.getSenderParticipantId());
				  
			  } else {
				  mMultiplayerListener.updatePing((System.nanoTime() - pck.time)/1000000f);
				  Log.i("PINGTEST", "Ping: "+(System.nanoTime() - pck.time)/1000/1000f);
			  }
		  }
		  
		  
		} catch (StreamCorruptedException e) { e.printStackTrace(); } catch (IOException e) {e.printStackTrace(); } catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
		  try {
		    bis.close();
		  } catch (IOException ex) {}
		  try {
		    if (in != null) {
		      in.close();
		    }
		  } catch (IOException ex) {}
		}
		
	}

    
    private void keepScreenOn() {
		// TODO Auto-generated method stub
		
	}
    
	private void stopKeepingScreenOn() {
		// TODO Auto-generated method stub
		
	}


	public void sendToHost(final Serializable source_packet,final boolean reliable) {
		if(mRoomId == null)
			return;

		  
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				 ObjectOutput out = null; 
				try {
				  out = new ObjectOutputStream(bos);
				  out.writeObject(source_packet);
				  byte[] packet = bos.toByteArray();

				  
				  if(reliable) 
					  Games.RealTimeMultiplayer.sendReliableMessage(mGameHelper.getApiClient(), null, packet, mRoomId, mHost.getParticipantId()); 
				  else
					  Games.RealTimeMultiplayer.sendUnreliableMessage(mGameHelper.getApiClient(), packet, mRoomId, mHost.getParticipantId()); 
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
				  try {
				    if (out != null) {
				      out.close();
				    }
				  } catch (IOException ex) {}
				  try {
				    bos.close();
				    
				  } catch (IOException ex) {}
				}
			}
		}).start();
		
		 
	}
	
	
	public void broadcast(final Serializable source_packet, final boolean reliable) {
		if(mRoomId == null)
			return;
		
		
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				
				 ByteArrayOutputStream bos = new ByteArrayOutputStream();
				 ObjectOutput out = null; 
				try {
				  out = new ObjectOutputStream(bos);
				  out.writeObject(source_packet);
				  byte[] packet = bos.toByteArray();
				  
				  if(reliable)  {
					  for (Participant p : mParticipants) {
				            if (p.getParticipantId().equals(mMyId))
				                continue;
				            if (p.getStatus() != Participant.STATUS_JOINED)
				                continue;
				               
				            Games.RealTimeMultiplayer.sendReliableMessage(mGameHelper.getApiClient(), null, packet, mRoomId, p.getParticipantId()); 
				       }
				  }
				  else {
					  Games.RealTimeMultiplayer.sendUnreliableMessageToOthers(mGameHelper.getApiClient(), packet, mRoomId);
				  }
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
				  try {
				    if (out != null) {
				      out.close();
				    }
				  } catch (IOException ex) {}
				  try {
				    bos.close();
				    
				  } catch (IOException ex) {}
				}
			}
		} ).start();
		
	}

	public boolean isHost() {
		if(mMyId == null || mHost == null)
			return false;
		return mMyId.equals(mHost.getParticipantId());
	}


	public int getMyPlayerId() {
		return getId(mMyId);
	}


	public int getNumberOfParticipants() {
		int n=0;
		for(Participant p : mParticipants) {
			if(p.getStatus() == Participant.STATUS_JOINED)
				n++;
		}
		return n;
	}


	public ArrayList<Participant> getParticipants() {
		return mParticipants;
	}


	public int getIdOfParticipant(Participant p) {
		return getId(p.getParticipantId());
	}


	

}
