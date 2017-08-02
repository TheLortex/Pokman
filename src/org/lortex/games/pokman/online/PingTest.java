package org.lortex.games.pokman.online;

public class PingTest {
	MultiplayerHandler mOnline;
	
	public PingTest(MultiplayerHandler multiHandler) {
		mOnline = multiHandler;
	}
	
	public void pingHost() {
		OnlinePackets.Ping p = new OnlinePackets.Ping();
		p.time = System.nanoTime();
		mOnline.sendToHost(p, false);
	}
}
