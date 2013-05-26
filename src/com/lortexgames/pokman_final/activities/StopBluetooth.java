package com.lortexgames.pokman_final.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.widget.Toast;

public class StopBluetooth extends Activity {
    protected void onStart(){
    	super.onStart();
    	
    	if(BluetoothAdapter.getDefaultAdapter().isEnabled()) {
    		Toast.makeText(this, "Bluetooth has been disabled", Toast.LENGTH_SHORT).show();
    		BluetoothAdapter.getDefaultAdapter().disable();
    		
    	}
    	finish();
    }
}
