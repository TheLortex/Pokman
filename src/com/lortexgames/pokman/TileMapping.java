package com.lortexgames.pokman;

import java.io.IOException;

import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.util.GLState;

public class TileMapping {
	
	private BitmapTextureAtlas mBitmapTextureAtlas;
	
	private TextureRegion mWallRDPT,mWallHB,mWallDG,mWallHD,mWallBD,mWallBG,mWallHG,mWallHBD,mWallBGD,mWallHBG,mWallHGD,mWallHBGD;
	private TextureRegion mWallH;
	private TextureRegion mWallB;
	private TextureRegion mWallD;
	private TextureRegion mWallG;

	private GameActivity mParent;
	private MazeGenerator map;
	private int margWallX;
	private int margWallY;
	
	TileMapping(MazeGenerator maze,GameActivity parent) {
		mParent = parent;
		map = maze;
	}
	
	public void loadTextures() throws IOException {
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
		
		mParent.getEngine().getTextureManager().loadTexture(mBitmapTextureAtlas);
	}
	
	public Sprite getWallSprite(int x, int y,Scene scene) {
		ITextureRegion wallTexture = selectTexture(x,y);
		Sprite wall = new Sprite(mParent.getMarginLeft() + x*(mParent.getTileSize()) + margWallX,mParent.getMarginTop()+y*(mParent.getTileSize()) + margWallY,wallTexture,mParent.getVertexBufferObjectManager());
		
		return wall;
	}
	
	public BitmapTextureAtlas getTextureAtlas() {
		return mBitmapTextureAtlas;
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
}
