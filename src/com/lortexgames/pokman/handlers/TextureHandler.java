package com.lortexgames.pokman.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.bitmap.BitmapTexture;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.adt.io.in.IInputStreamOpener;

public class TextureHandler {
	
	private HashMap<String,TextureRegion> mTextureRegions = new HashMap<String,TextureRegion>();
	private SimpleBaseGameActivity mContext;
	
	public TextureHandler(SimpleBaseGameActivity context) {
		mContext = context;
	}

	public void load(String name, final String path) {
		try {
			ITexture texture;
			texture = new BitmapTexture(mContext.getTextureManager(),new IInputStreamOpener() { @Override public InputStream open() throws IOException {return mContext.getAssets().open(path);}});
			texture.load();
			TextureRegion tr = TextureRegionFactory.extractFromTexture(texture);
			mTextureRegions.put(name, tr);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public TextureRegion get(String name) {
		return mTextureRegions.get(name);
	}
}
