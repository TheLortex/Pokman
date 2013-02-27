package  com.lortexgames.pokman;

import java.util.Vector;



public class MazeGenerator {
	private Vector<Vector<Element>> map;
	private int mWidth, mHeight;
	private int nWalls;
	
	private int MAX_WALL_LENGHT=7;
	
	MazeGenerator(int width, int height,int level) {
		this.mWidth = width;
		this.mHeight = height;
		
		nWalls=0;
		
		map = new Vector<Vector<Element>>();
		for(int i=0;i<this.mWidth;i++)  {
			map.add(new Vector<Element>());
			for(int j=0;j<this.mHeight;j++)  {
				map.get(i).add(Element.VIDE);
			}
		}
		MAX_WALL_LENGHT = (new LevelManager()).get(level).maxWallLenght;
		grid();
	}
	
	public void randomize() {
		grid(); 
		int timeout = 0;
		
		while(timeout < 1000) {

			timeout++;
			int testX, testY;
			if(randok(0.5)) {
				testX = (int) (Math.floor(Math.random()*(this.mWidth-1)/2.0)*2);
				if(testX == 0)
					testX = 2;
				testY = (int) (Math.floor(Math.random()*(this.mHeight-1)/2.0)*2+1);
			} else {
				testX = (int) (Math.floor(Math.random()*(this.mWidth-1)/2.0)*2+1);
				testY = (int) (Math.floor(Math.random()*(this.mHeight-1)/2.0)*2);
				if(testY == 0)
					testY = 2;
			}

			int num = (randok(0.5)) ? 3 : 4;
			int num2 = (randok(0.5)) ? 3 : 4;
			
			if(testX%2 == 0) {
				if((nbLinks(testX-1,testY)>=num)&&(nbLinks(testX+1,testY)>=num2)&&(wallLenght(testX,testY-1,testX,testY)+wallLenght(testX,testY+1,testX,testY)<MAX_WALL_LENGHT)) {
	
					nWalls++;
					value(testX,testY,Element.MUR);
				}
			} else {
				if((nbLinks(testX,testY-1)>=num)&&(nbLinks(testX,testY+1)>=num2)&&(wallLenght(testX-1,testY,testX,testY)+wallLenght(testX+1,testY,testX,testY)<MAX_WALL_LENGHT)) {
					nWalls++;
					value(testX,testY,Element.MUR);
				}
			}
			
		}
	}
	
	private int wallLenght(int x,int y,int prevX, int prevY) {
		if(value(x,y)!=Element.MUR) {
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
	
	public int getCountWalls() {
		return nWalls;
	}
	
	public Vector<Vector<Element>> getMap() {
		return map;
	}

	public Element value(int x,int y) {
		if((x<0)||(y<0)||(x>=mWidth)||(y>=mHeight))
			return Element.VIDE;
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
					value(x,y,Element.MUR);
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
		return Math.random() < freq ;
	}
	
	protected int nbLinks(int x,int y) {
		if(value(x,y) == Element.MUR)
			return 0;
		else {
			int links = 0;
			if((value(x,y-1) != Element.MUR))
				links++;
			if((value(x,y+1) != Element.MUR))
				links++;
			if((value(x-1,y) != Element.MUR))
				links++;
			if((value(x+1,y) != Element.MUR))
				links++;
			
			return links;
		}
	}
}