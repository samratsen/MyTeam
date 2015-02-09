package com.mportal.team.myteam.apprtc.utils;

import com.mportal.team.myteam.TeamApp;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class PrefManagerBase {
    private static final String TAG = "PrefManagerBase";
	protected final SharedPreferences mSettings;
	// aes key encryption key part
	private static final String D_USER_PREF= "u_pref";
    private static final String PREF_ID = "pref_id";

	public PrefManagerBase() {
		mSettings = PreferenceManager.getDefaultSharedPreferences(TeamApp.getContext());

	}
	
	public void setUser(String s){
        SharedPreferences.Editor editor = mSettings.edit();

        try {
            editor.putString(D_USER_PREF, s);
            editor.commit();

        } catch (Exception e) {
            Log.d(TAG, "Unable to set id setting", e);
        }
	}
	
	public String getUser(){
        String v = mSettings.getString(D_USER_PREF, null);

        if (v!=null){
            try {
                return v;

            } catch (Exception e){
                Log.d(TAG, "Unable to resolve id setting", e);
            }

        }
        return null;
	}

    public synchronized int getNextId(){
        int v = mSettings.getInt(PREF_ID, 0);
        v++;
        setId(v);

        if (v>0){
            try {
                return v;

            } catch (Exception e){
                Log.d(TAG, "Unable to resolve id setting", e);
            }

        }
        return 0;
    }

    private void setId(int setting){
        SharedPreferences.Editor editor = mSettings.edit();

        try {
            editor.putInt(PREF_ID, setting);
            editor.commit();

        } catch (Exception e) {
            Log.d(TAG, "Unable to set id setting", e);
        }
    }

    public synchronized int getCurrentId(){
        int v = mSettings.getInt(PREF_ID, 0);

        if (v>0){
            try {
                return v;

            } catch (Exception e){
                Log.d(TAG, "Unable to resolve id setting", e);
            }

        }
        return 0;
    }
}
