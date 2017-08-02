package org.lortex.games.pokman.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

import org.lortex.games.pokman.server.LevelManager;

import android.util.Pair;


public class MazeGenerator {
	private Vector<Vector<Element>> map;
	private int mWidth, mHeight;
	private int nWalls;
	
	private int MAX_WALL_LENGHT=7;
	
	private long mSeed;
	private Random rand;
	
	public MazeGenerator(int width, int height,int level) {
		this.mWidth = width;
		this.mHeight = height;

		rand = new Random(System.currentTimeMillis());
		
		nWalls=0;
		
		map = new Vector<Vector<Element>>();
		for(int i=0;i<this.mWidth;i++)  {
			map.add(new Vector<Element>());
			for(int j=0;j<this.mHeight;j++)  {
				map.get(i).add(Element.EMPTY);
			}
		}
		MAX_WALL_LENGHT = (new LevelManager()).get(level).maxWallLenght;
		
		grid();
	}
	
	public MazeGenerator(int level_width, int level_height, int level, long seed) {
		this(level_width, level_height, level);
		mSeed = seed;
		rand = new Random(mSeed);
	}

	public void randomize() {
		grid(); 
		
		ArrayList<Pair<Integer, Integer>> coordRand = new ArrayList<Pair<Integer, Integer>>();
		
		for(int i=1;i<this.mWidth;i+=2) {
			for(int j=2;j<this.mHeight;j+=2) {
				coordRand.add(new Pair<Integer, Integer>(i,j));
				coordRand.add(new Pair<Integer, Integer>(i+1,j-1));
			}
		}
		
		Collections.shuffle(coordRand, rand);
		Iterator<Pair<Integer, Integer>> it = coordRand.iterator();
		while(it.hasNext()) {
			Pair<Integer, Integer> coord = it.next();
			int testX = coord.first;
			int testY = coord.second;

			int num = (randok(0.5)) ? 3 : 3;
			int num2 = (randok(0.5)) ? 3 : 3;
			
			if(testX%2 == 0) {
				if((nbLinks(testX-1,testY)>=num)&&(nbLinks(testX+1,testY)>=num2)&&(wallLenght(testX,testY-1,testX,testY)+wallLenght(testX,testY+1,testX,testY)<MAX_WALL_LENGHT)) {
	
					nWalls++;
					value(testX,testY,Element.WALL);
				}
			} else {
				if((nbLinks(testX,testY-1)>=num)&&(nbLinks(testX,testY+1)>=num2)&&(wallLenght(testX-1,testY,testX,testY)+wallLenght(testX+1,testY,testX,testY)<MAX_WALL_LENGHT)) {
					nWalls++;
					value(testX,testY,Element.WALL);
				}
			}
		}
	}
	
	private int wallLenght(int x,int y,int prevX, int prevY) {
		if(value(x,y)!=Element.WALL) {
			return 0;
		} else if((x == 0)||(y == 0)||(x == mWidth-1)||(y==mHeight-1)) {
			return 1;
		} else {
			int gauche=0, droite=0,haut=0,bas=0;
			if(x-1 != prevX)
				gauche = wallLenght(x-1,y,x, y);
			if(x+1 != prevX)
				droite = wallLenght(x+1,y,x, y);
			if(y-1 != prevY)
				haut = wallLenght(x,y-1,x, y);
			if(y+1 != prevY)
				bas = wallLenght(x,y+1,x, y);
			
			
			return Math.max(Math.max(haut, bas), Math.max(droite, gauche))+1;
		}
	}
	
	public void customMap(Element[][] map) {
		for(int i=0;i<mWidth;i++) {
			for(int j=0;j<mHeight;j++) {
				value(i,j,map[j][i]);
			}
		}
	}
	
	public int getCountWalls(boolean b) {
		if(b == false)
			return nWalls;
		else {
			nWalls = 0;
			for(int x=0;x<mWidth;x++) 
				for(int y=0;y<mHeight;y++) 
					if(value(x,y) == Element.WALL)
						nWalls++;
			return nWalls;
		}
	}
	
	public Vector<Vector<Element>> getMap() {
		return map;
	}

	public Element value(int x,int y) {
		if((x<0)||(y<0)||(x>=mWidth)||(y>=mHeight))
			return Element.EMPTY;
		else 
			return map.get(x).get(y);
	}
	
	public void value(int x,int y, Element v) {
		if(!((x<0)||(y<0)||(x>=mWidth)||(y>=mHeight)))
			map.get(x).set(y,v);
	}
	
	protected void grid() {
		for(int y=0;y<this.mHeight;y++) {
			for(int x=0;x<this.mWidth;x++) {
				if(((x%2 == 0) && (y%2==0)) || (x==0) || (y==0) || (x==mWidth-1) || (y==mHeight-1)){
					nWalls++;
					value(x,y,Element.WALL);
				} else if(((x==1)||(x==mWidth-2))&&((y==1)||(y==mHeight-2))){
					value(x,y,Element.SPAWNGHOST);
				} else if(((x==3)||(x==mWidth-4))&&((y==3)||(y==mHeight-4))){
					value(x,y,Element.BONUS);
				} else if((x==(mWidth+1)/2-(((mWidth+3)/2)%2))&&(y==((mHeight+1)/2)-(((mHeight+3)/2)%2))){
					value(x,y,Element.SPAWNPAC);
				}else{
					value(x,y,Element.POINT);
				}
			}
		}
		
		
	}
	
	protected boolean randok(double freq) {
		return rand.nextDouble() < freq ;
	}
	
	protected int nbLinks(int x,int y) {
		if(value(x,y) == Element.WALL)
			return 0;
		else {
			int links = 0;
			if((value(x,y-1) != Element.WALL))
				links++;
			if((value(x,y+1) != Element.WALL))
				links++;
			if((value(x-1,y) != Element.WALL))
				links++;
			if((value(x+1,y) != Element.WALL))
				links++;
			
			return links;
		}
	}

	public int getWidth() {
		return mWidth;
	}
	public int getHeight() {
		return mHeight;
	}
}