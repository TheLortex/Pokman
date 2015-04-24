package org.lortex.games.pokman.common;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;
import android.util.SparseIntArray;

public class GameMode {
	public static final int SINGLEPLAYER = 0;
	public static final int CHOOSE_LEVEL = 1;
	public static final int MULTIPLAYER_TEST = 2;
	public static final int ONLINE_MULTIPLAYER_TEST = 3;

	public int level;
	public int mode;
	public int level_width  = 15;
	public int level_height = 25;

	public SparseIntArray stat_nlifes;
	public SparseIntArray stat_scores;

	public int stat_nlifes_default = 3;
	public int stat_score_default = 0;
	
	public boolean authoritative;
	
	public GameMode(int mode_p, int level_p) {
		mode  =  mode_p;
		level = level_p;

		stat_nlifes = new SparseIntArray();
		stat_scores = new SparseIntArray();
		authoritative = true;
		
	}
	
	public GameMode(int mode_p, int level_p, SparseIntArray nlifes, SparseIntArray scores, boolean isAuthoritative) {
		mode  =  mode_p;
		level = level_p;
		stat_nlifes = nlifes;
		stat_scores = scores;
		authoritative = isAuthoritative;
	}
}
