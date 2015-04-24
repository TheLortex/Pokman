package org.lortex.games.pokman.client;

import java.io.IOException;

import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.texturepacker.opengl.texture.util.texturepacker.TexturePack;
import org.andengine.extension.texturepacker.opengl.texture.util.texturepacker.TexturePackLoader;
import org.andengine.extension.texturepacker.opengl.texture.util.texturepacker.TexturePackTextureRegionLibrary;
import org.andengine.extension.texturepacker.opengl.texture.util.texturepacker.exception.TexturePackParseException;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.debug.Debug;
import org.lortex.games.pokman.common.Element;
import org.lortex.games.pokman.common.MazeGenerator;
import org.lortex.games.pokman.common.walls;

import com.badlogic.gdx.math.Vector2;

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
	
	public static final int TILE_SIZE = 40;
	
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
	}
	
	public Sprite getWallSprite(int posX, int posY,int gridX, int gridY) {
		ITextureRegion wallTexture = selectTexture(gridX,gridY);
		Sprite wall = new Sprite(posX+ margWallX,posY + margWallY,wallTexture,mParent.getVertexBufferObjectManager());
		
		return wall;
	}
	
	
	public ITexture getTexture() {
		return texturePack.getTexture();
	}
	

	public Vector2 getComputedOffset() {
		return new Vector2(margWallX - TILE_SIZE/2, margWallY);
	}
	
	public ITextureRegion selectTexture(int x,int y) {
		ITextureRegion selected=null;

		boolean h,b,d,g;

		h = (map.value(x, y+1) == Element.WALL);
		b = (map.value(x, y-1) == Element.WALL);
		d = (map.value(x+1, y) == Element.WALL);
		g = (map.value(x-1, y) == Element.WALL);


		margWallX = 0;
		margWallY = 0;
		
		if(h) {
			margWallY = 2;
			if(b) {
				margWallY = 0;
				if(d) {
					if(g) {
						selected = mWallHBGD;
					} else {
						margWallX = 2;
						selected = mWallHBD;
					}
				} else if(g) {
					margWallX = -2;
					selected = mWallHBG;
				}
				else {selected = mWallHB;}
			}else if(d) {
				if(g) {
					selected = mWallHGD;
				}
				else {
					margWallX = 2;
					selected = mWallHD;
				}
			} else if(g) {
				margWallX = -2;
				selected = mWallHG;
			}else {selected = mWallH;}
		}else if(b) { 
			margWallY = -2;
			if(d) {
				if(g) { 
					selected=mWallBGD;
				}else {
					margWallX = 2;
					selected=mWallBD;
				}
			} else if(g) {
				margWallX = -2;
				selected=mWallBG;
			}else {
				selected=mWallB;
			}
		} else if(d) {
			if(g) {
				selected=mWallDG;
			}else {
				selected=mWallD;
			}
		} else if(g) {
			selected=mWallG;
		}else {
			selected = mWallRDPT;
		}

		margWallX += TILE_SIZE/2;
		
		return selected;
	}

	public void release() {
	}


}