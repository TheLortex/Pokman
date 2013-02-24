package com.lortexgames.pokman;

import java.io.IOException;
import java.io.InputStream;

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

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
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
	private boolean isBetter=false;
	private int modifying=-1;

	private Text enterName=null;
	private Text modifiableText=null;

	private int level;

	@Override
	public EngineOptions onCreateEngineOptions() {
		Intent intent = getIntent();
		win = intent.getBooleanExtra(GameActivity.WIN, false);
		score = intent.getIntExtra(GameActivity.SCORE,0);
		level = intent.getIntExtra(MenuActivity.LEVEL,1);
		
		font = new FontManager(this);
		
		SharedPreferences settings = getSharedPreferences(MenuActivity.PREFS_NAME, 0);
		final float percentageGfx = settings.getInt("sfx", 100)/100f;
		
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

		camera = new Camera(0, 0, MenuActivity.SCREENWIDTH, MenuActivity.SCREENHEIGHT );
		return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(MenuActivity.SCREENWIDTH,MenuActivity.SCREENHEIGHT), camera);
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
		font.load(50, Color.YELLOW);
	}

	@Override
	protected void onStop() {
		saveHighScores();
		super.onStop();
	}
	
	@Override
	protected Scene onCreateScene() {
		Scene scene = new Scene();
		scene.setBackground(new Background(0f,0f,0f));
		
		final boolean hapticFeedback = System.getInt(this.getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED, 0) != 0;
		
		String message = win ? "WIN" : "GAME OVER";
		Text titleText = new Text(0,70,font.get(60, Color.WHITE),message,this.getVertexBufferObjectManager());
		titleText.setX(MenuActivity.SCREENWIDTH/2f - titleText.getWidth()/2f);
		scene.attachChild(titleText);
		
		Text levelMessage = new Text(0,140+titleText.getHeight(),font.get(50, Color.WHITE),"LEVEL ",this.getVertexBufferObjectManager());
		levelMessage.setX(MenuActivity.SCREENWIDTH/2f - levelMessage.getWidth()/2f - 50);
		scene.attachChild(levelMessage);
		
		Text levelText = new Text(levelMessage.getX() + levelMessage.getWidth() + 20,140+titleText.getHeight(),font.get(50, Color.YELLOW),""+level,this.getVertexBufferObjectManager());

		scene.attachChild(levelText);
		
		Text scoreMessage = new Text(0,300,font.get(50, Color.WHITE),"SCORE:",this.getVertexBufferObjectManager());
		scoreMessage.setX(MenuActivity.SCREENWIDTH/2f - scoreMessage.getWidth()/2f);
		scene.attachChild(scoreMessage);
		
		Text scoreText = new Text(0,370,font.get(50, Color.YELLOW),""+score,this.getVertexBufferObjectManager());
		scoreText.setX(MenuActivity.SCREENWIDTH/2f - scoreText.getWidth()/2f);
		scene.attachChild(scoreText);

		scoreList = new SparseArray<Pair<Integer,String>>();
		getHighScores(score);
		
		if(isBetter) {
			enterName = new Text(0,scoreText.getY() + 220,font.get(42, Color.WHITE),"Enter name",this.getVertexBufferObjectManager()) {
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
			                    
			                    if(getText() != "Enter name")
				                    editText.setText(getText());
			                    else 
				                    editText.setText("");
			                    
			                    editText.setGravity(Gravity.CENTER_HORIZONTAL);
			                   
			                    editText.setHint(R.string.hinthighscore);
			                    
			                    alert.setView(editText);
	
			                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		                            @Override
		                            public void onClick(DialogInterface dialog, int whichButton) {
		                            	scoreList.put(modifying, new Pair<Integer,String>(score,editText.getText().toString()));
		                            	setText(editText.getText().toString());
		                            	
		                    			int curScore = scoreList.get(modifying).first;
		                    			String curName = scoreList.get(modifying).second;
		                    			
		                    			String curScoreText = curScore+"";
		                    			String space = "";
		                    			for(int j=0;j<15-curScoreText.length()-curName.length();j++)
		                    				space = space + " ";
		                    			
		                    			modifiableText.setText(curName + space + curScoreText);
		                    			
		                            }
			                    });
	
			                    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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
			enterName.setX(MenuActivity.SCREENWIDTH/2f - enterName.getWidth()/2f);
			scene.attachChild(enterName);
			scene.registerTouchArea(enterName);
			
			final int padding = 30;
			
			Line leftBorderEN = new Line(enterName.getX() - padding, enterName.getY() - padding, enterName.getX() - padding, enterName.getY() + enterName.getHeight() + padding, this.getVertexBufferObjectManager());
			Line rightBorderEN = new Line(MenuActivity.SCREENWIDTH - enterName.getX() + padding, enterName.getY() - padding, MenuActivity.SCREENWIDTH - enterName.getX() + padding, enterName.getY() + enterName.getHeight() + padding, this.getVertexBufferObjectManager());
			Line highBorderEN = new Line(enterName.getX() - padding, enterName.getY() - padding, MenuActivity.SCREENWIDTH - enterName.getX() + padding, enterName.getY() - padding, this.getVertexBufferObjectManager());
			Line lowBorderEN = new Line(enterName.getX() - padding,  enterName.getY() + enterName.getHeight() + padding, MenuActivity.SCREENWIDTH - enterName.getX() + padding, enterName.getY() + enterName.getHeight() + padding, this.getVertexBufferObjectManager());
			
	
			scene.attachChild(leftBorderEN);
			scene.attachChild(rightBorderEN);
			scene.attachChild(highBorderEN);
			scene.attachChild(lowBorderEN);
		}
		
		// Buttons
		
		returnButton = new Text(0,MenuActivity.SCREENHEIGHT / 2f + 100,font.get(50, Color.WHITE),"TRY AGAIN",this.getVertexBufferObjectManager()) {
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
		returnButton.setX(MenuActivity.SCREENWIDTH/2f - returnButton.getWidth()/2f);
		scene.attachChild(returnButton);
		
		menuButton = new Text(0,MenuActivity.SCREENHEIGHT / 2f + returnButton.getHeight() + 200,font.get(50, Color.WHITE),"MENU",this.getVertexBufferObjectManager()) {
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
		menuButton.setX(MenuActivity.SCREENWIDTH/2f - menuButton.getWidth()/2f);
		scene.attachChild(menuButton);

		scene.registerTouchArea(menuButton);
		scene.registerTouchArea(returnButton);
		

		
		// Scene HIGH SCORE
		
		Text highText = new Text(0,70,font.get(60, Color.WHITE),"HIGH SCORE",this.getVertexBufferObjectManager());
		highText.setX(MenuActivity.SCREENWIDTH + (MenuActivity.SCREENWIDTH/2f - highText.getWidth()/2f));
		scene.attachChild(highText);

		Sprite highScoreTable = new Sprite(MenuActivity.SCREENWIDTH,170,720,800,this.mHighScoreBGTextureRegion,this.getVertexBufferObjectManager());
		scene.attachChild(highScoreTable);

		
		scene.setOnSceneTouchListener(new IOnSceneTouchListener() {
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
				} else if(pSceneTouchEvent.isActionUp()) {
					if(camera.getCenterX() < MenuActivity.SCREENWIDTH) {
						camera.setCenter(MenuActivity.SCREENWIDTH/2f, MenuActivity.SCREENHEIGHT/2f);
					} else {
						camera.setCenter(MenuActivity.SCREENWIDTH/2f, MenuActivity.SCREENHEIGHT/2f);
						camera.offsetCenter(MenuActivity.SCREENWIDTH, 0);
					}
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
			
			Text curBann = new Text(MenuActivity.SCREENWIDTH+50,240+(i-1)*156,font.get(42, Color.WHITE),curName + space + curScoreText,this.getVertexBufferObjectManager());
			if(i==modifying)
				modifiableText = curBann;
			
			scene.attachChild(curBann);
		}
		
		
		return scene;
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
				scoreList.put(i, new Pair<Integer,String>(curScore,""));
			}
		} 
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
	}

}
