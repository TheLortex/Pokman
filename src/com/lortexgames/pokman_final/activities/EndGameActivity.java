package com.lortexgames.pokman_final.activities;


import com.lortexgames.pokman_final.R;
import com.lortexgames.pokman_final.FontManager;
import com.lortexgames.pokman_final.addons.Paginator;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Line;
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
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.SharedPreferences.Editor;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.provider.Settings;
import android.provider.Settings.System;
import android.text.InputFilter;
import android.util.Pair;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;


public class EndGameActivity extends SimpleBaseGameActivity {
	private Camera camera;
	
	private boolean win;
	private int score;
	private int idDeath;
	private SoundPool mSoundPool;
	private Text returnButton;
	private Text menuButton;
	private float xcoor;
	private FontManager font;

	private TextureRegion mHighScoreBGTextureRegion;

	private SparseArray<Pair<Integer, String>> scoreList;
	private SparseArray<Pair<Integer, String>> onlineScoreList;
	
	private boolean isBetter=false;
	private int modifying=-1;
	
	private boolean onlineBetter;
	private int onlineModifying=-1;

	private Text enterName=null;
	private Text modifiableText=null;
	private Text onlineModifiableText=null;

	private int level;

	public boolean scoreUpload;
	
	DefaultHttpClient client = new DefaultHttpClient();
	ResponseHandler<String> responseHandler = new BasicResponseHandler();
	String response = "";

	private Scene mScene;
	private Handler asyncTaskHandler ;

	private Line leftBorderEN;
	private Line rightBorderEN;
	private Line highBorderEN;
	private Line lowBorderEN;

	private SharedPreferences settings;
	
	private String lastName;
	
	private Paginator pagination;
	
	private int mInternetUsage;
	
	private boolean scoreReafy=false;


	@Override
	public EngineOptions onCreateEngineOptions() {
		Intent intent = getIntent();
		win = intent.getBooleanExtra(GameActivity.WIN, false);
		score = intent.getIntExtra(GameActivity.SCORE,0);
		level = intent.getIntExtra(MenuActivity.LEVEL,1);
		
		font = new FontManager(this);
		
		SharedPreferences settings = getSharedPreferences(MenuActivity.PREFS_NAME, 0);
		final float percentageGfx = settings.getInt("sfx", 100)/100f;
		mInternetUsage = settings.getInt("internetUsage", OptionsActivity.IU_WIFI_ONLY);
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
		AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		float actualVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		final float volume = actualVolume / maxVolume;
		
		mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		if(!win) {
			idDeath = mSoundPool.load(this, R.raw.pacdie, 1);
			mSoundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
				@Override
				public void onLoadComplete(SoundPool arg0, int arg1, int arg2) {
					mSoundPool.play(idDeath, percentageGfx*volume, percentageGfx*volume, 1, 0, 1f);
				}
			});
		}

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
		
		font.load(60, Color.WHITE);
		font.load(50, Color.WHITE);
		font.load(42, Color.WHITE);
		font.load(42, Color.YELLOW);
		font.load(48, Color.WHITE);
		font.load(30, Color.WHITE);
		font.load(50, Color.YELLOW);
	}
	
	
	protected void onCreate(Bundle b) {
		super.onCreate(b);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if(scoreReafy) {
			saveHighScores();
			if(modifying!=-1) {
				Editor edit = settings.edit();
				String name = scoreList.get(modifying).second;
				edit.putString("lastName", name);
				edit.commit();
			}
		}
	}
	
	@Override
	protected Scene onCreateScene() {
		mScene = new Scene();
		mScene.setBackground(new Background(0f,0f,0f));

		settings = this.getSharedPreferences(MenuActivity.PREFS_NAME, 0);
		lastName = settings.getString("lastName", "");
		asyncTaskHandler = new Handler(Looper.getMainLooper());
		scoreList = new SparseArray<Pair<Integer,String>>();
		onlineScoreList = new SparseArray<Pair<Integer,String>>();
		getHighScores(score);

		final boolean hapticFeedback = System.getInt(this.getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED, 0) != 0;
		
		String message = win ? getResources().getString(R.string.end_win_title) : getResources().getString(R.string.end_lose_title);
		Text titleText = new Text(0,70,font.get(60, Color.WHITE),message,this.getVertexBufferObjectManager());
		titleText.setX(MenuActivity.getWidth()/2f - titleText.getWidth()/2f);
		mScene.attachChild(titleText);
		
		Text levelMessage = new Text(0,140+titleText.getHeight(),font.get(50, Color.WHITE),"LEVEL ",this.getVertexBufferObjectManager());
		levelMessage.setX(MenuActivity.getWidth()/2f - levelMessage.getWidth()/2f - 50);
		mScene.attachChild(levelMessage);
		
		Text levelText = new Text(levelMessage.getX() + levelMessage.getWidth() + 20,140+titleText.getHeight(),font.get(50, Color.YELLOW),""+level,this.getVertexBufferObjectManager());

		mScene.attachChild(levelText);
		
		Text scoreMessage = new Text(0,300,font.get(50, Color.WHITE),"SCORE:",this.getVertexBufferObjectManager());
		scoreMessage.setX(MenuActivity.getWidth()/2f - scoreMessage.getWidth()/2f);
		mScene.attachChild(scoreMessage);
		
		Text scoreText = new Text(0,370,font.get(50, Color.YELLOW),""+score,this.getVertexBufferObjectManager());
		scoreText.setX(MenuActivity.getWidth()/2f - scoreText.getWidth()/2f);
		mScene.attachChild(scoreText);
		
		
		enterName = new Text(0,scoreText.getY() + 220,font.get(42, Color.WHITE),"XXXXXXXXXX",this.getVertexBufferObjectManager()) {
		    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
				if ((pSceneTouchEvent.isActionDown())||(pSceneTouchEvent.isActionMove()))
		        	this.setColor(1f,1f,0f);
		        else if(pSceneTouchEvent.isActionUp()) {
		        	if(hapticFeedback) {
			        	Vibrator v = (Vibrator) EndGameActivity.this.getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
			        	v.vibrate(100);
		        	}
		        	EndGameActivity.this.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							final AlertDialog.Builder alert = new AlertDialog.Builder(EndGameActivity.this);
				        	 
		                    alert.setTitle("");
		                    alert.setMessage("OMG BEST SCORE");

		                    final EditText editText = new EditText(EndGameActivity.this);
		                    editText.setTextSize(20f);
		                    
		                    InputFilter[] FilterArray = new InputFilter[1];
		                    FilterArray[0] = new InputFilter.LengthFilter(9);

		                    editText.setFilters(FilterArray);
		                    
		                    if(getText() != getResources().getString(R.string.end_entername))
			                    editText.setText(getText());
		                    else 
			                    editText.setText("");
		                    
		                    editText.setGravity(Gravity.CENTER_HORIZONTAL);
		                   
		                    editText.setHint(R.string.hinthighscore);
		                    
		                    alert.setView(editText);

		                    alert.setPositiveButton(getResources().getString(R.string.alert_dialog_ok), new DialogInterface.OnClickListener() {
	                            @Override
	                            public void onClick(DialogInterface dialog, int whichButton) {
	                            	scoreList.put(modifying, new Pair<Integer,String>(score,editText.getText().toString()));
	                            	onlineScoreList.put(onlineModifying, new Pair<Integer,String>(score,editText.getText().toString()));
	                            	setText(editText.getText().toString());
	                            	
	                    			int curScore = scoreList.get(modifying).first;
	                    			String curName = scoreList.get(modifying).second;
	                    			
	                    			String curScoreText = curScore+"";
	                    			String space = "";
	                    			for(int j=0;j<15-curScoreText.length()-curName.length();j++)
	                    				space = space + " ";
	                    			
	                    			if(modifiableText!=null)
	                    				modifiableText.setText(curName + space + curScoreText);
	                    			if(onlineModifiableText!=null)
	                    				onlineModifiableText.setText(curName + space + curScoreText);
	                            }
		                    });

		                    alert.setNegativeButton(getResources().getString(R.string.alert_dialog_cancel), new DialogInterface.OnClickListener() {
	                            @Override
	                            public void onClick(DialogInterface dialog, int whichButton) {
	                            }
		                    });

		                    final AlertDialog dialog = alert.create();
		                    dialog.setOnShowListener(new OnShowListener() {
	                            @Override
	                            public void onShow(DialogInterface dialog) {
                                    editText.requestFocus();
                                    final InputMethodManager imm = (InputMethodManager) EndGameActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
	                            }
		                    });
		                    dialog.show();
						}
		        		
		        	});
		        	
		        	this.setColor(1f,1f,1f);
		        }
		        return true;
		    }
		};
		enterName.setX(MenuActivity.getWidth()/2f - enterName.getWidth()/2f);
		mScene.attachChild(enterName);
		mScene.registerTouchArea(enterName);
		
		final int padding = 30;
		
		leftBorderEN = new Line(enterName.getX() - padding, enterName.getY() - padding, enterName.getX() - padding, enterName.getY() + enterName.getHeight() + padding, this.getVertexBufferObjectManager());
		rightBorderEN = new Line(MenuActivity.getWidth() - enterName.getX() + padding, enterName.getY() - padding, MenuActivity.getWidth() - enterName.getX() + padding, enterName.getY() + enterName.getHeight() + padding, this.getVertexBufferObjectManager());
		highBorderEN = new Line(enterName.getX() - padding, enterName.getY() - padding, MenuActivity.getWidth() - enterName.getX() + padding, enterName.getY() - padding, this.getVertexBufferObjectManager());
		lowBorderEN = new Line(enterName.getX() - padding,  enterName.getY() + enterName.getHeight() + padding, MenuActivity.getWidth() - enterName.getX() + padding, enterName.getY() + enterName.getHeight() + padding, this.getVertexBufferObjectManager());
		

		mScene.attachChild(leftBorderEN);
		mScene.attachChild(rightBorderEN);
		mScene.attachChild(highBorderEN);
		mScene.attachChild(lowBorderEN);
		
		if(!isBetter)
			activateInput(false);

		enterName.setText(lastName==""?getResources().getString(R.string.end_entername):lastName); 
		// Buttons
		
		returnButton = new Text(0,MenuActivity.getHeight() / 2f + 100,font.get(50, Color.WHITE),getResources().getString(R.string.end_tryagain),this.getVertexBufferObjectManager()) {
			@Override
		    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
				if ((pSceneTouchEvent.isActionDown())||(pSceneTouchEvent.isActionMove()))
		        	this.setColor(1f,1f,0f);
		        else if(pSceneTouchEvent.isActionUp()) {
		        	if(hapticFeedback) {
			        	Vibrator v = (Vibrator) EndGameActivity.this.getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
			        	v.vibrate(100);
		        	}
		        	
		        	Intent intent = new Intent(EndGameActivity.this, GameActivity.class);
		    		final SharedPreferences settings = getSharedPreferences(MenuActivity.PREFS_NAME, 0);
		        	intent.putExtra(MenuActivity.LEVEL, settings.getInt("startLevel", 1));
					startActivityForResult(intent, 1);
					finish();
		        	this.setColor(1f,1f,1f);
		        }
		        return true;
		    }
		};
		returnButton.setX(MenuActivity.getWidth()/2f - returnButton.getWidth()/2f);
		mScene.attachChild(returnButton);
		
		menuButton = new Text(0,MenuActivity.getHeight() / 2f + returnButton.getHeight() + 200,font.get(50, Color.WHITE),getResources().getString(R.string.end_menu),this.getVertexBufferObjectManager()) {
			@Override
		    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
				if ((pSceneTouchEvent.isActionDown())||(pSceneTouchEvent.isActionMove()))
		        	this.setColor(1f,1f,0f);
		        else if(pSceneTouchEvent.isActionUp()) {
		        	if(hapticFeedback) {
			        	Vibrator v = (Vibrator) EndGameActivity.this.getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
			        	v.vibrate(100);
		        	}
		        	
		        	Intent intent = new Intent(EndGameActivity.this, MenuActivity.class);
		        	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivityForResult(intent, 1);
					finish();
		        	this.setColor(1f,1f,1f);
		        }
		        return true;
		    }
		};
		menuButton.setX(MenuActivity.getWidth()/2f - menuButton.getWidth()/2f);
		mScene.attachChild(menuButton);

		mScene.registerTouchArea(menuButton);
		mScene.registerTouchArea(returnButton);
		
		// Scene HIGH SCORE local
		
		Text highText = new Text(0,70,font.get(60, Color.WHITE),getResources().getString(R.string.end_highscore),this.getVertexBufferObjectManager());
		highText.setX(MenuActivity.getWidth() + (MenuActivity.getWidth()/2f - highText.getWidth()/2f));
		mScene.attachChild(highText);

		Text highLocalText = new Text(0,150,font.get(48, Color.WHITE),getResources().getString(R.string.end_local),this.getVertexBufferObjectManager());
		highLocalText.setX(MenuActivity.getWidth() + (MenuActivity.getWidth()/2f - highLocalText.getWidth()/2f));
		mScene.attachChild(highLocalText);

		Sprite highScoreTable = new Sprite(MenuActivity.getWidth(),250,720,800,this.mHighScoreBGTextureRegion,this.getVertexBufferObjectManager());
		mScene.attachChild(highScoreTable);

		
		mScene.setOnSceneTouchListener(new IOnSceneTouchListener() {
			@Override
			public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
				menuButton.setColor(1f,1f,1f);
				returnButton.setColor(1f,1f,1f);
				if(enterName != null)
					enterName.setColor(1f,1f,1f);
				
				if(pSceneTouchEvent.isActionDown()) {
					xcoor = pSceneTouchEvent.getX();
				} else if(pSceneTouchEvent.isActionMove()) {
					camera.offsetCenter(xcoor - pSceneTouchEvent.getX(), 0);
					pagination.setX(pagination.getX()+xcoor - pSceneTouchEvent.getX());
				} else if(pSceneTouchEvent.isActionUp()) {
					int page;
					if(camera.getCenterX() < 0) {
						camera.setCenter(MenuActivity.getWidth()/2f, MenuActivity.getHeight()/2f);
						camera.offsetCenter(-MenuActivity.getWidth(), 0);
						pagination.setX(-MenuActivity.getWidth());
						page=1;
					} else if(camera.getCenterX() < MenuActivity.getWidth()) {
						camera.setCenter(MenuActivity.getWidth()/2f, MenuActivity.getHeight()/2f);
						pagination.setX(0);
						page=2;
					} else {
						camera.setCenter(MenuActivity.getWidth()/2f, MenuActivity.getHeight()/2f);
						camera.offsetCenter(MenuActivity.getWidth(), 0);
						pagination.setX(MenuActivity.getWidth());
						page=3;
					}
					pagination.setPage(page);
				}
				return false;
			}
		});
		
		for(int i=1;i<=scoreList.size();i++) {
			int curScore = scoreList.get(i).first;
			String curName = scoreList.get(i).second;
			String curScoreText = curScore+"";
			String space = "";
			for(int j=0;j<15-curScoreText.length()-curName.length();j++)
				space = space + " ";
			
			int color = (i==modifying) ? Color.YELLOW : Color.WHITE;
			Text curBann = new Text(MenuActivity.getWidth()+50,320+(i-1)*156,font.get(42,color),curName + space + curScoreText,this.getVertexBufferObjectManager());
			if(i==modifying)
				modifiableText = curBann;
			
			mScene.attachChild(curBann);
		}
		
		// Scene high score global
		
		Text globalHighText = new Text(0,70,font.get(60, Color.WHITE),getResources().getString(R.string.end_highscore),this.getVertexBufferObjectManager());
		globalHighText.setX(-MenuActivity.getWidth() + (MenuActivity.getWidth()/2f - globalHighText.getWidth()/2f));
		mScene.attachChild(globalHighText);

		Text globalHighTextSub = new Text(0,150,font.get(48, Color.WHITE),getResources().getString(R.string.end_global),this.getVertexBufferObjectManager());
		globalHighTextSub.setX(-MenuActivity.getWidth() + (MenuActivity.getWidth()/2f - globalHighTextSub.getWidth()/2f));
		mScene.attachChild(globalHighTextSub);
		
		Sprite globalHighScoreTable = new Sprite(-MenuActivity.getWidth(),250,720,800,this.mHighScoreBGTextureRegion,this.getVertexBufferObjectManager());
		mScene.attachChild(globalHighScoreTable);
		
		pagination = new Paginator(20, MenuActivity.getHeight()-20, MenuActivity.getWidth()-40, 15, 3, 2, this.getVertexBufferObjectManager());
		mScene.attachChild(pagination);
		scoreReafy=true;
		return mScene;
	}
	
	private void activateInput(boolean activate) {
		if(activate) {
			mScene.registerTouchArea(enterName);
			enterName.setVisible(true);

			leftBorderEN.setVisible(true);
			rightBorderEN.setVisible(true);
			highBorderEN.setVisible(true);
			lowBorderEN.setVisible(true);
		}else {
			mScene.unregisterTouchArea(enterName);
			enterName.setVisible(false);

			leftBorderEN.setVisible(false);
			rightBorderEN.setVisible(false);
			highBorderEN.setVisible(false);
			lowBorderEN.setVisible(false);
		}
	}
	
	public void onBackPressed() {
    	Intent intent = new Intent(EndGameActivity.this, MenuActivity.class);
    	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivityForResult(intent, 1);
		finish();
	}
	
	protected void getHighScores(int curScore) {
		SharedPreferences settings = getSharedPreferences(MenuActivity.PREFS_NAME, 0); 
		for(int i=1;i<=5;i++) {
			int i_score = settings.getInt("HighScore"+i, 0); 
			String i_name = settings.getString("HighScoreName"+i, ""); 
			Pair<Integer,String> pair = new Pair<Integer,String>(i_score,i_name);
			scoreList.put(i, pair);
		} 
		
		for(int i=5;i>0;i--) {
			if(scoreList.get(i).first < curScore) {
				if(i+1 <= 5) {
					scoreList.put(i+1,new Pair<Integer,String>(scoreList.get(i).first,scoreList.get(i).second));
				}
				modifying = i;
				isBetter=true;
				scoreList.put(i, new Pair<Integer,String>(curScore,lastName==""?"Unknown":lastName));
			}
		} 

	    scoreUpload=false;
	    asyncTaskHandler.post(new Runnable() {
			@Override
			public void run() {
		    	new AsyncScoreSubmit().execute((Void)null);
			}
	    });
	}
	
	public void drawElements() {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				for(int i=0;i<((onlineScoreList.size()>5) ? 5 : onlineScoreList.size()) ;i++) {
					int curScore = onlineScoreList.get(i).first;
					String curName = onlineScoreList.get(i).second;
					String curScoreText = String.valueOf(curScore);
					String space = "";
					for(int n=0;n<15-curScoreText.length()-curName.length();n++)
						space = space + " ";

					int color = (i==onlineModifying) ? Color.YELLOW : Color.WHITE;
					Text curBann = new Text(-MenuActivity.getWidth()+50,320+i*156,font.get(42, color),curName + space + curScoreText,EndGameActivity.this.getVertexBufferObjectManager());
					if(i==onlineModifying)
						onlineModifiableText = curBann;
					
					mScene.attachChild(curBann);
				}
				
				if(onlineBetter)
					activateInput(true);
			}
		});
	}
	
	protected void saveHighScores() {
		SharedPreferences settings = getSharedPreferences(MenuActivity.PREFS_NAME, 0); 
	    SharedPreferences.Editor editor = settings.edit();
	    
		for(int i=1;i<=5;i++) {
			int i_score = scoreList.get(i).first; 
			String i_name = scoreList.get(i).second; 

			editor.putInt("HighScore"+i, i_score);
			editor.putString("HighScoreName"+i, i_name);
		}
	    
	    editor.commit();
	    
	    if(onlineBetter) {
	    	scoreUpload=true;
	    	asyncTaskHandler.post(new Runnable() {
	    		@Override
				public void run() {
		    		new AsyncScoreSubmit().execute((Void)null);
				}
	    	});
	    }
	}
	
	private class AsyncScoreSubmit extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			
			ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
			NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			NetworkInfo mMobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

			boolean wifiAvailable   = (mWifi == null) ? false:mWifi.isConnected();
			boolean mobileAvailable = (mMobile == null) ? false:mMobile.isConnected();
			
			
			if(mInternetUsage==OptionsActivity.IU_NEVER) {
				onlineScoreList.put(0,new Pair<Integer,String>((Integer)0, getResources().getString(R.string.end_err_notenabled))); 
	            new Timer().schedule(new TimerTask() {
					@Override
					public void run() {
						EndGameActivity.this.drawElements();
					}
	            },2000);
				return null;
			} else if(mInternetUsage==OptionsActivity.IU_WIFI_ONLY) {
			    if (!wifiAvailable) {
			    	onlineScoreList.put(0,new Pair<Integer,String>((Integer)0, getResources().getString(R.string.end_err_nowifi))); 
			    	 new Timer().schedule(new TimerTask() {
						@Override
						public void run() {
							EndGameActivity.this.drawElements();
						}
		            },2000);
					return null;
			    }
			}
			
			if((!mobileAvailable) && (!wifiAvailable)) {
				onlineScoreList.put(0,new Pair<Integer,String>((Integer)0, getResources().getString(R.string.end_err_nonetwork))); 
				 new Timer().schedule(new TimerTask() {
					@Override
					public void run() {
						EndGameActivity.this.drawElements();
					}
	            },2000);
				return null;
			}
			
			if(scoreUpload) { // Upload the score
					try {
					HttpPost postMethod = new HttpPost("http://lortexgames.alwaysdata.net/submit.php");
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
					nameValuePairs.add(new BasicNameValuePair("name", enterName.getText().toString()));
					nameValuePairs.add(new BasicNameValuePair("score", String.valueOf(score)));
					nameValuePairs.add(new BasicNameValuePair("level", String.valueOf(level)));

					String keywords = "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz";
					
					MessageDigest md;
					md = MessageDigest.getInstance("SHA-256");
			        md.update((String.valueOf(score) + "hello" + String.valueOf(level) + "what are your doing ?" + keywords.charAt(19) + keywords.charAt(17) + keywords.charAt(14) + keywords.charAt(11) + keywords.charAt(11) + keywords.charAt(5) + keywords.charAt(0) + keywords.charAt(2) + keywords.charAt(4)).getBytes());
					
			        
			        byte byteData[] = md.digest();
			 
			        //convert the byte to hex format method 1
			        StringBuffer sb = new StringBuffer();
			        for (int i = 0; i < byteData.length; i++) {
			         sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
			        }
			        
					String hash = sb.toString();
					/*Debug.i("Data:"+String.valueOf(onlineScoreList.get(onlineModifying).first) + "hello" + String.valueOf(level) + "what are your doing ?" + mUuid + keywords.charAt(19) + keywords.charAt(17) + keywords.charAt(14) + keywords.charAt(11) + keywords.charAt(11) + keywords.charAt(5) + keywords.charAt(0) + keywords.charAt(2) + keywords.charAt(4));
			        Debug.i("Hash:"+hash);*/
			    
					
					nameValuePairs.add(new BasicNameValuePair("hash", hash));
					
					postMethod.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					client.execute(postMethod,responseHandler); 
					
					if(postMethod.getEntity() != null ) {
						postMethod.getEntity().consumeContent();
				     }
					
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					} catch (ClientProtocolException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (NoSuchAlgorithmException e1) {
						e1.printStackTrace();
				}
			} else { //Download the scoreboard
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
			        }
			        
					for(int i=onlineScoreList.size()-1;i>=0;i--) {
						if(onlineScoreList.get(i).first < score) {
							if(i+1 <= 5) {
								onlineScoreList.put(i+1,new Pair<Integer,String>(onlineScoreList.get(i).first,onlineScoreList.get(i).second));
							}
							onlineModifying = i;
							onlineBetter=true;
							onlineScoreList.put(i, new Pair<Integer,String>(score,lastName==""?"Unknown":lastName));
						}
					} 
					
					if(postMethod.getEntity() != null ) {
						postMethod.getEntity().consumeContent();
				     }
					
		            EndGameActivity.this.drawElements();
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return null;
		}
	}
}