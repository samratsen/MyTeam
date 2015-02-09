package com.mportal.team.myteam.apprtc;

import android.util.Log;

import com.mportal.team.myteam.apprtc.utils.AsyncHttpURLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by samratsen on 2/5/15.
 */
public class SignallingParametersFetcher {
    private static final String TAG = "RoomRTCClient";
    private SignallingParametersFetcherEvents events=null;
    private boolean loopback=false;
    private String registerUrl=null;
    private AsyncHttpURLConnection httpConnection;

    public static interface  SignallingParametersFetcherEvents {
        /**
         * Callback fired once the room's signaling parameters
         * SignalingParameters are extracted.
         */
        public void onSignalingParametersReady(final AppRTCClient.SignalingParameters params);

        /**
         * Callback for room parameters extraction error.
         */
        public void onSignalingParametersError(final String description);
    }

    public SignallingParametersFetcher(boolean loopback, final String registerUrl,
                                 final SignallingParametersFetcherEvents events) {
        Log.d(TAG, "Connecting to room: " + registerUrl);
        this.loopback = loopback;
        this.registerUrl = registerUrl;
        this.events = events;

        httpConnection = new AsyncHttpURLConnection("POST", registerUrl, null,
                new AsyncHttpURLConnection.AsyncHttpEvents() {
                    @Override
                    public void onHttpError(String errorMessage) {
                        Log.e(TAG, "Room connection error: " + errorMessage);
                        events.onSignalingParametersError(errorMessage);
                    }

                    @Override
                    public void onHttpComplete(String response) {
                        Log.d(TAG, "On Http Complete: " + response);
                        try {
                            String roomUrl = registerUrl;

                            LinkedList<IceCandidate> iceCandidates = null;
                            LinkedList<PeerConnection.IceServer> iceServers = null;
                            SessionDescription offerSdp = null;

                            JSONObject jsonObject = new JSONObject(response);
                            String roomId = jsonObject.getString("sessionUserId");
                            String clientId = jsonObject.getString("sessionAuthToken");
                            JSONObject configObject = jsonObject.getJSONObject("config");
                            JSONArray groupsArray = configObject.getJSONArray("groups");
                            List<String> groups = new ArrayList<String>();
                            if (groupsArray!=null){
                                for (int i = 0; i < groupsArray.length(); ++i) {
                                    String group = (String)groupsArray.get(i);
                                    groups.add(group);
                                }
                            }

                            String wssUrl = configObject.getString("webSocket");
                            String wssPostUrl = configObject.getString("rtcSocket");
                            String mixPanel = configObject.getString("mixpanel");
                            // information has to come from TEAM server, missing now
                            boolean initiator = false;
                            if (!configObject.isNull("is_initiator")) {
                                initiator = configObject.getBoolean("is_initiator");
                            }

                            if (!initiator){
                                // if its not an initiator then it has to get offer or sdp ice candidate messages,
                                // there is nothing in TEAM protocol and it depends on a persistent
                                // ws connection with the server
                            }

                            JSONObject peerConfig = configObject.getJSONObject("peerConfig");
                            String stunURL = peerConfig.getString("stunServer");
                            String serverInfo = jsonObject.getString("serverInfo");
                            String status = jsonObject.getString("status");

                            iceServers =
                                    iceServersFromConfigJSON(peerConfig.getJSONArray("iceServers"));
                            boolean isTurnPresent = false;
                            for (PeerConnection.IceServer server : iceServers) {
                                Log.d(TAG, "IceServer: " + server);
                                if (server.uri.startsWith("turn:")) {
                                    isTurnPresent = true;
                                    break;
                                }
                            }

                            // All Media constraints missing
                            MediaConstraints pcConstraints = null;
                            MediaConstraints videoConstraints = null;
                            MediaConstraints audioConstraints = null;

                            AppRTCClient.SignalingParameters params = new AppRTCClient.SignalingParameters(
                                    iceServers, initiator,
                                    pcConstraints, videoConstraints, audioConstraints,
                                    roomUrl, roomId, clientId,
                                    wssUrl, wssPostUrl,
                                    offerSdp, iceCandidates,
                                    groups, mixPanel, status,
                                    serverInfo, stunURL);
                            events.onSignalingParametersReady(params);

                        } catch (Exception e){
                            events.onSignalingParametersError("JSON parse exception");
                            Log.e(TAG, "JSON parse exception", e);
                        }
                    }
                });
        httpConnection.send();
    }

    private LinkedList<PeerConnection.IceServer> iceServersFromConfigJSON(
            JSONArray servers) throws JSONException {
        //JSONObject json = new JSONObject(pcConfig);
        //JSONArray servers = json.getJSONArray(pcConfig);
        if (servers==null) return null;
        LinkedList<PeerConnection.IceServer> ret =
                new LinkedList<PeerConnection.IceServer>();
        for (int i = 0; i < servers.length(); ++i) {
            JSONObject server = servers.getJSONObject(i);
            String url = server.getString("url");
            String userName =
                    server.has("username") ? server.getString("username") : "";
            String credential =
                    server.has("credential") ? server.getString("credential") : "";
            ret.add(new PeerConnection.IceServer(url, userName, credential));
        }
        return ret;
    }
}
