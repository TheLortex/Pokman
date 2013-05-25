package com.lortexgames.pokman.activities;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
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
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.opengl.view.RenderSurfaceView;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.adt.io.in.IInputStreamOpener;
import org.andengine.util.debug.Debug;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.google.example.games.basegameutils.GameHelper;
import com.lortexgames.pokman.FontManager;
import com.lortexgames.pokman.R;
import com.purplebrain.giftiz.sdk.GiftizSDK;
import com.purplebrain.giftiz.sdk.GiftizSDK.Inner.ButtonNeedsUpdateDelegate;


import android.app.AlertDialog;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.provider.Settings;
import android.provider.Settings.System;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MenuActivity extends SimpleBaseGameActivity  /*implements ButtonNeedsUpdateDelegate, GameHelper.GameHelperListener*/{
	private Camera camera;

	static int mScreenWidth;
	static int mScreenHeight=1280;
	
	private ITextureRegion mTitleTexture;

	private Sprite mTitle;
	private Text highscores;
	private Text options;
	private Text play;
	private Text credit;
	private Text feedbackText;
	private Text howto;
	private Text modtText;
	

	private TextureRegion mGiftizLogoTextureRegion;
	private TextureRegion mGiftizLogoWarningTextureRegion;
	private TextureRegion mGiftizLogoBadgeTextureRegion;
	private Sprite giftizButton;
	protected boolean buttonClicked;
	private TextureRegion mGiftizNullTextureRegion;
	private FontManager font;
	
	private int mInternetUsage;
	DefaultHttpClient client = new DefaultHttpClient();
	ResponseHandler<String> responseHandler = new BasicResponseHandler();
	private Handler asyncTaskHandler;
	protected float timeElapsed;
	protected boolean updateModt;
	private Text multiplayer;
	//private GameHelper mGameHelper;
	
    public static final String PREFS_NAME = "PokmanPrefs";
    public final static String LEVEL = "com.lortexgames.pokman.LEVEL";
	
    public static boolean GIFTIZ_ENABLED=false;
	
	@Override
	public EngineOptions onCreateEngineOptions() {
		DisplayMetrics metrics = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		
		mScreenWidth = mScreenHeight*metrics.widthPixels/metrics.heightPixels;
		
		camera = new Camera(0, 0, getWidth(), getHeight());
		return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(getWidth(),getHeight()), camera);
	}

	@Override
	protected void onCreateResources() {
		try {
			ITexture titleTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {@Override public InputStream open() throws IOException { return getAssets().open("gfx/title.png");} });
			titleTexture.load();
			this.mTitleTexture = TextureRegionFactory.extractFromTexture(titleTexture);
			
			if(GIFTIZ_ENABLED) {
				ITexture giftizLogoTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {@Override public InputStream open() throws IOException { return getAssets().open("gfx/giftiz_logo.png");} });
				giftizLogoTexture.load();
				this.mGiftizLogoTextureRegion = TextureRegionFactory.extractFromTexture(giftizLogoTexture);
				
				ITexture giftizLogoBadgeTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {@Override public InputStream open() throws IOException { return getAssets().open("gfx/giftiz_logo_badge.png");} });
				giftizLogoBadgeTexture.load();
				this.mGiftizLogoBadgeTextureRegion = TextureRegionFactory.extractFromTexture(giftizLogoBadgeTexture);
				
				ITexture giftizLogoWarningTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {@Override public InputStream open() throws IOException { return getAssets().open("gfx/giftiz_logo_warning.png");} });
				giftizLogoWarningTexture.load();
				this.mGiftizLogoWarningTextureRegion = TextureRegionFactory.extractFromTexture(giftizLogoWarningTexture);
				
				ITexture giftizNullTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {@Override public InputStream open() throws IOException { return getAssets().open("gfx/giftiz_null.png");} });
				giftizNullTexture.load();
				this.mGiftizNullTextureRegion = TextureRegionFactory.extractFromTexture(giftizNullTexture);
			}
			
			
			font = new FontManager(this);

			font.load(60);
			font.load(28);
			font.load(26);
			font.load(53);
		}catch (IOException e) {
			Debug.e(e);
		}
		/*
		mGameHelper = new GameHelper(this);
		mGameHelper.setup(this);*/
	}

	@Override
	protected Scene onCreateScene() {
		final Scene scene = new Scene();
		scene.setBackground(new Background(0,0,0));
		
		final boolean hapticFeedback = System.getInt(this.getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED, 0) != 0;
		
		mTitle = new Sprite((getWidth()/2)-(mTitleTexture.getWidth()/2), (float) (getHeight() * 0.1), mTitleTexture, getVertexBufferObjectManager());
		scene.attachChild(mTitle);
		
		final SharedPreferences settings = getSharedPreferences(MenuActivity.PREFS_NAME, 0);
        final int maxLevel = settings.getInt("maxLevel", 1);
        mInternetUsage = settings.getInt("internetUsage", OptionsActivity.IU_WIFI_ONLY);
        modtText = new Text(getWidth(), 45, font.get(26), String.format("%500s", ""), this.getVertexBufferObjectManager());
        scene.attachChild(modtText); //AsyncGetModt
        updateModt=false;
		asyncTaskHandler = new Handler(Looper.getMainLooper());
		asyncTaskHandler.post(new Runnable() {
			@Override
			public void run() {
		    	new AsyncGetModt().execute((Void)null);
			}
	    });

       
        
        
		play = new Text((float) (getWidth()*0.3), (float) (getHeight() * 0.3), font.get(60, Color.WHITE), getResources().getString(R.string.btn_startgame), this.getVertexBufferObjectManager()) {
		    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
				if ((pSceneTouchEvent.isActionDown())||(pSceneTouchEvent.isActionMove()))
		        	this.setColor(1f,1f,0f);
		        else if(pSceneTouchEvent.isActionUp()) {
		        	if(hapticFeedback) {
			        	Vibrator v = (Vibrator) MenuActivity.this.getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
			        	v.vibrate(100);
		        	}
		        	if(maxLevel > 1) {
			        	MenuActivity.this.runOnUiThread(new Runnable() {
	
							@Override
							public void run() {
								final AlertDialog.Builder alert = new AlertDialog.Builder(MenuActivity.this);
								
			                    alert.setTitle("");
			                    alert.setMessage(getResources().getString(R.string.choose_level_dialog));
	
			                    final LinearLayout layout = new LinearLayout(MenuActivity.this);
			                    
			                    final TextView label = new TextView(MenuActivity.this);
			                    label.setHeight(30);
			                    label.setText("1");
			                    
			                    
			                    
			                    final SeekBar seekbar = new SeekBar(MenuActivity.this);
			                    
			                    seekbar.setMax(maxLevel-1);
			                    seekbar.setPadding(50, 20, 50, 20);
			                    
			                    seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
									@Override
									public void onProgressChanged(SeekBar arg0,int arg1, boolean arg2) {
										int value = arg0.getProgress();
										label.setText(""+(value+1));
									}
	
									@Override
									public void onStartTrackingTouch(SeekBar arg0) {}
	
									@Override
									public void onStopTrackingTouch(SeekBar arg0) {;}
			                    });
			                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
			                    layout.setLayoutParams(params);
			                    LinearLayout.LayoutParams paramsSeek = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
			                    paramsSeek.weight=1;
			                    layout.addView(seekbar,paramsSeek);
			                    LinearLayout.LayoutParams paramsLabel = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
			                    paramsLabel.weight=9;
			                    paramsLabel.setMargins(0, 40, 0, 0);
			                    layout.addView(label,paramsLabel);
			                    layout.setWeightSum(10);
			                    alert.setView(layout);
	
	
			                    alert.setPositiveButton(getResources().getString(R.string.alert_dialog_ok), new DialogInterface.OnClickListener() {
		                            @Override
		                            public void onClick(DialogInterface dialog, int whichButton) {
		                            	Intent intent = new Intent(MenuActivity.this, GameActivity.class);
		                            	intent.putExtra(LEVEL, seekbar.getProgress()+1);
		                            	intent.putExtra(GameActivity.NVIES, 3);
		                            	
		            		        	Editor edit = settings.edit();
		            		        	edit.putInt("startLevel", seekbar.getProgress()+1);
		            		        	edit.commit();
		            					startActivityForResult(intent, 1);
		                            }
			                    });
	
			                    alert.setNegativeButton(getResources().getString(R.string.alert_dialog_cancel), new DialogInterface.OnClickListener() {
		                            @Override
		                            public void onClick(DialogInterface dialog, int whichButton) {
		                            }
			                    });
	
			                    final AlertDialog dialog = alert.create();
			                    dialog.show();
							}
			        		
			        	});
		        	} else {
		        		Intent intent = new Intent(MenuActivity.this, GameActivity.class);
			        	intent.putExtra(LEVEL, 1);
						startActivityForResult(intent, 1);
		        	}
		        	
		        	this.setColor(1f,1f,1f);
		        }
		        return true;
		    }
		};
		
		
		
		play.setX((float) (0.5*getWidth() - play.getWidth()/2.0));
		
		multiplayer = new Text(0f, (float) (getHeight() * 0.4), font.get(60), "MULTI", this.getVertexBufferObjectManager()){
			@Override
		    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
				if ((pSceneTouchEvent.isActionDown())||(pSceneTouchEvent.isActionMove()))
		        	this.setColor(1f,1f,0f);
		        else if(pSceneTouchEvent.isActionUp()) {
		        	if(hapticFeedback) {
			        	Vibrator v = (Vibrator) MenuActivity.this.getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
			        	v.vibrate(100);
		        	} 
		        	Intent intent = new Intent(MenuActivity.this, LobbyActivity2.class);
					startActivityForResult(intent, 1);
		        	this.setColor(1f,1f,1f);
		        }
		        return true;
		    }
		};
		multiplayer.setX(getWidth()/2f - multiplayer.getWidth()/2f);
		
		 howto = new Text((float) (getWidth()*0.3), (float) (getHeight() * 0.5), font.get(53), getResources().getString(R.string.btn_tuto), this.getVertexBufferObjectManager()){
				@Override
			    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
					if ((pSceneTouchEvent.isActionDown())||(pSceneTouchEvent.isActionMove()))
			        	this.setColor(1f,1f,0f);
			        else if(pSceneTouchEvent.isActionUp()) {
			        	if(hapticFeedback) {
				        	Vibrator v = (Vibrator) MenuActivity.this.getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
				        	v.vibrate(100);
			        	}
			        	
			        	Intent intent = new Intent(MenuActivity.this, TutorialActivity.class);
						startActivityForResult(intent, 1);
			        	this.setColor(1f,1f,1f);
			        }
			        return true;
			    }
			};
			
			howto.setX((float) (0.5*getWidth() - howto.getWidth()/2.0));
			scene.attachChild(howto);
			scene.registerTouchArea(howto);

		highscores = new Text(0f, (float) (getHeight() * 0.6), font.get(53), getResources().getString(R.string.btn_highscores), this.getVertexBufferObjectManager()){
			@Override
		    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
				if ((pSceneTouchEvent.isActionDown())||(pSceneTouchEvent.isActionMove()))
		        	this.setColor(1f,1f,0f);
		        else if(pSceneTouchEvent.isActionUp()) {
		        	if(hapticFeedback) {
			        	Vibrator v = (Vibrator) MenuActivity.this.getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
			        	v.vibrate(100);
		        	} 
		        	Intent intent = new Intent(MenuActivity.this, HighscoresActivity.class);
					startActivityForResult(intent, 1);
		        	this.setColor(1f,1f,1f);
		        }
		        return true;
		    }
		};
		highscores.setX(getWidth()/2f - highscores.getWidth()/2f);
		
		options = new Text((float) (getWidth()*0.3), (float) (getHeight() * 0.7), font.get(53), getResources().getString(R.string.btn_settings), this.getVertexBufferObjectManager()){
			@Override
		    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
				if ((pSceneTouchEvent.isActionDown())||(pSceneTouchEvent.isActionMove()))
		        	this.setColor(1f,1f,0f);
		        else if(pSceneTouchEvent.isActionUp()) {
		        	if(hapticFeedback) {
			        	Vibrator v = (Vibrator) MenuActivity.this.getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
			        	v.vibrate(100);
		        	}
		        	
		        	Intent intent = new Intent(MenuActivity.this, OptionsActivity.class);
					startActivityForResult(intent, 1);
		        	this.setColor(1f,1f,1f);
		        }
		        return true;
		    }
		};
		
		if(GIFTIZ_ENABLED)
			options.setX((float) (getWidth()/2f - options.getWidth()/2f - 70));
		else
			options.setX((float) (getWidth()/2f - options.getWidth()/2f));
			
		credit = new Text(0, (float) (getHeight() * 0.85), font.get(28), getResources().getString(R.string.btn_credit), this.getVertexBufferObjectManager()) {
			@Override
		    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
				if ((pSceneTouchEvent.isActionDown())||(pSceneTouchEvent.isActionMove()))
		        	this.setColor(1f,1f,0f);
		        else if(pSceneTouchEvent.isActionUp()) {
		        	if(hapticFeedback) {
			        	Vibrator v = (Vibrator) MenuActivity.this.getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
			        	v.vibrate(100);
		        	}
		        	
					Intent intent = new Intent(MenuActivity.this, CreditActivity.class);
		        	intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
					startActivity(intent);
		        	this.setColor(1f,1f,1f);
		        }
		        return true;
		    }
		}; 
		credit.setX(getWidth()/2f-credit.getWidth()/2f);
		
		Text pacVer = new Text(0, (float) (getHeight() * 0.925),font.get(28), getResources().getString(R.string.btn_version), this.getVertexBufferObjectManager());
		pacVer.setX(getWidth() - pacVer.getWidth() - 10);
		
		feedbackText = new Text((float) (getWidth()*0.05), (float) (getHeight() * 0.925), font.get(28), getResources().getString(R.string.btn_feedback), this.getVertexBufferObjectManager()) {
			@Override
		    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
				if ((pSceneTouchEvent.isActionDown())||(pSceneTouchEvent.isActionMove()))
		        	this.setColor(1f,1f,0f);
		        else if(pSceneTouchEvent.isActionUp()) {
		        	if(hapticFeedback) {
			        	Vibrator v = (Vibrator) MenuActivity.this.getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
			        	v.vibrate(100);
		        	}
		        	
		        	Intent intent = new Intent(Intent.ACTION_SEND);
	                intent.setType("plain/text");
	                intent.putExtra(Intent.EXTRA_SUBJECT, "Pokman feedback");
	                intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "lucas.pluvinage@gmail.com" });
	                startActivity(Intent.createChooser(intent, ""));
	                
		        	this.setColor(1f,1f,1f);
		        }
		        return true;
		    }
		};

		if(GIFTIZ_ENABLED) {
		//	GiftizSDK.Inner.setButtonNeedsUpdateDelegate(this);
			TextureRegion giftizSelectedTexture=giftizTextureRegion();		
			
			buttonClicked=false;
			
			giftizButton = new Sprite(0,0,giftizSelectedTexture,this.getVertexBufferObjectManager()) {
				@Override
			    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
					if ((pSceneTouchEvent.isActionDown())||(pSceneTouchEvent.isActionMove())) {
						if(buttonClicked != true) {
							giftizButton.setY(giftizButton.getY()-3);
							giftizButton.setX(giftizButton.getX()-3);
							buttonClicked = true;
						}
					}
			        else if(pSceneTouchEvent.isActionUp()) {
			        	if(hapticFeedback) {
				        	Vibrator v = (Vibrator) MenuActivity.this.getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
				        	v.vibrate(100);
			        	}
			        	GiftizSDK.Inner.buttonClicked(MenuActivity.this);
			        	this.setColor(1f,1f,1f);
						giftizButton.setY(giftizButton.getY()+3);
						giftizButton.setX(giftizButton.getX()+3);
						buttonClicked = false;
			        }
			        return true;
			    }
			};
			giftizButton.setScale(1.5f);
			giftizButton.setY(options.getY()-10);
			giftizButton.setX(options.getX()+options.getWidth()+100);
			
			scene.attachChild(giftizButton);
			scene.registerTouchArea(giftizButton);
		}
		
		scene.attachChild(play);
		scene.attachChild(options);
		scene.attachChild(highscores);
		scene.attachChild(credit);
		scene.attachChild(pacVer);
		scene.attachChild(feedbackText);
		scene.attachChild(multiplayer);

		scene.registerTouchArea(play);
		scene.registerTouchArea(options);
		scene.registerTouchArea(highscores);
		scene.registerTouchArea(credit);
		scene.registerTouchArea(feedbackText);
		scene.registerTouchArea(multiplayer);
		
		scene.setOnSceneTouchListener(new IOnSceneTouchListener() {
			@Override
			public boolean onSceneTouchEvent(Scene pScene,TouchEvent pSceneTouchEvent) {
				highscores.setColor(1f,1f,1f);
				options.setColor(1f,1f,1f);
				play.setColor(1f,1f,1f);
				credit.setColor(1f,1f,1f);
				feedbackText.setColor(1f,1f,1f);
				howto.setColor(1f,1f,1f);
				multiplayer.setColor(1f,1f,1f);

				if(GIFTIZ_ENABLED) {
					if(buttonClicked != false) {
						giftizButton.setY(giftizButton.getY()+3);
						giftizButton.setX(giftizButton.getX()+3);
						buttonClicked = false;
					}
				}
				return false;
			}
		});
		
		scene.registerUpdateHandler(new IUpdateHandler() {
			@Override
			public void onUpdate(float pSecondsElapsed) {
				if(updateModt) {
					if(modtText.getX() < - modtText.getWidth() - getWidth()) {
						modtText.setX(getWidth());
					} else {
						modtText.setX(modtText.getX()-2);
					}
				}
			}

			@Override
			public void reset() {}
		});
		
		return scene;
	}

	private TextureRegion giftizTextureRegion() {
		TextureRegion giftizSelectedTexture=null;
		switch(GiftizSDK.Inner.getButtonStatus(this)) {
		case ButtonBadge:
			giftizSelectedTexture=mGiftizLogoBadgeTextureRegion;
			break;
		case ButtonInvisible:
			giftizSelectedTexture=mGiftizNullTextureRegion;
			break;
		case ButtonNaked:
			giftizSelectedTexture=mGiftizLogoTextureRegion;
			break;
		case ButtonWarning:
			giftizSelectedTexture=mGiftizLogoWarningTextureRegion;
			break;
		default:
			giftizSelectedTexture=mGiftizNullTextureRegion;
			break;
		}
		return giftizSelectedTexture;
	}

//	@Override
	public void buttonNeedsUpdate() {
		giftizButton.setTextureRegion(giftizTextureRegion());
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if(GIFTIZ_ENABLED)
			GiftizSDK.onPauseMainActivity(this); 
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if(GIFTIZ_ENABLED)
			GiftizSDK.onResumeMainActivity(this); 
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onDestroy() {
		SharedPreferences settings = this.getSharedPreferences(MenuActivity.PREFS_NAME, 0);
		if(settings.getInt("bluetoothStatus", 0) == 2) {
			NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			Notification notification = new Notification(R.drawable.ic_launcher, "Pokman", java.lang.System.currentTimeMillis());
			Intent intentNotification = new Intent(this,StopBluetooth.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(MenuActivity.this, 0, intentNotification, 0);
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			notification.setLatestEventInfo(MenuActivity.this, "Oh ! I think you forgot something.", "Tap to stop bluetooth.", pendingIntent);
			notificationManager.notify(1, notification);

			Editor edit = settings.edit();
			edit.putInt("bluetoothStatus", 0);
			edit.commit();
		}
		super.onDestroy();

	}
	
	public void onBackPressed() {
		finish();
	}
	
	@Override
	protected void onSetContentView() {
	    //Creating the parent frame layout:
	    final FrameLayout frameLayout = new FrameLayout(this);
	    //Creating its layout params, making it fill the screen.
	    @SuppressWarnings("deprecation")
		final FrameLayout.LayoutParams frameLayoutLayoutParams =
	            new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,
	                    FrameLayout.LayoutParams.FILL_PARENT);

	    //Creating the banner view.
	  /*  View banner = new View(this);
	    this.getResources().getLayout(R.layout.gsign);
	    setContentView(R.layout.gsign);*/

	    LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View bannerView = vi.inflate(R.layout.gsign, null); //THIS IS MY CUSTOM VIEW
        bannerView.bringToFront();
 

	    //Creating the banner layout params. With this params, the ad will be placed in the top of the screen, middle horizontally.
	    final FrameLayout.LayoutParams bannerViewLayoutParams =
	            new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
	                    FrameLayout.LayoutParams.WRAP_CONTENT,
	                    Gravity.TOP | Gravity.CENTER_HORIZONTAL);

	    //Creating AndEngine's view.
	    this.mRenderSurfaceView = new RenderSurfaceView(this);

	    mRenderSurfaceView.setRenderer(mEngine, this);

	    //createSurfaceViewLayoutParams is an AndEngine method for creating the params for its view.
	    final android.widget.FrameLayout.LayoutParams surfaceViewLayoutParams =
	            new FrameLayout.LayoutParams(super.createSurfaceViewLayoutParams());

	    //Adding the views to the frame layout.
	    frameLayout.addView(this.mRenderSurfaceView, surfaceViewLayoutParams);
	    frameLayout.addView(bannerView, bannerViewLayoutParams);

	    //Setting content view
	    this.setContentView(frameLayout, frameLayoutLayoutParams);

        findViewById(R.id.sign_in_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
			//	mGameHelper.beginUserInitiatedSignIn();
			}
        });
        findViewById(R.id.sign_out_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
			//	mGameHelper.signOut();

		        findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
		        findViewById(R.id.sign_out_button).setVisibility(View.GONE);
			}
        });  
	}
	
	private class AsyncGetModt extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			
			ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
			NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			NetworkInfo mMobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

			boolean wifiAvailable   = (mWifi == null) ? false:mWifi.isConnected();
			boolean mobileAvailable = (mMobile == null) ? false:mMobile.isConnected();
			
			if(mInternetUsage==OptionsActivity.IU_NEVER) 
				return null;
			
			if(mInternetUsage==OptionsActivity.IU_WIFI_ONLY)
			    if (!wifiAvailable) 
			    	return null;
			
			if((!wifiAvailable) && (!mobileAvailable))
				return null;
			
			
			
			

			HttpPost postMethod = new HttpPost("http://lortexgames.alwaysdata.net/modt.php");
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("locale", Locale.getDefault().getLanguage()));
			try {
				postMethod.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				String response = client.execute(postMethod, responseHandler);
				modtText.setText(response);
				
				if(postMethod.getEntity() != null ) {
					postMethod.getEntity().consumeContent();
			    }
				updateModt=true;
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	public static int getWidth() {
		return mScreenWidth;
	}
	
	public static int getHeight() {
		return mScreenHeight;
	}
	
/*	@Override
	public void onSignInFailed() {
		Debug.w("Sign in failed");
	    findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
	    findViewById(R.id.sign_out_button).setVisibility(View.GONE);
	}

	@Override
	public void onSignInSucceeded() {
		Debug.w("Sign in succeeded");
	    findViewById(R.id.sign_in_button).setVisibility(View.GONE);
	    findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
	}*/

}
