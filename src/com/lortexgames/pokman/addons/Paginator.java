package com.lortexgames.pokman.addons;

import org.andengine.entity.Entity;
import org.andengine.entity.primitive.Line;
import org.andengine.opengl.vbo.VertexBufferObjectManager;


//Modified by LortexGames
public class Paginator extends Entity {
	private int mX, mY, mWidth, mHeight;
	private int mNPages;
	private int mCurPage;
	private VertexBufferObjectManager mBuf;
	private Line indicator;
	private float mIndicatorWidth;
	
	private static final int largeur=5;
	
	public Paginator (int x, int y, int width, int height, int nPages, int curPage,VertexBufferObjectManager buf){
		mX = x;
		mY = y;
		mWidth=width;
		mHeight=height;
		mNPages=nPages;
		mCurPage=curPage;
		mBuf=buf;
		
		mIndicatorWidth=mWidth/((float)mNPages);
		
		drawElements();
	}

	private void drawElements() {
		Line bottom = new Line(mX, mY+mHeight-largeur*2, mX+mWidth, mY+mHeight-largeur*2, largeur, mBuf);
		bottom.setColor(0.7f,0.7f,0.7f);
		this.attachChild(bottom);
		
		indicator = new Line(0,mY,mIndicatorWidth, mY, mHeight, mBuf);
		indicator.setColor(1f,1f,1f);
		this.attachChild(indicator);
		
		setPage(mCurPage);
	}
	
	@SuppressWarnings("deprecation")
	public void setPage(int curPage) {
		mCurPage=curPage;
		indicator.setPosition(mX+(curPage-1)*mIndicatorWidth, mY);
	}
}

