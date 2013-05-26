package com.lortexgames.pokman_final.handlers;

import java.io.IOException;
import java.util.UUID;

import org.andengine.util.debug.Debug;

import com.lortexgames.pokman_final.activities.LobbyActivity2;
import com.lortexgames.pokman_final.activities.MenuActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class BluetoothHandler {
	public static final int REQUEST_ENABLE_BT = 42;
	private static final UUID APP_UUID = UUID.fromString("2e977251-9073-4d14-bce0-08caa829ff5c");
	
	LobbyActivity2 mContext;
	private BluetoothAdapter mBluetoothAdapter;

	private BroadcastReceiver mReceiver;

//	private boolean connected=false;	
	private boolean mBtEnabled;	
	
	//private BluetoothDevice pair;
	//private BluetoothSocket sock;
	
	private AcceptThread mServer=null;
	
	public BluetoothHandler(LobbyActivity2 context) {
		mContext=context;
	}
	

	public void init() {
		mReceiver = new BroadcastReceiver() {
		    public void onReceive(Context context, Intent intent) {
		        String action = intent.getAction();
		        // When discovery finds a device
		        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
		            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		            mContext.onNewPairFound(device);
		            
		        }
		    }
		};
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		mContext.registerReceiver(mReceiver, filter);
		
		try {
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		} catch (Exception e) {
			Debug.i(e.getMessage());
		}
		
		if (mBluetoothAdapter == null) {
			mContext.onActivityResult(REQUEST_ENABLE_BT, Activity.RESULT_CANCELED, null);
			return;
		}				

		SharedPreferences settings = mContext.getSharedPreferences(MenuActivity.PREFS_NAME, 0);
		Editor edit = settings.edit();
		
	
		if (!mBluetoothAdapter.isEnabled()) {
			edit.putInt("bluetoothStatus", 2);
			edit.commit();
		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    mContext.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			return;
		} else {
			if(settings.getInt("bluetoothStatus", 0) != 2) {
				edit.putInt("bluetoothStatus", 1);
				edit.commit();
			} else
				edit.commit();
			
			mContext.onActivityResult(REQUEST_ENABLE_BT, Activity.RESULT_OK, null);
			return;
		}
	}
	
	public void askDiscoverable() {
		Debug.w("BT::ScanMode="+mBluetoothAdapter.getScanMode());
		if(mBluetoothAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
			return;
		
		Intent discoverableIntent = new
				Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
				discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3600);
				mContext.startActivity(discoverableIntent);
	}
	
	public void server(boolean status) {
		if(status) { //Start
			if(mServer==null) {
				askDiscoverable();
				mServer = new AcceptThread();
				mServer.start();
			}
		} else { //Stop
			if(mServer!=null) {
				mServer.kill();
				mServer = null;
			}
		}
	}

	public boolean isServer() {
		return mServer!=null;
	}
	
	public boolean activated() {
		return mBtEnabled;
	}
	
	public void setActivated(boolean status) {
		mBtEnabled = status;
	}

	public void searchDevices() {
		mBluetoothAdapter.startDiscovery();
	}

	public BroadcastReceiver getReceiver() {
		return mReceiver;
	}

	public void connect(BluetoothDevice host) {
		Debug.i("BT::Connecting to server "+host.getName());
		new ConnectThread(host).start();
	}
	
	private class ConnectThread extends Thread {
	    private final BluetoothSocket mmSocket;
	    private final BluetoothDevice mmDevice;
	 
	    public ConnectThread(BluetoothDevice device) {
	        // Use a temporary object that is later assigned to mmSocket,
	        // because mmSocket is final
	        BluetoothSocket tmp = null;
	        mmDevice = device;
	 
	        // Get a BluetoothSocket to connect with the given BluetoothDevice
	        try {
	            // MY_UUID is the app's UUID string, also used by the server code
	            tmp = device.createRfcommSocketToServiceRecord(APP_UUID);
	        } catch (IOException e) { }
	        mmSocket = tmp;
	    }
	 
	    public void run() {
	        // Cancel discovery because it will slow down the connection
	        mBluetoothAdapter.cancelDiscovery();
	 
	        try {
	            // Connect the device through the socket. This will block
	            // until it succeeds or throws an exception
	            mmSocket.connect();
	        } catch (IOException connectException) {
	            // Unable to connect; close the socket and get out
	            try {
	                mmSocket.close();
	            } catch (IOException closeException) { }
	            return;
	        }
	 
	        // Do work to manage the connection (in a separate thread)
			Debug.i("BT::Connected to server "+mmDevice.getName());
	        mContext.manageSocket(mmSocket);
	    }
	 
	    /** Will cancel an in-progress connection, and close the socket */
	    @SuppressWarnings("unused")
		public void cancel() {
	        try {
	            mmSocket.close();
	        } catch (IOException e) { }
	    }
	}
	

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;
		private boolean continuer=true;
     
        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("Pokman", APP_UUID);
            } catch (IOException e) { }
            mmServerSocket = tmp;
        }
     
        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (continuer) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
        			Debug.i("BT::Client "+socket.getRemoteDevice().getName()+" connected.");
            		mContext.manageSocket(socket);
            		socket=null;
                 /*   try {
						mmServerSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}*/
                }
            }
        }
        
        public void kill() {
            try {
				mmServerSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        	continuer=false;
        }
    }

}
