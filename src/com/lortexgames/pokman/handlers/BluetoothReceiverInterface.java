package com.lortexgames.pokman.handlers;

public interface BluetoothReceiverInterface {
	public void message(String string, int bytes, BluetoothConnectedThread sender);
}
