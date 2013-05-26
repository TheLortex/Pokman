package com.lortexgames.pokman_final.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.andengine.util.debug.Debug;

import com.lortexgames.pokman_final.R;
import com.lortexgames.pokman_final.AppInterface;
import com.lortexgames.pokman_final.handlers.BluetoothConnectedThread;
import com.lortexgames.pokman_final.handlers.BluetoothHandler;
import com.lortexgames.pokman_final.handlers.BluetoothReceiverInterface;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;


@SuppressWarnings("deprecation")
public class LobbyActivity2 extends Activity implements OnDrawerOpenListener,OnDrawerCloseListener, BluetoothReceiverInterface, OnClickListener, OnItemClickListener  {
	

	
	private static final int N_MAX_PLAYERS = 4;
	private BluetoothHandler 	net;
	private ArrayList<String> 	mFoundDevicesName				= new ArrayList<String>();
	private HashMap<String, BluetoothDevice> mDevices 			= new HashMap<String, BluetoothDevice>();
	private HashMap<BluetoothDevice, String> mOnConnectRequests = new HashMap<BluetoothDevice, String>();
	private Vector<BluetoothConnectedThread> mClients			= new Vector<BluetoothConnectedThread>();
	
	private BluetoothConnectedThread mServerThread;
	private BluetoothConnectedThread mConnexionThread;
	private BluetoothDevice mServerDevice;
	private boolean mConnected;
	private TextView clientClickedText;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		net = new BluetoothHandler(this);
        net.init();
        
        
        setContentView(R.layout.lobby_layout);
        
	    SlidingDrawer helpDrawer = (SlidingDrawer) this.findViewById(R.id.slidingDrawer);
		helpDrawer.setOnDrawerOpenListener(this);
		helpDrawer.setOnDrawerCloseListener(this);

		((Button) this.findViewById(R.id.lobbyActButton)).setOnClickListener(this);
		((ListView) this.findViewById(R.id.lobbyDevicesList)).setOnItemClickListener(this);
		((Button) this.findViewById(R.id.lobbyStartGameButton)).setOnClickListener(this);
		

		TextView tv = (TextView) findViewById(R.id.lobbyTitleView);
		Typeface face = Typeface.createFromAsset(getAssets(),"font/police.ttf");
		tv.setTypeface(face);
		tv = (TextView) findViewById(R.id.lobbyServerModeTitleView);
		tv.setTypeface(face);
		
		((Button) this.findViewById(R.id.lobbyStartGameButton)).setEnabled(false);
		updateNPlayersText();
    }
    
    @Override
    public void onDrawerOpened()
    {
    	LinearLayout l = (LinearLayout) LobbyActivity2.this.findViewById(R.id.clientModeLayout);
        l.setVisibility(ListView.GONE);
        
        ToggleButton tb = (ToggleButton) LobbyActivity2.this.findViewById(R.id.lobbyHostButton);
        tb.setChecked(true);
        
        net.server(true);
    }
    
    @Override
    public void onDrawerClosed()
    {
		LinearLayout l = (LinearLayout) LobbyActivity2.this.findViewById(R.id.clientModeLayout);
        l.setVisibility(ListView.VISIBLE);
         
	    ToggleButton tb = (ToggleButton) LobbyActivity2.this.findViewById(R.id.lobbyHostButton);
        tb.setChecked(false);
        
        net.server(false);
        for(int i=0;i<mClients.size();i++)
        	mClients.get(i).write("BYE".getBytes());
        
        mClients.clear();
    }


	public synchronized void message(String string, int bytes, BluetoothConnectedThread sender) {
		String msg = string.substring(0, bytes);
    	Debug.i("BT::Socket::<"+msg);
		manageRequest(msg, sender);
	}
	
	private synchronized void manageRequest(String req, BluetoothConnectedThread sender) {
		String[] trames = req.split("#");
		for(int tr=0;tr<trames.length;tr++) {
			String curMsg = trames[tr];
			String[] sep = curMsg.split(" ");
			
			// Client side requests
			if(sep[0].equals("TKTMACOUILLE")) {
				if(sender.getSock().getRemoteDevice() == mServerDevice) {
					mServerThread = sender;
					this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							clientClickedText.setTextColor(0xFF00FF00);
						}
					});
				}
			}else if(sep[0].equals("IMPOSSIBRU")) {
				cancelConnexion();
			}else if(sep[0].equals("LETSGOCOUSIN")) {
				AppInterface app = (AppInterface)this.getApplication();
				app.setConnectionThread(sender);
				
	        	Intent intent = new Intent(LobbyActivity2.this,  MultiplayerActivity.class);
	        	intent.putExtra("isServer", false);
	        	intent.putExtra("pIndex", Integer.parseInt(sep[1]));
	        	intent.putExtra("nPly", Integer.parseInt(sep[2]));
	        	
				startActivity(intent);
			}
			// Server side requests
			else if((sep[0].equals("BEATBOX"))&&(net.isServer())) {
				if(mClients.size() < N_MAX_PLAYERS) {
					mClients.add(sender);
					sender.write("TKTMACOUILLE".getBytes());
					this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							((Button) findViewById(R.id.lobbyStartGameButton)).setEnabled(true);
							updateNPlayersText();
						}
					});
				} else {
					sender.write("IMPOSSIBRU".getBytes());
				}
			}

			else if(sep[0].equals("BYE")) {
				mClients.remove(sender);

				this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(mClients.size() == 0)
							((Button) findViewById(R.id.lobbyStartGameButton)).setEnabled(false);
						
						updateNPlayersText();
					}
				});
				
				if(sender == mServerThread) {
					cancelConnexion();
				} else {
					sender.write("BYE".getBytes());
					sender.cancel();
				}
				
			}
		}
	}
	public void manageSocket(BluetoothSocket socket) {
		mConnexionThread = new BluetoothConnectedThread(socket, this);
		mConnexionThread.start();
		
		if(mOnConnectRequests.get(socket.getRemoteDevice()) != null) {
			mConnexionThread.write(mOnConnectRequests.get(socket.getRemoteDevice()).getBytes());
		}
		
	}
	

	public void onNewPairFound(BluetoothDevice device) {
		String displayName = device.getAddress() + " - " + device.getName();
		mFoundDevicesName.add(displayName);
		mDevices.put(displayName, device);
		updateList();
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == BluetoothHandler.REQUEST_ENABLE_BT) {
			SharedPreferences settings = this.getSharedPreferences(MenuActivity.PREFS_NAME, 0);
			Editor edit = settings.edit();
			
			if(resultCode == RESULT_OK) {
				net.setActivated(true);
			} else {
				Debug.i("BTST=0");
				edit.putInt("bluetoothStatus", 0);
				edit.commit();
				
				net.setActivated(false);
				//this.toastOnUIThread("Il faut avoir le bluetooth pour pouvoir jouer.");
				finish();
			}
		}
	}

	@Override
	public void onClick(View arg0) {
		if(arg0 == (Button) this.findViewById(R.id.lobbyActButton)) {
			if(!mConnected) {
				mFoundDevicesName.clear();
				mDevices.clear();
				
				updateList();
				net.searchDevices();
				Toast.makeText(this, "Actualisation",	Toast.LENGTH_SHORT).show();
			} else {
				cancelConnexion();
			}
		} else if(arg0 == (Button) this.findViewById(R.id.lobbyStartGameButton)) {
			Toast.makeText(this, "Starting Game",	Toast.LENGTH_SHORT).show();
			
			AppInterface app = (AppInterface)LobbyActivity2.this.getApplication();
			for(int i=0;i<mClients.size();i++) {
				mClients.get(i).write(("LETSGOCOUSIN "+(i+2)+" "+(mClients.size()+1)).getBytes());
				app.addConnectionThread(mClients.get(i));
			}
			
        	Intent intent = new Intent(LobbyActivity2.this,  MultiplayerActivity.class);
        	intent.putExtra("isServer", true);
        	intent.putExtra("pIndex", 1);
        	intent.putExtra("nPly", mClients.size()+1);
			startActivity(intent);
			
		}
	}
	
	private void cancelConnexion() {

		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				((Button) findViewById(R.id.lobbyActButton)).setText("Actualiser");
				updateList();
			}
		});
    	mConnected=false;
    	mServerDevice=null;
    	if(mServerThread != null) {
    		mServerThread.write("BYE".getBytes());
    		mServerThread.cancel();
    		mServerThread=null;
    	}
	}
	
	private void updateList() {
		ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mFoundDevicesName);
		
		ListView devicesList = (ListView) findViewById(R.id.lobbyDevicesList);
		devicesList.setAdapter(adapter);
	}

	private void updateNPlayersText() {
		TextView nPlayersText = (TextView) findViewById(R.id.lobbyServerNPlayers);
		nPlayersText.setText((mClients.size()+1)+"/"+N_MAX_PLAYERS + " players connected.");
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if(view instanceof TextView) {
			if(!mConnected) {
				clientClickedText = (TextView)view;
	        	Toast.makeText(getBaseContext(), clientClickedText.getText().toString(), Toast.LENGTH_LONG).show();
	        	BluetoothDevice selected = mDevices.get(clientClickedText.getText().toString());
	        	mOnConnectRequests.put(selected, "BEATBOX");
	        	net.connect(selected);
	        	mServerDevice = selected;
	        	
	        	clientClickedText.setTextColor(0xFF00FFFF);
	        	
	        	((Button) this.findViewById(R.id.lobbyActButton)).setText("Annuler");
	        	mConnected=true;
			}
		}
	}
}
