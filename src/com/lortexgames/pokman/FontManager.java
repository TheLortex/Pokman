package com.lortexgames.pokman;

import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.ui.activity.SimpleBaseGameActivity;

import android.util.SparseArray;

public class FontManager {
	private SparseArray<SparseArray<Font>> fonts;
	private static final String file="font/police.ttf";
	private SimpleBaseGameActivity parent;
	
	public FontManager(SimpleBaseGameActivity i_parent) {
		parent = i_parent;
		fonts = new SparseArray<SparseArray<Font>>();
	}
	
	public void load(int size, int color) {
		final ITexture fontNormTexture = new BitmapTextureAtlas(parent.getTextureManager(), 512, 512, TextureOptions.BILINEAR);
		Font fontTR = FontFactory.createFromAsset(parent.getFontManager(), fontNormTexture, parent.getAssets(), file,(float) size, true, color);
		fontTR.load();
		if(fonts.get(size) == null) {
			SparseArray<Font> h = new SparseArray<Font>();
			h.put(color, fontTR);
			fonts.put(size, h);
		} else {
			fonts.get(size).put(color, fontTR);
		}
	}
	
	public Font get(int size, int color) {
		return fonts.get(size).get(color);
	}
}
