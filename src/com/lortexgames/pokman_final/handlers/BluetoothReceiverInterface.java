package com.lortexgames.pokman_final.handlers;

public interface BluetoothReceiverInterface {
	public void message(String string, int bytes, BluetoothConnectedThread sender);
}
