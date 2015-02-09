package com.mportal.team.myteam.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.widget.Toast;

import com.mportal.team.myteam.R;
import com.mportal.team.myteam.TeamApp;
import com.mportal.team.myteam.apprtc.AppRTCClient;
import com.mportal.team.myteam.apprtc.SignallingParametersFetcher;
import com.mportal.team.myteam.apprtc.WebSocketRTCClient;
import com.mportal.team.myteam.apprtc.utils.PrefManagerBase;
import com.mportal.team.myteam.fragments.CallsFragment;
import com.mportal.team.myteam.fragments.ContactsFragment;
import com.mportal.team.myteam.fragments.LoginDialogFragment;
import com.mportal.team.myteam.fragments.MessagesFragment;
import com.mportal.team.myteam.fragments.NavigationDrawerFragment;
import com.mportal.team.myteam.model.ContactModel;
import com.mportal.team.myteam.model.ContactsStaticDataModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        ContactsFragment.OnFragmentInteractionListener,
        MessagesFragment.OnFragmentInteractionListener,
        CallsFragment.OnFragmentInteractionListener,
        LoginDialogFragment.OnLoginDialogListener,
        AppRTCClient.SignalingEvents {

    private static final String TAG = "MainActivity";
    private AppRTCClient appRtcClient;
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        startSession();

    }

    private void startSession(){
        ContactModel contactModel = ContactsStaticDataModel.getLogInUser();
        if (contactModel==null||contactModel.getIdTag()==null){

            PrefManagerBase prefMgr = new PrefManagerBase();
            String u = prefMgr.getUser();
            if (u==null||u.trim().isEmpty()) {
                showLoginDialog();
                return;
            } else {
                contactModel = new ContactModel();
                contactModel.setIdTag(u);
                ContactsStaticDataModel.setLogInUser(contactModel);
            }
        }
        String url = TeamApp.getUrl() + "/login?user="+contactModel.getIdTag();
        boolean loopback = false;
        appRtcClient = new WebSocketRTCClient(this);
        appRtcClient.connectToRoom(url, loopback);
    }

    public void showLoginDialog(){
        FragmentManager fm = getSupportFragmentManager();
        LoginDialogFragment dialog = new LoginDialogFragment();
        dialog.show(fm, LoginDialogFragment.class.getName());
    }

    @Override
    protected void onDestroy() {
        disconnect();
        super.onDestroy();
        /*if (logToast != null) {
            logToast.cancel();
        }
        activityRunning = false;*/
    }

    private void disconnect() {
        if (appRtcClient != null) {
            appRtcClient.disconnectFromRoom();
            appRtcClient = null;
        }
        /*if (pc != null) {
            pc.close();
            pc = null;
        }
        if (audioManager != null) {
            audioManager.close();
            audioManager = null;
        }
        if (iceConnected && !isError) {
            setResult(RESULT_OK);
        } else {
            setResult(RESULT_CANCELED);
        }*/
        finish();
    }

    private void disconnectWithErrorMessage(final String errorMessage) {
        /*if (commandLineRun || !activityRunning) {
            Log.e(TAG, "Critical error: " + errorMessage);
            disconnect();
        } else {*/
            new AlertDialog.Builder(this)
                    .setTitle("Communication Error")
                    .setMessage(errorMessage)
                    .setCancelable(false)
                    .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            disconnect();
                        }
                    }).create().show();
        //}
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = null;
        switch (position){
            case 0:
                fragment = new ContactsFragment();
                break;
            case 1:
                fragment = new MessagesFragment();
                break;
            case 2:
                fragment = new CallsFragment();
                break;
        }
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_contacts);
                break;
            case 2:
                mTitle = getString(R.string.title_messages);
                break;
            case 3:
                mTitle = getString(R.string.title_calls
                );
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onFragmentInteraction(String message) {
        if (message==null) return;
        try {
            JSONObject jsonObject = new JSONObject(message);
            String fragment = jsonObject.getString("fragment");
            if (fragment.equalsIgnoreCase("MessagesFragment")) {
                String messageStr = jsonObject.getString("message");
                appRtcClient.sendWSMessage(messageStr);
            }
        } catch (Exception e){
            Log.e(TAG, "json message parsing error", e);
        }
    }

    public void onContactClicked(int position){
        if (getSupportFragmentManager()==null) return;
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (fragment==null || !(fragment instanceof ContactsFragment)) return;

        ((ContactsFragment)fragment).onContactClicked(position);
    }

    @Override
    public void onConnectedToRoom(AppRTCClient.SignalingParameters params) {

    }

    @Override
    public void onRemoteDescription(SessionDescription sdp) {

    }

    @Override
    public void onRemoteIceCandidate(IceCandidate candidate) {

    }

    @Override
    public void onChannelClose() {
        Toast.makeText(getApplicationContext(), "Network connectivity closed", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onChannelError(String description) {
        Toast.makeText(getApplicationContext(), "There was a problem during Network connectivity", Toast.LENGTH_LONG).show();
        showLoginDialog();

    }

    @Override
    public void onWebSocketOpen() {
        Log.d(TAG, "websocket open");
    }

    @Override
    public void onWebSocketMessage(final String message) {
        Log.d(TAG, "onWebSocketMessage message " + message);
        if (message.equalsIgnoreCase("presence")){
            if (getSupportFragmentManager()==null) return;
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
            if (fragment==null || !(fragment instanceof ContactsFragment)) return;
            ((ContactsFragment)fragment).refreshList();
            return;
        }
        processWSMessages(message);
        /*runOnUiThread(new Runnable() {
            @Override
            public void run() {

                processWSMessages(message);

            }
        });*/
    }

    @Override
    public void onWebSocketError(String description) {
        Log.e(TAG, "websocket error " + description);
        Toast.makeText(getApplicationContext(), "Web Socket Error", Toast.LENGTH_SHORT).show();
        showLoginDialog();

    }

    @Override
    public void OnLoginDialogEnd(String message) {
        Log.d(TAG, "Login Dialog exited " + message);
        try {
            JSONObject jsonObject = new JSONObject(message);
            String u = jsonObject.getString("userName");
            if (u!=null||!u.isEmpty()){
                PrefManagerBase prefMgr = new PrefManagerBase();
                prefMgr.setUser(u);
                ContactModel contactModel = new ContactModel();
                contactModel.setIdTag(u);
                ContactsStaticDataModel.setLogInUser(contactModel);
                startSession();
            } else {
                showLoginDialog();
            }
        } catch (Exception e){
            Log.e(TAG, "exception ", e);
            Toast.makeText(getApplicationContext(), "Invalid credentials", Toast.LENGTH_SHORT).show();
        }

    }

    public void processWSMessages(String message){
        if (message!=null){
            try {
                JSONObject jsonObject = new JSONObject(message);
                if (!jsonObject.isNull("type")){
                    String type = jsonObject.getString("type");
                    Log.d(TAG, "onWebSocketMessage type "+ type);
                    if (type.equalsIgnoreCase("message")){
                        ContactModel loginuser = ContactsStaticDataModel.getLogInUser();
                        if (!jsonObject.isNull("to")){
                            JSONArray toUserArray = jsonObject.getJSONArray("to");
                            String toUser = toUserArray.getString(0);
                            Log.d(TAG, "onWebSocketMessage toUser "+ toUser);
                            Log.d(TAG, "onWebSocketMessage login user "+ loginuser.getIdTag());
                            String fromUser = jsonObject.getString("from");
                            Log.d(TAG, "onWebSocketMessage fromUser "+ fromUser);
                            if (loginuser!=null && loginuser.getIdTag()!=null && toUser!=null && toUser.equalsIgnoreCase(loginuser.getIdTag())){
                                if (getSupportFragmentManager()==null) return;
                                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
                                if (fragment==null || !(fragment instanceof MessagesFragment)) return;
                                ((MessagesFragment)fragment).onNewMessageReceived(message, fromUser, toUser);
                            }
                        }
                    }
                }
            }catch (JSONException e) {
                e.printStackTrace();

            }  catch (Exception e){
                Log.e(TAG, "Unhandled exception", e);
            }
        }
    }
}
