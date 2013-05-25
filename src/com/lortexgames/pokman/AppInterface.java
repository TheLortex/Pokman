package com.lortexgames.pokman;

import java.util.Vector;

import com.lortexgames.pokman.handlers.BluetoothConnectedThread;

import android.app.Application;

public class AppInterface extends Application {
	BluetoothConnectedThread value=null;
	Vector<BluetoothConnectedThread> values=new Vector<BluetoothConnectedThread>();
	public AppInterface() {}
	
	public void setConnectionThread(BluetoothConnectedThread sender) {
		value=sender;
	}
	
	public BluetoothConnectedThread getConnectionThread() {
		return value;
	}

	public void addConnectionThread(
			BluetoothConnectedThread bluetoothConnectedThread) {
		values.add(bluetoothConnectedThread);
	}
	
	public Vector<BluetoothConnectedThread> getAllConnectionThread() {
		return values;
	}
}
