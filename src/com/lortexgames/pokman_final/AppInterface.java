package com.lortexgames.pokman_final;

import java.util.Vector;

import com.google.example.games.basegameutils.GameHelper;
import com.lortexgames.pokman_final.handlers.BluetoothConnectedThread;

import android.app.Application;

public class AppInterface extends Application {
	BluetoothConnectedThread value=null;
	Vector<BluetoothConnectedThread> values=new Vector<BluetoothConnectedThread>();
	private GameHelper mGH;
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
	
	public void addGameHelper(GameHelper gh) {
		mGH = gh;
	}
	
	public GameHelper getGameHelper() {
		return mGH;
	}
}
