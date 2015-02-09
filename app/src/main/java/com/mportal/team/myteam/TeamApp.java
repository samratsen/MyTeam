package com.mportal.team.myteam;

import android.app.Application;
import android.content.Context;

/**
 * Created by samratsen on 2/1/15.
 * Application calss
 */
public class TeamApp extends Application{
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getContext()
    {
        return context;
    }

    public static String getUrl(){
        return getContext().getResources().getString(R.string.team_url);
    }
}
