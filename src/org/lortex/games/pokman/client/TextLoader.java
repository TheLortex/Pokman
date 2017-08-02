package org.lortex.games.pokman.client;

import java.util.HashMap;

import org.lortex.games.pokman.R;

public class TextLoader {
	private SceneManagerActivity mContext;
	private HashMap<String, String> translations;
	
	public TextLoader (SceneManagerActivity context) {
		mContext = context;
		translations = new HashMap<String, String>();
		loadTranslations();
	}
	
	private void loadTranslations() {
		translations.put("app_name", load(R.string.app_title));
	}
	
	private String load(int id) {
		return mContext.getResources().getString(id);
	}
}
