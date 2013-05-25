package com.lortexgames.pokman.activities;

import java.io.IOException;
import java.io.InputStream;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.bitmap.BitmapTexture;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.adt.io.in.IInputStreamOpener;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.lortexgames.pokman.FontManager;
import com.lortexgames.pokman.R;
import com.lortexgames.pokman.R.string;
import com.lortexgames.pokman.addons.Paginator;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;
import android.util.SparseArray;

public class HighscoresActivity extends SimpleBaseGameActivity { 
	private FontManager font;
	private Camera camera;
	private TextureRegion mHighScoreBGTextureRegion;
	private Scene mScene;
	private int mInternetUsage;

	private SparseArray<Pair<Integer, String>> scoreList=new SparseArray<Pair<Integer, String>>();
	private SparseArray<Pair<Integer, String>> onlineScoreList=new SparseArray<Pair<Integer, String>>();
	private SparseArray<Pair<Integer, String>> onlineWeeklyScoreList=new SparseArray<Pair<Integer, String>>();
	
	DefaultHttpClient client = new DefaultHttpClient();
	ResponseHandler<String> responseHandler = new BasicResponseHandler();
	String response = "";
	protected float xcoor;
	private Paginator pagination;

	@Override
	public EngineOptions onCreateEngineOptions() {
		camera = new Camera(0, 0, MenuActivity.getWidth(), MenuActivity.getHeight() );
		return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(MenuActivity.getWidth(),MenuActivity.getHeight()), camera);
	}

	@Override
	protected void onCreateResources() {
		try {
			ITexture highScoreTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
			    @Override
			    public InputStream open() throws IOException {
			        return getAssets().open("gfx/highscorebg.png");
			    }
			});
			
			highScoreTexture.load();
			this.mHighScoreBGTextureRegion = TextureRegionFactory.extractFromTexture(highScoreTexture);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		font = new FontManager(this);
		
		font.load(60, Color.WHITE);
		font.load(50, Color.WHITE);
		font.load(42, Color.WHITE);
		font.load(42, Color.YELLOW);
		font.load(48, Color.WHITE);
		font.load(30, Color.WHITE);
		font.load(50, Color.YELLOW);
		
	}
	
	
	@Override
	protected Scene onCreateScene() {
		mScene = new Scene();
		mScene.setBackground(new Background(0f,0f,0f));
		
		SharedPreferences settings = getSharedPreferences(MenuActivity.PREFS_NAME, 0);
		mInternetUsage = settings.getInt("internetUsage", OptionsActivity.IU_WIFI_ONLY);
		
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mMobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		boolean wifiAvailable   = (mWifi == null) ? false:mWifi.isConnected();
		boolean mobileAvailable = (mMobile == null) ? false:mMobile.isConnected();
		
		boolean useNetwork=true;
		
		if(mInternetUsage==OptionsActivity.IU_NEVER) {
			useNetwork=false;
		} else if(mInternetUsage==OptionsActivity.IU_WIFI_ONLY) {
		    if (!wifiAvailable) {
		    	useNetwork=false;
		    }
		}else if((!mobileAvailable) && (!wifiAvailable)) {
			useNetwork=false;
		}
		
		if(useNetwork) {
			getLocalHighscores();
			drawLocal(MenuActivity.getWidth());
			Handler asyncTaskHandler = new Handler(Looper.getMainLooper());
		    asyncTaskHandler.post(new Runnable() {
				@Override
				public void run() {
			    	new AsyncScoreGet().execute((Void)null);
				}
		    });
		} else {
			getLocalHighscores();
			drawLocal(0);
		}
	
		if(useNetwork) {
			mScene.setOnSceneTouchListener(new IOnSceneTouchListener() {
				@Override
				public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
					if(pSceneTouchEvent.isActionDown()) {
						xcoor = pSceneTouchEvent.getX();
					} else if(pSceneTouchEvent.isActionMove()) {
						camera.offsetCenter(xcoor - pSceneTouchEvent.getX(), 0);
						pagination.setX(pagination.getX()+xcoor - pSceneTouchEvent.getX());
					} else if(pSceneTouchEvent.isActionUp()) {
						int page;
					/*	if(camera.getCenterX() < 0) {
							camera.setCenter(MenuActivity.getWidth()/2f, MenuActivity.getHeight()/2f);
							camera.offsetCenter(-MenuActivity.getWidth(), 0);
							pagination.setX(-MenuActivity.getWidth());
							page=1;
						} else */if(camera.getCenterX() < MenuActivity.getWidth()) {
							camera.setCenter(MenuActivity.getWidth()/2f, MenuActivity.getHeight()/2f);
							pagination.setX(0);
							page=1;
						} else {
							camera.setCenter(MenuActivity.getWidth()/2f, MenuActivity.getHeight()/2f);
							camera.offsetCenter(MenuActivity.getWidth(), 0);
							pagination.setX(MenuActivity.getWidth());
							page=2;
						}
						pagination.setPage(page);
					}
					return false;
				}
			});
			pagination = new Paginator(20, MenuActivity.getHeight()-20, MenuActivity.getWidth()-40, 15, 2, 1, this.getVertexBufferObjectManager());
			mScene.attachChild(pagination);
		}
		return mScene;
	}
	
	protected void drawLocal(int offsetX) {
		// TITLE
		Text highLocalText = new Text(0,120,font.get(60, Color.WHITE),getResources().getString(R.string.end_local),this.getVertexBufferObjectManager());
		highLocalText.setX(offsetX+ (MenuActivity.getWidth()/2f - highLocalText.getWidth()/2f));
		mScene.attachChild(highLocalText);
		//Background
		Sprite highScoreTable = new Sprite(offsetX,250,720,800,this.mHighScoreBGTextureRegion,this.getVertexBufferObjectManager());
		mScene.attachChild(highScoreTable);
		// Values
				
		for(int i=1;i<=scoreList.size();i++) {
			int curScore = scoreList.get(i).first;
			String curName = scoreList.get(i).second;
			String curScoreText = curScore+"";
			String space = "";
			for(int j=0;j<15-curScoreText.length()-curName.length();j++)
				space = space + " ";
			
			Text curBann = new Text(offsetX+50,320+(i-1)*156,font.get(42),curName + space + curScoreText,this.getVertexBufferObjectManager());
		
			mScene.attachChild(curBann);
		}
		
	}
	
	protected void drawOnline(int offsetX) {
		//TITLE
		Text highText = new Text(0,120,font.get(60, Color.WHITE),getResources().getString(R.string.end_global),this.getVertexBufferObjectManager());
		highText.setX(offsetX+ (MenuActivity.getWidth()/2f - highText.getWidth()/2f));
		mScene.attachChild(highText);
		//Background
		Sprite highTable = new Sprite(offsetX,250,720,800,this.mHighScoreBGTextureRegion,this.getVertexBufferObjectManager());
		mScene.attachChild(highTable);
		// Values
				
		for(int i=0;i<onlineScoreList.size();i++) {
			int curScore = onlineScoreList.get(i).first;
			String curName = onlineScoreList.get(i).second;
			String curScoreText = curScore+"";
			String space = "";
			for(int j=0;j<15-curScoreText.length()-curName.length();j++)
				space = space + " ";
			
			Text curBann = new Text(offsetX+50,320+i*156,font.get(42),curName + space + curScoreText,this.getVertexBufferObjectManager());
		
			mScene.attachChild(curBann);
		}
	}
	/*
	protected void drawWeeklyOnline(int offsetX) {
		//TITLE
		Text highText = new Text(0,120,font.get(60, Color.WHITE),getResources().getString(R.string.end_global_weekly),this.getVertexBufferObjectManager());
		highText.setX(offsetX+ (MenuActivity.getWidth()/2f - highText.getWidth()/2f));
		mScene.attachChild(highText);
		//Background
		Sprite highTable = new Sprite(offsetX,250,720,800,this.mHighScoreBGTextureRegion,this.getVertexBufferObjectManager());
		mScene.attachChild(highTable);
		// Values
				
		for(int i=0;i<onlineWeeklyScoreList.size();i++) {
			int curScore = onlineWeeklyScoreList.get(i).first;
			String curName = onlineWeeklyScoreList.get(i).second;
			String curScoreText = curScore+"";
			String space = "";
			for(int j=0;j<15-curScoreText.length()-curName.length();j++)
				space = space + " ";
			
			Text curBann = new Text(offsetX+50,320+i*156,font.get(42),curName + space + curScoreText,this.getVertexBufferObjectManager());
		
			mScene.attachChild(curBann);
		}
	}*/
	
	protected void getLocalHighscores() {
		SharedPreferences settings = getSharedPreferences(MenuActivity.PREFS_NAME, 0); 
		for(int i=1;i<=5;i++) {
			int i_score = settings.getInt("HighScore"+i, 0); 
			String i_name = settings.getString("HighScoreName"+i, ""); 
			Pair<Integer,String> pair = new Pair<Integer,String>(i_score,i_name);
			scoreList.put(i, pair);
		} 
	}
	
	private class AsyncScoreGet extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			
			ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
			NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			NetworkInfo mMobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

			boolean wifiAvailable   = (mWifi == null) ? false:mWifi.isConnected();
			boolean mobileAvailable = (mMobile == null) ? false:mMobile.isConnected();
			
			
			if(mInternetUsage==OptionsActivity.IU_NEVER) {
				return null;
			} else if(mInternetUsage==OptionsActivity.IU_WIFI_ONLY) {
			    if (!wifiAvailable) {
					return null;
			    }
			}
			
			if((!mobileAvailable) && (!wifiAvailable)) {
				return null;
			}

			HttpPost postMethod = new HttpPost("http://lortexgames.alwaysdata.net/get.php");

			try {
				response = client.execute(postMethod, responseHandler);
				//Debug.i(response);
		        JSONArray jsonArray  = new JSONArray(response);
		        for(int i=0; i<jsonArray.length(); i++){
		            JSONObject j;
						j = jsonArray.getJSONObject(i);
					
		            String name = j.get("name").toString();
		            int score = Integer.parseInt(j.get("score").toString());
		            onlineScoreList.append(i, new Pair<Integer,String>(score,name));
		         //   onlineWeeklyScoreList.append(i, new Pair<Integer,String>(score,name));
			    }
				
				if(postMethod.getEntity() != null ) {
					postMethod.getEntity().consumeContent();
			    }

				HighscoresActivity.this.drawOnline(0);
			//	HighscoresActivity.this.drawWeeklyOnline(0);
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
}
