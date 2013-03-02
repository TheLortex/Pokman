package com.lortexgames.pokman;

import java.io.IOException;
import java.io.InputStream;
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

import com.purplebrain.giftiz.sdk.GiftizSDK;
import com.purplebrain.giftiz.sdk.GiftizSDK.Inner.ButtonNeedsUpdateDelegate;


import android.app.AlertDialog;
import android.app.Service;
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
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MenuActivity extends SimpleBaseGameActivity  implements ButtonNeedsUpdateDelegate  {

	private Camera camera;
	static final int SCREENWIDTH=720;
	static final int SCREENHEIGHT=1280;
	private ITextureRegion mTitleTexture;

	private Sprite mTitle;
	private Text quit;
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
	
    public static final String PREFS_NAME = "PokmanPrefs";
    public final static String LEVEL = "com.lortexgames.pokman.LEVEL";
	
    public static boolean GIFTIZ_ENABLED=true;
	
	@Override
	public EngineOptions onCreateEngineOptions() {
		camera = new Camera(0, 0, SCREENWIDTH, SCREENHEIGHT);
		return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(SCREENWIDTH,SCREENHEIGHT), camera);
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
	}

	@Override
	protected Scene onCreateScene() {
		final Scene scene = new Scene();
		scene.setBackground(new Background(0,0,0));
		
		final boolean hapticFeedback = System.getInt(this.getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED, 0) != 0;
		
		mTitle = new Sprite((SCREENWIDTH/2)-(mTitleTexture.getWidth()/2), (float) (SCREENHEIGHT * 0.1), mTitleTexture, getVertexBufferObjectManager());
		scene.attachChild(mTitle);
		
		final SharedPreferences settings = getSharedPreferences(MenuActivity.PREFS_NAME, 0);
        final int maxLevel = settings.getInt("maxLevel", 1);
        mInternetUsage = settings.getInt("internetUsage", OptionsActivity.IU_WIFI_ONLY);
        modtText = new Text(MenuActivity.SCREENWIDTH, 45, font.get(26), String.format("%500s", ""), this.getVertexBufferObjectManager());
        scene.attachChild(modtText); //AsyncGetModt
        updateModt=false;
		asyncTaskHandler = new Handler(Looper.getMainLooper());
		asyncTaskHandler.post(new Runnable() {
			@Override
			public void run() {
		    	new AsyncGetModt().execute((Void)null);
			}
	    });

        howto = new Text((float) (SCREENWIDTH*0.3), (float) (SCREENHEIGHT * 0.44), font.get(53), getResources().getString(R.string.btn_tuto), this.getVertexBufferObjectManager()){
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
		
		howto.setX((float) (0.5*SCREENWIDTH - howto.getWidth()/2.0));
		scene.attachChild(howto);
		scene.registerTouchArea(howto);
        
        
		play = new Text((float) (SCREENWIDTH*0.3), (float) (SCREENHEIGHT * 0.3), font.get(60, Color.WHITE), getResources().getString(R.string.btn_startgame), this.getVertexBufferObjectManager()) {
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
		
		
		
		play.setX((float) (0.5*SCREENWIDTH - play.getWidth()/2.0));
		
		options = new Text((float) (SCREENWIDTH*0.3), (float) (SCREENHEIGHT * 0.58), font.get(53), getResources().getString(R.string.btn_settings), this.getVertexBufferObjectManager()){
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
		options.setX((float) (0.5*SCREENWIDTH - options.getWidth()/2.0));
		
		quit = new Text((float) (SCREENWIDTH*0.3), (float) (SCREENHEIGHT * 0.7), font.get(53), getResources().getString(R.string.btn_quit), this.getVertexBufferObjectManager()){
			@Override
		    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
				if ((pSceneTouchEvent.isActionDown())||(pSceneTouchEvent.isActionMove()))
		        	this.setColor(1f,1f,0f);
		        else if(pSceneTouchEvent.isActionUp()) {
		        	if(hapticFeedback) {
			        	Vibrator v = (Vibrator) MenuActivity.this.getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
			        	v.vibrate(100);
		        	}
		            finish();
		        }
		        return true;
		    }
		};
		quit.setX((float) (SCREENWIDTH/2f - quit.getWidth()/2f - 70));
		
		credit = new Text(0, (float) (SCREENHEIGHT * 0.85), font.get(28), getResources().getString(R.string.btn_credit), this.getVertexBufferObjectManager()) {
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
		credit.setX(SCREENWIDTH/2f-credit.getWidth()/2f);
		
		Text pacVer = new Text(0, (float) (SCREENHEIGHT * 0.925),font.get(28), getResources().getString(R.string.btn_version), this.getVertexBufferObjectManager());
		pacVer.setX(SCREENWIDTH - pacVer.getWidth() - 10);
		
		feedbackText = new Text((float) (SCREENWIDTH*0.05), (float) (SCREENHEIGHT * 0.925), font.get(28), getResources().getString(R.string.btn_feedback), this.getVertexBufferObjectManager()) {
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
			GiftizSDK.Inner.setButtonNeedsUpdateDelegate(this);
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
			giftizButton.setY(quit.getY()-10);
			giftizButton.setX(quit.getX()+quit.getWidth()+100);
			
			scene.attachChild(giftizButton);
			scene.registerTouchArea(giftizButton);
		}
		
		scene.attachChild(play);
		scene.attachChild(options);
		scene.attachChild(quit);
		scene.attachChild(credit);
		scene.attachChild(pacVer);
		scene.attachChild(feedbackText);

		scene.registerTouchArea(play);
		scene.registerTouchArea(options);
		scene.registerTouchArea(quit);
		scene.registerTouchArea(credit);
		scene.registerTouchArea(feedbackText);
		
		scene.setOnSceneTouchListener(new IOnSceneTouchListener() {
			@Override
			public boolean onSceneTouchEvent(Scene pScene,TouchEvent pSceneTouchEvent) {
				quit.setColor(1f,1f,1f);
				options.setColor(1f,1f,1f);
				play.setColor(1f,1f,1f);
				credit.setColor(1f,1f,1f);
				feedbackText.setColor(1f,1f,1f);
				howto.setColor(1f,1f,1f);

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
					if(modtText.getX() < - modtText.getWidth() - MenuActivity.SCREENWIDTH) {
						modtText.setX(MenuActivity.SCREENWIDTH);
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

	@Override
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
	
	public void onBackPressed() {
		finish();
	}
	
	private class AsyncGetModt extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			
			ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
			NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			NetworkInfo mMobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

			if(mInternetUsage==OptionsActivity.IU_NEVER) 
				return null;
			
			if(mInternetUsage==OptionsActivity.IU_WIFI_ONLY)
			    if (!mWifi.isConnected()) 
			    	return null;
			
			if(!mMobile.isConnected() && !mWifi.isConnected()) 
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

}
