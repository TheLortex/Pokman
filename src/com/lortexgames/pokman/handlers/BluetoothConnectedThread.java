package com.lortexgames.pokman.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.andengine.util.debug.Debug;


import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

public class BluetoothConnectedThread extends Thread {
 	private BluetoothSocket mmSocket;
    private InputStream mmInStream;
    private OutputStream mmOutStream;
    private BluetoothReceiverInterface mContext;
 
    public BluetoothConnectedThread(BluetoothSocket socket,BluetoothReceiverInterface context)  {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
 
        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }
 
        mmInStream = tmpIn;
        mmOutStream = tmpOut;
        mContext = context;
    }
 
    public void run() {
        byte[] buffer = new byte[1024];  // buffer store for the stream
        int bytes=0; // bytes returned from read()
 
        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                // Read from the InputStream
            	if(bytes>1) {
            		//Debug.w("BT::Message="+Arrays.toString(buffer));
                    mContext.message(new String(buffer),bytes,this);
            	}
        		
                bytes = mmInStream.read(buffer);
                // Send the obtained bytes to the UI activity
        		//Debug.w("BT::Message:"+bytes);
            } catch (IOException e) {
                break;
            }
        }
    }
 
    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) {
        try {
        	//Debug.i("BT::Socket::>"+new String(bytes));
        	
            mmOutStream.write(((new String(bytes))+"#").getBytes());
        } catch (IOException e) { }
    }
 
    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
        	Debug.i("BT::Socket::Closing connection");
            mmSocket.close();
        } catch (IOException e) { }
    }

	public BluetoothSocket getSock() {
		return mmSocket;
	}

	public void setContext(BluetoothReceiverInterface context) {
		mContext=context;
	}
}
