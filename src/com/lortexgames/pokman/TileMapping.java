package com.lortexgames.pokman;

import java.io.IOException;

import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.texturepacker.opengl.texture.util.texturepacker.TexturePack;
import org.andengine.extension.texturepacker.opengl.texture.util.texturepacker.TexturePackLoader;
import org.andengine.extension.texturepacker.opengl.texture.util.texturepacker.TexturePackTextureRegionLibrary;
import org.andengine.extension.texturepacker.opengl.texture.util.texturepacker.exception.TexturePackParseException;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.Texture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.debug.Debug;

public class TileMapping implements walls{
	private TexturePackTextureRegionLibrary texturePackLibrary;
	private TexturePack texturePack;
	
	private TextureRegion mWallRDPT,mWallHB,mWallDG,mWallHD,mWallBD,mWallBG,mWallHG,mWallHBD,mWallBGD,mWallHBG,mWallHGD,mWallHBGD;
	private TextureRegion mWallH;
	private TextureRegion mWallB;
	private TextureRegion mWallD;
	private TextureRegion mWallG;

	private MazeGenerator map;
	private int margWallX;
	private int margWallY;

	private SimpleBaseGameActivity mParent;
	private BitmapTextureAtlas mBitmapTextureAtlas;
	
	public TileMapping(MazeGenerator maze,SimpleBaseGameActivity parent) {
		mParent = parent;
		map = maze;
	}

	public void loadTextures() throws IOException {
		
		try 
	    {
	        texturePack = new TexturePackLoader(mParent.getTextureManager(), "gfx/walls/").loadFromAsset(mParent.getAssets(), "walls.xml");
	        texturePack.loadTexture();
	        texturePackLibrary = texturePack.getTexturePackTextureRegionLibrary();
	    } 
	    catch (final TexturePackParseException e) 
	    {
	        Debug.e(e);
	    }

		this.mWallRDPT = texturePackLibrary.get(WALL_RDPT_ID);
		this.mWallHB = texturePackLibrary.get(WALL_HB_ID);
		this.mWallDG = texturePackLibrary.get(WALL_DG_ID);
		this.mWallHD = texturePackLibrary.get(WALL_HD_ID);
		this.mWallBD = texturePackLibrary.get(WALL_BD_ID);
		this.mWallBG = texturePackLibrary.get(WALL_BG_ID);
		this.mWallHG = texturePackLibrary.get(WALL_HG_ID);
		this.mWallHBD = texturePackLibrary.get(WALL_HBD_ID);
		this.mWallHBG = texturePackLibrary.get(WALL_HBG_ID);
		this.mWallHGD = texturePackLibrary.get(WALL_HGD_ID);
		this.mWallBGD = texturePackLibrary.get(WALL_BGD_ID);
		this.mWallHBGD = texturePackLibrary.get(WALL_HBDG_ID);
		this.mWallH = texturePackLibrary.get(WALL_H_ID);
		this.mWallB = texturePackLibrary.get(WALL_B_ID);
		this.mWallD = texturePackLibrary.get(WALL_D_ID);
		this.mWallG = texturePackLibrary.get(WALL_G_ID);
		/*
		this.mBitmapTextureAtlas = new BitmapTextureAtlas(mParent.getTextureManager(), 256, 256, TextureOptions.NEAREST_PREMULTIPLYALPHA);
		//mBitmapTextureAtlas.
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		
		
		this.mWallRDPT = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, mParent.getApplicationContext(), "wall-rdpt.png", 0, 0); 
		this.mWallHB = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, mParent.getApplicationContext(), "wall-hb.png", 0, 40); 
		this.mWallDG = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, mParent.getApplicationContext(), "wall-dg.png", 40, 0); 
		this.mWallHD = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, mParent.getApplicationContext(), "wall-hd.png", 40, 40); 
		this.mWallBD = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, mParent.getApplicationContext(), "wall-bd.png", 80, 0); 
		this.mWallBG = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, mParent.getApplicationContext(), "wall-bg.png", 80, 40); 
		this.mWallHG = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, mParent.getApplicationContext(), "wall-hg.png", 80, 80); 
		this.mWallHBD = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, mParent.getApplicationContext(), "wall-hbd.png", 0, 80); 
		this.mWallHBG = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, mParent.getApplicationContext(), "wall-hbg.png", 40, 80); 
		this.mWallHGD = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, mParent.getApplicationContext(), "wall-hgd.png", 120, 0); 
		this.mWallBGD = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, mParent.getApplicationContext(), "wall-bgd.png", 120, 40); 
		this.mWallHBGD = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, mParent.getApplicationContext(), "wall-hbdg.png", 120, 80); 
		this.mWallH = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, mParent.getApplicationContext(), "wall-h.png", 120, 120); 
		this.mWallB = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, mParent.getApplicationContext(), "wall-b.png", 0, 120); 
		this.mWallD = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, mParent.getApplicationContext(), "wall-d.png", 40, 120); 
		this.mWallG = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, mParent.getApplicationContext(), "wall-g.png", 80, 120); 
		
		mParent.getEngine().getTextureManager().loadTexture(mBitmapTextureAtlas);*/
	}
	
	public Sprite getWallSprite(int posX, int posY,int gridX, int gridY,Scene scene) {
		ITextureRegion wallTexture = selectTexture(gridX,gridY);
		Sprite wall = new Sprite(posX+ margWallX,posY + margWallY,wallTexture,mParent.getVertexBufferObjectManager());
		
		return wall;
	}
	
	public ITexture getTexture() {
		return texturePack.getTexture();
		//return mBitmapTextureAtlas;
	}
	
	protected ITextureRegion selectTexture(int x,int y) {
		ITextureRegion selected=null;

		boolean h,b,d,g;

		h = (map.value(x, y-1) == Element.MUR);
		b = (map.value(x, y+1) == Element.MUR);
		d = (map.value(x+1, y) == Element.MUR);
		g = (map.value(x-1, y) == Element.MUR);

		margWallY=4;
		margWallX=4;
		
		if(h) {
			margWallY=0;
			if(b) {
				if(d) {
					if(g) {
						margWallX=0;
						selected = mWallHBGD;
					} else {
						selected = mWallHBD;
					}
				} else if(g) {
					selected = mWallHBG;
					margWallX=0;
				}
				else {selected = mWallHB;}
			}else if(d) {
				if(g) {
					margWallX=0;
					selected = mWallHGD;
				}
				else {selected = mWallHD;}
			} else if(g) {
				margWallX=0;
				selected = mWallHG;
			}else {selected = mWallH;}
		}else if(b) { 
			if(d) {
				if(g) { 
					margWallX=0;
					selected=mWallBGD;
				}else {
					selected=mWallBD;
				}
			} else if(g) {
				margWallX=0;
				selected=mWallBG;
			}else {
				selected=mWallB;
			}
		} else if(d) {
			if(g) {
				margWallX=0;
				selected=mWallDG;
			}else {
				selected=mWallD;
			}
		} else if(g) {
			margWallX=0;
			selected=mWallG;
		}else {
			selected = mWallRDPT;
		}
		
		return selected;
	}

	public void release() {
		/*mBitmapTextureAtlas.clearTextureAtlasSources();
		mBitmapTextureAtlas.unload();*/
	}
}
