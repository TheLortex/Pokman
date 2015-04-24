package org.lortex.games.pokman.client;

import org.andengine.entity.Entity;
import org.andengine.entity.primitive.Line;
import org.andengine.opengl.vbo.VertexBufferObjectManager;


//Modified by LortexGames
public class Paginator extends Entity {
	private int mX, mY, mWidth, mHeight;
	private int mNPages;
	private float mCurPage;
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
		Line bottom = new Line(mX, mY-largeur*2, mX+mWidth, mY-largeur*2, largeur, mBuf);
		bottom.setColor(0.7f,0.7f,0.7f);
		this.attachChild(bottom);
		
		indicator = new Line(mX,mY,mX+mIndicatorWidth, mY, mHeight, mBuf);
		indicator.setColor(1f,1f,1f);
		this.attachChild(indicator);
		
		setPage(mCurPage);
	}
	
	public void setPage(float curPage) {
		mCurPage=curPage;
		indicator.setPosition(mX + mIndicatorWidth*(curPage-1), mY, mX + mIndicatorWidth*curPage, mY);
	}
}