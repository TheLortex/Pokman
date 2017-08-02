package org.lortex.games.pokman.client.scenes;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.andengine.entity.Entity;
import org.andengine.entity.modifier.AlphaModifier;
import org.andengine.entity.modifier.MoveModifier;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.adt.color.Color;
import org.lortex.games.pokman.R;
import org.lortex.games.pokman.client.FontManager;
import org.lortex.games.pokman.client.SceneManagerActivity;
import org.lortex.games.pokman.client.TextureHandler;
import org.lortex.games.pokman.common.GameMode;

import com.esotericsoftware.kryonet.Client;
import com.google.android.gms.games.Games;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.graphics.pdf.PdfDocument.Page;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class NewMenuScene extends PScene {

	private SceneManagerActivity mContext;
	private Scene mMainScene;
	
	private FontManager mFontManager;
	private TextureHandler mTex;
	
	private int currentPage=PAGE_MAIN;
	private Sprite mBar;
	private Text mPlayTitleText;
	private Text mConnectText;
	private Text mGPGText;
	private Sprite mGPGSprite;
	private Text mOnlineText;

	private Text mAchievementsText;
	private Text mLeaderboardsText;
	private Text mSingleplayerText;
	private Text mLocalMultiplayerText;
	private Text mQuickmatchText;
	private Text mInviteText;
	private Text mShowInvitesText;
	private boolean mReady = false;

	public static final int PAGE_MAIN = 0;
	public static final int PAGE_MAIN_LOADGPG = 1;
	public static final int PAGE_MAIN_GPG = 2;
	public static final int PAGE_PLAY = 3;
	public static final int PAGE_ONLINE = 4;

	public static final int REQUEST_LEADERBOARD = 10042;
	
	public NewMenuScene (SceneManagerActivity context) {
		mContext = context;
	}
	
	@Override
	public void onLoadResources() {
		FontFactory.setAssetBasePath("font/");
		mFontManager = new FontManager(mContext);
		mTex = new TextureHandler(mContext);

		mTex.load("menu_bg", "gfx/menu.png");
		mTex.load("menu_bar", "gfx/menu_bar.png");
		mTex.load("gpg_icon", "gfx/gpg_icon.png");
	}
	
	public void moveEntity(Entity spr, float x, float y, boolean direct) {
		if(!direct)
			spr.registerEntityModifier(new MoveModifier(0.2f, spr.getX(), spr.getY(), x, y));
		else 
			spr.setPosition(x, y);
	}
	
	public void alphaEntity(Entity spr, float alpha, boolean direct) {
	/*	if(!direct)
			spr.registerEntityModifier(new AlphaModifier(0.05f, spr.getAlpha(), alpha));
		else*/
	//		spr.setAlpha(alpha);
		if(alpha == 1)
			spr.setVisible(true);
		else
			spr.setVisible(false);
	}

	@Override
	public void onCreateScene() {
		mMainScene = new Scene();
		mMainScene.getBackground().setColor(0, 0, 0);

		mBar = new Sprite(centerX(),  300, mTex.get("menu_bar"), getVbo());
		mBar.setWidth(mContext.getWidth());
		mMainScene.attachChild(mBar);
		
		Sprite mBackground = new Sprite(centerX(),centerY(),mTex.get("menu_bg"),getVbo());
		mBackground.setWidth(mContext.getWidth());
		mMainScene.attachChild(mBackground);
		
		mPlayTitleText = new Text(centerX(), 600, mFontManager.get(86), "JOUER", getVbo()){
			@Override
		    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float X, float Y) 
			{
				if(currentPage == PAGE_MAIN || currentPage == PAGE_MAIN_GPG || currentPage == PAGE_MAIN_LOADGPG) {
					if (pSceneTouchEvent.isActionDown() || pSceneTouchEvent.isActionMove()){
			        	this.setColor(new Color(0.9f,0.5f,0.1f));
			        } else if (pSceneTouchEvent.isActionUp()) {
			        	mPlayTitleText.setColor(Color.WHITE);
			        	//setPage(PAGE_PLAY, false);
			        	onSingleplayerButtonPressed();
			        }
				}
				return true;
			}
		};
		
		mMainScene.attachChild(mPlayTitleText);
		mMainScene.registerTouchArea(mPlayTitleText);
		
		
		mConnectText	= new Text(centerX() + 77, 190, mFontManager.get(49), "Connexion", 11, getVbo()){
			@Override
		    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float X, float Y) 
			{
				switch(currentPage) {
				case PAGE_MAIN:
					if (pSceneTouchEvent.isActionDown() || pSceneTouchEvent.isActionMove()){
			        	this.setColor(new Color(0.9f,0.5f,0.1f));
			        } else if (pSceneTouchEvent.isActionUp()) {
			        	mConnectText.setColor(Color.WHITE);
		 				mContext.getGameHelper().beginUserInitiatedSignIn();
		 				setPage(PAGE_MAIN_LOADGPG, false);
			        }
					return true;
				case PAGE_ONLINE:
				case PAGE_MAIN_GPG:
					if (pSceneTouchEvent.isActionDown() || pSceneTouchEvent.isActionMove()){
			        	this.setColor(new Color(0.9f,0.5f,0.1f));
			        } else if (pSceneTouchEvent.isActionUp()) {
			        	mConnectText.setColor(Color.WHITE);
		 				mContext.getGameHelper().signOut();
		 				setPage(PAGE_MAIN, false);
			        }
					return true;
				}
				return false;
			}
		};
		mMainScene.attachChild(mConnectText);
		mMainScene.registerTouchArea(mConnectText);
		
		mGPGText	= new Text(centerX() + 77, 140, mFontManager.get(28), "Google Play Jeux", getVbo());
		mMainScene.attachChild(mGPGText);
		
		mGPGSprite = new Sprite(127, 175, mTex.get("gpg_icon"), getVbo());
		mMainScene.attachChild(mGPGSprite);
		
		mOnlineText = new Text(centerX() + 85, 500, mFontManager.get(70), "ONLINE", getVbo()){
			@Override
		    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float X, float Y) 
			{
				if(currentPage == PAGE_MAIN || currentPage == PAGE_MAIN_GPG || currentPage == PAGE_MAIN_LOADGPG) {
					if (pSceneTouchEvent.isActionDown() || pSceneTouchEvent.isActionMove()){
			        	this.setColor(new Color(0.9f,0.5f,0.1f));
			        } else if (pSceneTouchEvent.isActionUp()) {
			        	mOnlineText.setColor(Color.WHITE);
			        	setPage(PAGE_ONLINE, false);
			        }
					return true;
				}
				return false;
			}
		};
		
		
		mOnlineText.setVisible(false);
		mMainScene.attachChild(mOnlineText);
		
		mMainScene.registerTouchArea(mOnlineText);

		mAchievementsText = new Text(centerX(), 200, mFontManager.get(45), "ACHIEVEMENTS", getVbo()){
			@Override
		    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float X, float Y) 
			{
				if(currentPage == PAGE_MAIN_GPG || currentPage == PAGE_ONLINE) {
					if (pSceneTouchEvent.isActionDown() || pSceneTouchEvent.isActionMove()){
			        	this.setColor(new Color(0.9f,0.5f,0.1f));
			        } else if (pSceneTouchEvent.isActionUp()) {
			        	this.setColor(Color.WHITE);
			        	onAchievementsButtonPressed();
			        }
					return true;
				}
				return false;
			}
		};
		mAchievementsText.setVisible(false);
		mMainScene.attachChild(mAchievementsText);
		mMainScene.registerTouchArea(mAchievementsText);
		
		mLeaderboardsText = new Text(centerX(), 300, mFontManager.get(45), "CLASSEMENTS", getVbo()){
			@Override
		    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float X, float Y) 
			{
				if(currentPage == PAGE_MAIN_GPG || currentPage == PAGE_ONLINE) {
					if (pSceneTouchEvent.isActionDown() || pSceneTouchEvent.isActionMove()){
			        	this.setColor(new Color(0.9f,0.5f,0.1f));
			        } else if (pSceneTouchEvent.isActionUp()) {
			        	this.setColor(Color.WHITE);
			        	onLeaderboardsButtonPressed();
			        }
					return true;
				}
				return false;
			}
		};
		
		mLeaderboardsText.setVisible(false);
		mMainScene.attachChild(mLeaderboardsText);
		mMainScene.registerTouchArea(mLeaderboardsText);
		
		
		
		
		mSingleplayerText = new Text(centerX(), 600, mFontManager.get(60), "Mode Solo", getVbo()) {
			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float X, float Y) 
			{
				if(currentPage == PAGE_PLAY) {
					if (pSceneTouchEvent.isActionDown() || pSceneTouchEvent.isActionMove()){
			        	this.setColor(new Color(0.9f,0.5f,0.1f));
			        } else if (pSceneTouchEvent.isActionUp()) {
			        	this.setColor(Color.WHITE);
			        	onSingleplayerButtonPressed();
			        }
					return true;
				}
				return false;
			}
		};
		mMainScene.attachChild(mSingleplayerText);
		mMainScene.registerTouchArea(mSingleplayerText);
		
		mLocalMultiplayerText = new Text(centerX(), 400, mFontManager.get(50), "Multijoueurs\n   Local", getVbo()) {
			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float X, float Y) 
			{
				if(currentPage == PAGE_PLAY) {
					if (pSceneTouchEvent.isActionDown() || pSceneTouchEvent.isActionMove()){
			        	this.setColor(new Color(0.9f,0.5f,0.1f));
			        } else if (pSceneTouchEvent.isActionUp()) {
			        	this.setColor(Color.WHITE);
			        	onLocalMultiplayerButtonPressed();
			        }
					return true;
				}
				return false;
			}
		};
		mMainScene.attachChild(mLocalMultiplayerText);
		mMainScene.registerTouchArea(mLocalMultiplayerText);
		
		mQuickmatchText  = new Text(centerX(), 620, mFontManager.get(42), " Lancer une\npartie rapide", getVbo()) {
			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float X, float Y) 
			{
				if(currentPage == PAGE_ONLINE) {
					if (pSceneTouchEvent.isActionDown() || pSceneTouchEvent.isActionMove()){
			        	this.setColor(new Color(0.9f,0.5f,0.1f));
			        } else if (pSceneTouchEvent.isActionUp()) {
			        	this.setColor(Color.WHITE);
			        	onQuickmathButtonPressed();
			        }
					return true;
				}
				return false;
			}
		};
		mMainScene.attachChild(mQuickmatchText);
		mMainScene.registerTouchArea(mQuickmatchText);
		
		mInviteText  = new Text(centerX(), 520, mFontManager.get(40), "Inviter des amis", getVbo()) {
			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float X, float Y) 
			{
				if(currentPage == PAGE_ONLINE) {
					if (pSceneTouchEvent.isActionDown() || pSceneTouchEvent.isActionMove()){
			        	this.setColor(new Color(0.9f,0.5f,0.1f));
			        } else if (pSceneTouchEvent.isActionUp()) {
			        	this.setColor(Color.WHITE);
			        	onInviteFriendsButtonPressed();
			        }
					return true;
				}
				return false;
			}
		};
		mMainScene.attachChild(mInviteText);
		mMainScene.registerTouchArea(mInviteText);
		
		mShowInvitesText  = new Text(centerX(), 420, mFontManager.get(40), " Voir les\ninvitations", getVbo()) {
			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float X, float Y) 
			{
				if(currentPage == PAGE_ONLINE) {
					if (pSceneTouchEvent.isActionDown() || pSceneTouchEvent.isActionMove()){
			        	this.setColor(new Color(0.9f,0.5f,0.1f));
			        } else if (pSceneTouchEvent.isActionUp()) {
			        	this.setColor(Color.WHITE);
			        	onShowInvitationsButtonPressed();
			        }
					return true;
				}
				return false;
			}
		};
		mMainScene.attachChild(mShowInvitesText);
		mMainScene.registerTouchArea(mShowInvitesText);
		
		if(mContext.getGameHelper().isConnecting()) {
			this.setPage(PAGE_MAIN_LOADGPG, true);
		}
		
		mMainScene.setOnSceneTouchListener(new IOnSceneTouchListener() {
			@Override
			public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
				if(mConnectText.getAlpha() != 0)
					mConnectText.setColor(Color.WHITE);
				if(mPlayTitleText.getAlpha() != 0)
					mPlayTitleText.setColor(Color.WHITE);
				if(mOnlineText.getAlpha() != 0)
					mOnlineText.setColor(Color.WHITE);
				if(mLeaderboardsText.getAlpha() != 0)
					mLeaderboardsText.setColor(Color.WHITE);
				if(mAchievementsText.getAlpha() != 0)
					mAchievementsText.setColor(Color.WHITE);
				if(mSingleplayerText.getAlpha() != 0)
					mSingleplayerText.setColor(Color.WHITE);
				if(mLocalMultiplayerText.getAlpha() != 0)
					mLocalMultiplayerText.setColor(Color.WHITE);
				if(mQuickmatchText.getAlpha() != 0)
					mQuickmatchText.setColor(Color.WHITE);
				if(mInviteText.getAlpha() != 0)
					mInviteText.setColor(Color.WHITE);
				if(mShowInvitesText.getAlpha() != 0)
					mShowInvitesText.setColor(Color.WHITE);
				
				
				if(pSceneTouchEvent.getY() > 900 && currentPage != PAGE_MAIN && currentPage != PAGE_MAIN_GPG && currentPage != PAGE_MAIN_LOADGPG) {
					showMainPage(false);
				}
				return false;
			}
		});

        
        mReady  = true;
        showMainPage(true);
	}
	
	/*
	 * 
	 * Actions linked to buttons
	 * 
	 */
	
	
	private void onSingleplayerButtonPressed() {
		SharedPreferences settings = mContext.getSharedPreferences("org.lortex.games.pokman", 0);
        final int maxLevel = settings.getInt("best_level", 1);
		
        if(maxLevel == 1)
        	loadSingleplayerGame(1);
        else {
        	mContext.runOnUiThread(new Runnable() {
				private AlertDialog mDialog;
				private int selectedLevel = 1;
				@Override
				public void run() {
					AlertDialog.Builder chooseLevelDialog = new AlertDialog.Builder(mContext);
					chooseLevelDialog.setTitle("Sélectionner le niveau de départ");
					SeekBar levelSelect = new SeekBar(mContext);
					
					levelSelect.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
						
						@Override
						public void onStopTrackingTouch(SeekBar seekBar) {}
						
						@Override
						public void onStartTrackingTouch(SeekBar seekBar) {}
						
						@Override
						public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
							selectedLevel = progress + 1;
							if(mDialog != null)
								mDialog.setMessage("Niveau choisi: " + selectedLevel);
						}
					});
					levelSelect.setMax(maxLevel-1);
					chooseLevelDialog.setMessage("Niveau choisi: 1");
					chooseLevelDialog.setPositiveButton("OK", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,int which) {
							loadSingleplayerGame(selectedLevel);
							
						}
					});
					chooseLevelDialog.setNegativeButton("Annuler", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int which) {}
					});
					chooseLevelDialog.setView(levelSelect);
					mDialog = chooseLevelDialog.create();
					mDialog.show();
				}
        	});
        }
	}
	
	private void onLocalMultiplayerButtonPressed() {
		mContext.runOnUiThread(new Runnable() {			
			@Override
			public void run() {
				AlertDialog.Builder multiDialogBuilder = new AlertDialog.Builder(mContext);
				multiDialogBuilder.setPositiveButton("Héberger la partie", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mContext.unlockMultiplayerAchievements();
						GameMode gm = new GameMode(GameMode.MULTIPLAYER_TEST, 1);
						mContext.setScene(new GameScene(mContext,gm,true,"127.0.0.1",0));
					}
				});
				multiDialogBuilder.setNegativeButton("Rejoindre une partie", new OnClickListener() {
					
					private ProgressBar prg;
					private AlertDialog the_dialog;

					@Override
					public void onClick(DialogInterface dialog, int which) {
						new Thread(new Runnable() {
							@Override
							public void run() {
								Client mClient = new Client();
								mClient.start();
								final List<InetAddress> servers  = mClient.discoverHosts(45568, 5000);
								mClient.stop();
								final ArrayList<String> list = new ArrayList<String>();
								final Vector<String> destinations = new Vector<String>();
								
								for(int i=0;i<servers.size();i++) {
									list.add(servers.get(i).getHostName() + " @" + servers.get(i).getHostAddress());
									destinations.add(servers.get(i).getHostAddress());
								}
								
								mContext.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										prg.setVisibility(View.GONE);
									    final ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,android.R.layout.simple_list_item_1, list);
									    
									    the_dialog.getListView().setAdapter(adapter);
									    the_dialog.getListView().setOnItemClickListener(new OnItemClickListener() {

											@Override
											public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
												mContext.unlockMultiplayerAchievements();
												GameMode gm = new GameMode(GameMode.MULTIPLAYER_TEST, 1);
												mContext.setScene(new GameScene(mContext,gm,false,destinations.get(position),0));
											}
									    });
										
									    if(list.size() == 0) {
									    	the_dialog.setTitle("Aucun serveur disponible");
									    } else {
									    	the_dialog.setTitle("Sélectionner un serveur");
									    }
									    
									    
									}
								});
								
							}
						}).start();
						
						AlertDialog.Builder dial = new AlertDialog.Builder(mContext);
						dial.setTitle("Recherche des serveurs disponibles...");
						prg = new ProgressBar(mContext);
						dial.setPositiveButton("Entrer l'ip manuellement", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,int which) {
								final EditText input = new EditText(mContext);
								
								new AlertDialog.Builder(mContext)
									.setTitle("Entrer l'ip")
									.setView(input)
									.setPositiveButton("OK", new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											mContext.unlockMultiplayerAchievements();
											GameMode gm = new GameMode(GameMode.MULTIPLAYER_TEST, 1);
											mContext.setScene(new GameScene(mContext,gm,false,input.getText().toString(),0));
										}

									})
									.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {}
									}).show();
							}
					    });
						dial.setView(prg);
						dial.setAdapter(new ArrayAdapter<String>(mContext,android.R.layout.simple_list_item_1, new ArrayList<String>()), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {}
						});
						
						the_dialog = dial.show();
					}
				});
				multiDialogBuilder.show();
			}
		});
	}
	
	private void onQuickmathButtonPressed() {
		Log.w("LORTEX","[Menu] Quick match!");
		mContext.getMultiplayerHandler().startQuickGame();
	}
	
	private void onShowInvitationsButtonPressed() {
		mContext.getMultiplayerHandler().seeInvitations();
	}
	
	private void onInviteFriendsButtonPressed() {
		mContext.getMultiplayerHandler().invitePlayers();
	}
	
	private void onLeaderboardsButtonPressed() {
    	if(mContext.getGameHelper().isSignedIn()) {
        	mContext.startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mContext.getGameHelper().getApiClient(),mContext.getResources().getString(R.string.leaderboard_high_scores)), REQUEST_LEADERBOARD);
 		} 
	}
	
	private void onAchievementsButtonPressed() {
    	if(mContext.getGameHelper().isSignedIn()) {
        	mContext.startActivityForResult(Games.Achievements.getAchievementsIntent(mContext.getGameHelper().getApiClient()),42);
 		} 
	}
	
	
	
	
	public void startTutorial() {
		mContext.setScene(new TutorialScene(mContext));
	}
	
	public void loadSingleplayerGame(int level) {
		if(level == 1) {
			mContext.getAchievementsManager().setRush(true);
			mContext.getAchievementsManager().setMaster(true);
		}
		else {
			mContext.getAchievementsManager().setRush(false);
			mContext.getAchievementsManager().setMaster(false);
		}
		
		GameMode gm = new GameMode(GameMode.SINGLEPLAYER, level);
		mContext.setScene(new GameScene(mContext, gm, true, "127.0.0.1",0));
	}
	
	
	
	private VertexBufferObjectManager getVbo() {
		return mContext.getVertexBufferObjectManager();
	}

	@Override
	public Scene getScene() {
		return mMainScene;
	}
	
	public void setPage(int p, boolean direct) {
		currentPage = p;
		renderUpdate(direct);
	}
	
	public float centerX() {
		return mContext.getWidth()/2f;
	}
	
	public float centerY() {
		return SceneManagerActivity.getHeight()/2f;
	}
	
	public void renderUpdate(boolean direct) {
		switch(currentPage) {
		case PAGE_MAIN:
			Log.w("LORTEX","[Menu] Page: MAIN");
			moveEntity (mBar, centerX(), 300, direct);

			moveEntity (mPlayTitleText, centerX(), 600, direct);
			alphaEntity(mPlayTitleText, 1, direct);

			moveEntity (mConnectText, centerX() + 77, 190, direct);
			alphaEntity(mConnectText, 1, direct);
			mConnectText.setText("Connexion");

			moveEntity (mGPGText, centerX() + 77, 140, direct);
			alphaEntity(mGPGText, 1, direct);

			moveEntity (mGPGSprite, mContext.getWidth()/6, 175, direct);
			alphaEntity(mGPGSprite, 1, direct);

			alphaEntity(mOnlineText, 0, direct);
			alphaEntity(mAchievementsText, 0, direct);
			alphaEntity(mLeaderboardsText, 0, direct);
			
			alphaEntity(mSingleplayerText, 0, direct);
			alphaEntity(mLocalMultiplayerText, 0, direct);
			alphaEntity(mQuickmatchText,0, direct);
			alphaEntity(mInviteText, 0, direct);
			alphaEntity(mShowInvitesText, 0, direct);
			
			break;
		case PAGE_MAIN_LOADGPG:
			Log.w("LORTEX","[Menu] Page: LOADGPG");
			moveEntity (mBar, centerX(), 300, direct);

			moveEntity (mPlayTitleText, centerX(), 600, direct);
			alphaEntity(mPlayTitleText, 1, direct);

			moveEntity (mConnectText, centerX() + 77, 190, direct);
			alphaEntity(mConnectText, 1, direct);
			mConnectText.setText("Chargement");

			moveEntity (mGPGText, centerX() + 77, 140, direct);
			alphaEntity(mGPGText, 1, direct);

			moveEntity (mGPGSprite, mContext.getWidth()/6, 175, direct);
			alphaEntity(mGPGSprite, 1, direct);

			alphaEntity(mOnlineText, 0, direct);
			alphaEntity(mAchievementsText, 0, direct);
			alphaEntity(mLeaderboardsText, 0, direct);

			alphaEntity(mSingleplayerText, 0, direct);
			alphaEntity(mLocalMultiplayerText, 0, direct);
			alphaEntity(mQuickmatchText,0, direct);
			alphaEntity(mInviteText, 0, direct);
			alphaEntity(mShowInvitesText, 0, direct);
			break;
		case PAGE_MAIN_GPG:
			Log.w("LORTEX","[Menu] Page: GPG");
			moveEntity (mBar, centerX(), 500, direct);
			
			moveEntity (mPlayTitleText,centerX(), 700, direct);
			alphaEntity(mPlayTitleText,1, direct);
			
			moveEntity (mConnectText,centerX(), 100, direct);
			alphaEntity(mConnectText,1, direct);
			mConnectText.setText("Déconnexion");
			
			moveEntity (mGPGText,centerX() + 77, 140, direct);
			alphaEntity(mGPGText,0, direct);
			
			moveEntity (mGPGSprite,mContext.getWidth()/6, 415, direct);
			alphaEntity(mGPGSprite,1, direct);

			moveEntity (mOnlineText,centerX() + 85, 500, direct);
			alphaEntity(mOnlineText,0, direct);
			alphaEntity(mAchievementsText,1, direct);
			alphaEntity(mLeaderboardsText,1, direct);

			alphaEntity(mSingleplayerText, 0, direct);
			alphaEntity(mLocalMultiplayerText, 0, direct);
			alphaEntity(mQuickmatchText,0, direct);
			alphaEntity(mInviteText, 0, direct);
			alphaEntity(mShowInvitesText, 0, direct);
			break;
		case PAGE_PLAY:
			Log.w("LORTEX","[Menu] Page: PLAY");
			moveEntity (mBar, centerX(), 100, direct);
			
			moveEntity (mPlayTitleText,centerX(), 800, direct);
			alphaEntity(mPlayTitleText,1, direct);
			
			alphaEntity(mConnectText,0, direct);;
			alphaEntity(mGPGText,0, direct);
			alphaEntity(mGPGSprite,0, direct);
			alphaEntity(mOnlineText,0, direct);
			alphaEntity(mAchievementsText,0, direct);
			alphaEntity(mLeaderboardsText,0, direct);

			alphaEntity(mSingleplayerText, 1, direct);
			alphaEntity(mLocalMultiplayerText, 1, direct);
			
			alphaEntity(mQuickmatchText,0, direct);
			alphaEntity(mInviteText, 0, direct);
			alphaEntity(mShowInvitesText, 0, direct);
			break;
		case PAGE_ONLINE:
			Log.w("LORTEX","[Menu] Page: ONLINE");
			moveEntity (mBar, centerX(), 875, direct);
			alphaEntity(mPlayTitleText,0, direct);
			
			moveEntity (mConnectText,centerX(), 100, direct);
			alphaEntity(mConnectText,1, direct);
			mConnectText.setText("Déconnexion");
			
			moveEntity (mGPGText,centerX() + 77, 140, direct);
			alphaEntity(mGPGText,0, direct);
			
			moveEntity (mGPGSprite,mContext.getWidth()/6, 775, direct);
			alphaEntity(mGPGSprite,0, direct);

			moveEntity (mOnlineText,centerX() + 85, 775, direct);
			alphaEntity(mOnlineText,0, direct);
			
			alphaEntity(mAchievementsText,1, direct);
			alphaEntity(mLeaderboardsText,1, direct);

			alphaEntity(mSingleplayerText, 0, direct);
			alphaEntity(mLocalMultiplayerText, 0, direct);

			alphaEntity(mQuickmatchText, 1, direct);
			alphaEntity(mInviteText, 1, direct);
			alphaEntity(mShowInvitesText, 1, direct);
			break;
		}
	}
	

	@Override
	public void onBackPressed() {
		if(currentPage == PAGE_MAIN || currentPage == PAGE_MAIN_GPG || currentPage == PAGE_MAIN_LOADGPG)
			mContext.finish();
		
		showMainPage(false);
	}
	
	public void showMainPage(boolean direct) {
		if(mContext.getGameHelper().isSignedIn()) {
        	setPage(PAGE_MAIN_GPG, direct);
		} else if(mContext.getGameHelper().isConnecting()) {
        	setPage(PAGE_MAIN_LOADGPG, direct);
		} else  {
        	setPage(PAGE_MAIN, direct);
		}
	}
	
	
	@Override
	public void onSignInFailed() {
		mContext.setAutoConnect(false);
		mContext.runOnUpdateThread(new Runnable() {
			@Override
			public void run() {
				if(mReady)
					setPage(PAGE_MAIN, false);
			}
		});
	}
	

	@Override
	public void onSignInSucceeded() {
		mContext.setAutoConnect(false);
		mContext.runOnUpdateThread(new Runnable() {
			@Override
			public void run() {
				if(mReady) {
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							setPage(PAGE_MAIN_GPG, true);
						}
					}).start();
				}
			}
		});
	}
}
