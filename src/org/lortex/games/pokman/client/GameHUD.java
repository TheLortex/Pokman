package org.lortex.games.pokman.client;

import org.andengine.engine.camera.hud.HUD;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.text.Text;
import org.lortex.games.pokman.client.scenes.GameScene;
import org.lortex.games.pokman.common.Packets.PlayerDataUpdate;

import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;

public class GameHUD extends HUD{
	public static final int HUD_HEIGHT = 200;


	SparseArray<Text> renderedText 	= new SparseArray<Text>();

	SparseIntArray 	  scoreData 		= new SparseIntArray(); 
	SparseIntArray 	  lifeData 			= new SparseIntArray();


	private GameScene mParent;

	
	public GameHUD(GameScene parent) {
		mParent = parent;
		renderHUD();
	}

	public void updateHUD(final PlayerDataUpdate theTruc) {
		scoreData.put(theTruc.id, theTruc.score);
		lifeData.put(theTruc.id, theTruc.nlifes);
		if(renderedText.get(theTruc.id) == null) {
			clearHUD();
			renderHUD();
		} else {
			Log.i("LORTEX", "Swag mon id: "+mParent.mon_id + " vs id: "+theTruc.id);
			mParent.getContext().runOnUpdateThread(new Runnable() {
				@Override
				public void run() {
					String txt = generateText(theTruc.id, theTruc.score, theTruc.nlifes, scoreData.size(), theTruc.id == mParent.mon_id);
					renderedText.get(theTruc.id) .setText(txt);
				}
			});
		}
	}
	
	private void clearHUD() {
		this.detachChildren();
		
		renderedText.clear();
		
	}
	
	private String generateText(int id, int score, int nlifes, int nplayers, boolean isCurrentUser) {
		if(nplayers == 1) {
			return "PLAYER "+id+ "  \n SCORE : "+ String.format("% 5d", score) +" \n LIFES : "+String.format("% 5d", nlifes);
		} else {
			if(isCurrentUser) {
				return "-> P"+id+ ":"+ String.format("% 5d", score) +"PTS/"+nlifes+"L    ";
			} else {
				return "P"+id+ ":"+ String.format("% 5d", score) +"PTS/"+nlifes+"L";
			}
		}
	}
	
	private void renderHUD() {
		Rectangle r = new Rectangle(mParent.getContext().getWidth()/2f, HUD_HEIGHT, mParent.getContext().getWidth()*0.75f, 2, mParent.getContext().getVertexBufferObjectManager());
		this.attachChild(r);
		
		int nplayers = scoreData.size();

		if(nplayers == 1) {
			int id = scoreData.keyAt(0);
			
			int score = scoreData.get(id);
			int nlifes = lifeData.get(id);
			
			Text scoreText = new Text(mParent.getContext().getWidth()/2f, HUD_HEIGHT/2f, mParent.getPokmanFontManager().get(42),generateText(id, score, nlifes, 1, true),100, mParent.getContext().getVertexBufferObjectManager());
			renderedText.append(id, scoreText);
			this.attachChild(scoreText);
		} else {
			
			for(int i=0;i<nplayers;i++) {
				int id = scoreData.keyAt(i);
				int score = scoreData.get(id);
				int nlifes = lifeData.get(id);

				Text scoreText = new Text(mParent.getContext().getWidth()/2f, HUD_HEIGHT-30-40*i, mParent.getPokmanFontManager().get(36),generateText(id, score, nlifes, nplayers, false),100,mParent.getContext().getVertexBufferObjectManager());
				renderedText.append(id, scoreText);
				this.attachChild(scoreText);
				
			}
		}
		
	}
	
}
