/*
 * libjingle
 * Copyright 2014, Google Inc.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.mportal.team.myteam.apprtc;

import android.util.Log;
import android.widget.Toast;

import com.mportal.team.myteam.TeamApp;
import com.mportal.team.myteam.apprtc.utils.AsyncHttpURLConnection;
import com.mportal.team.myteam.apprtc.utils.LooperExecutor;
import com.mportal.team.myteam.model.ContactModel;
import com.mportal.team.myteam.model.ContactsStaticDataModel;
import com.mportal.team.myteam.model.Device;
import com.mportal.team.myteam.model.SessionStaticDataModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import java.util.ArrayList;
import java.util.List;

/**
 * Negotiates signaling for chatting with apprtc.appspot.com "rooms".
 * Uses the client<->server specifics of the apprtc AppEngine webapp.
 *
 * <p>To use: create an instance of this object (registering a message handler) and
 * call connectToRoom().  Once room connection is established
 * onConnectedToRoom() callback with room parameters is invoked.
 * Messages to other party (with local Ice candidates and answer SDP) can
 * be sent after WebSocket connection is established.
 */
public class WebSocketRTCClient implements AppRTCClient,
        WebSocketChannelClient.WebSocketChannelEvents {
  private static final String TAG = "WSRTCClient";

  private enum ConnectionState {
    NEW, CONNECTED, CLOSED, ERROR
  };
  private enum MessageType {
    MESSAGE, BYE
  };
  private final LooperExecutor executor;
  private boolean loopback;
  private boolean initiator;
  private SignalingEvents events;
  private WebSocketChannelClient wsClient;
  private ConnectionState roomState;
  private String postMessageUrl;
  private String byeMessageUrl;

  public WebSocketRTCClient(SignalingEvents events) {
    this.events = events;
    executor = new LooperExecutor();
  }

  // --------------------------------------------------------------------
  // AppRTCClient interface implementation.
  // Asynchronously connect to an AppRTC room URL, e.g.
  // https://apprtc.appspot.com/register/<room>, retrieve room parameters
  // and connect to WebSocket server.
  @Override
  public void connectToRoom(final String url, final boolean loopback) {
    executor.requestStart();
    executor.execute(new Runnable() {
      @Override
      public void run() {
        connectToRoomInternal(url, loopback);
      }
    });
  }

  @Override
  public void disconnectFromRoom() {
    executor.execute(new Runnable() {
      @Override
      public void run() {
        disconnectFromRoomInternal();
      }
    });
    executor.requestStop();
  }

  // Connects to room - function runs on a local looper thread.
  private void connectToRoomInternal(String url, boolean loopback) {
    Log.d(TAG, "Connect to room: " + url);
    this.loopback = loopback;
    roomState = ConnectionState.NEW;
    // Create WebSocket client.
    wsClient = new WebSocketChannelClient(executor, this);
    // Get room parameters.
    new SignallingParametersFetcher(loopback, url,
      new SignallingParametersFetcher.SignallingParametersFetcherEvents() {
        @Override
        public void onSignalingParametersReady(
            final SignalingParameters params) {
            SessionStaticDataModel.params = params;
          executor.execute(new Runnable() {
            @Override
            public void run() {
              signalingParametersReady(params);
            }
          });
        }

        @Override
        public void onSignalingParametersError(String description) {
          reportError(description);
        }
      }
    );
  }

  // Disconnect from room and send bye messages - runs on a local looper thread.
  private void disconnectFromRoomInternal() {
    Log.d(TAG, "Disconnect. Room state: " + roomState);
    if (roomState == ConnectionState.CONNECTED) {
      Log.d(TAG, "Closing room.");
      sendPostMessage(MessageType.BYE, byeMessageUrl, "");
    }
    roomState = ConnectionState.CLOSED;
    if (wsClient != null) {
      wsClient.disconnect(true);
    }
  }

  // Callback issued when room parameters are extracted. Runs on local
  // looper thread.
  private void signalingParametersReady(final SignalingParameters params) {
    Log.d(TAG, "Room connection completed.");

    initiator = params.initiator;
    //postMessageUrl = params.roomUrl + "/message/"
    //  + params.roomId + "/" + params.clientId;
    //byeMessageUrl = params.roomUrl + "/bye/"
    //  + params.roomId + "/" + params.clientId;
    roomState = ConnectionState.CONNECTED;

    // Fire connection and signaling parameters events.
    events.onConnectedToRoom(params);

    // Connect to WebSocket server.
    wsClient.connect(
            params.wssUrl,
            params.wssPostUrl,
            params.roomId,
            params.clientId);

    // For call receiver get sdp offer and ice candidates
    // from room parameters and fire corresponding events.
/*    if (!params.initiator) {
      if (params.offerSdp != null) {
        events.onRemoteDescription(params.offerSdp);
      }
      if (params.iceCandidates != null) {
        for (IceCandidate iceCandidate : params.iceCandidates) {
          events.onRemoteIceCandidate(iceCandidate);
        }
      }
    }*/
  }

  // Send local offer SDP to the other participant.
  @Override
  public void sendOfferSdp(final SessionDescription sdp) {
    executor.execute(new Runnable() {
      @Override
      public void run() {
        if (roomState != ConnectionState.CONNECTED) {
          reportError("Sending offer SDP in non connected state.");
          return;
        }
        JSONObject json = new JSONObject();
        jsonPut(json, "sdp", sdp.description);
        jsonPut(json, "type", "offer");
        sendPostMessage(MessageType.MESSAGE, postMessageUrl, json.toString());
        if (loopback) {
          // In loopback mode rename this offer to answer and route it back.
          SessionDescription sdpAnswer = new SessionDescription(
              SessionDescription.Type.fromCanonicalForm("answer"),
              sdp.description);
          events.onRemoteDescription(sdpAnswer);
        }
      }
    });
  }

  // Send local answer SDP to the other participant.
  @Override
  public void sendAnswerSdp(final SessionDescription sdp) {
    executor.execute(new Runnable() {
      @Override
      public void run() {
        if (loopback) {
          Log.e(TAG, "Sending answer in loopback mode.");
          return;
        }
        if (wsClient.getState() != WebSocketChannelClient.WebSocketConnectionState.REGISTERED) {
          reportError("Sending answer SDP in non registered state.");
          return;
        }
        JSONObject json = new JSONObject();
        jsonPut(json, "sdp", sdp.description);
        jsonPut(json, "type", "answer");
        wsClient.send(json.toString());
      }
    });
  }

  // Send Ice candidate to the other participant.
  @Override
  public void sendLocalIceCandidate(final IceCandidate candidate) {
    executor.execute(new Runnable() {
      @Override
      public void run() {
        JSONObject json = new JSONObject();
        jsonPut(json, "type", "candidate");
        jsonPut(json, "label", candidate.sdpMLineIndex);
        jsonPut(json, "id", candidate.sdpMid);
        jsonPut(json, "candidate", candidate.sdp);
        if (initiator) {
          // Call initiator sends ice candidates to GAE server.
          if (roomState != ConnectionState.CONNECTED) {
            reportError("Sending ICE candidate in non connected state.");
            return;
          }
          sendPostMessage(MessageType.MESSAGE, postMessageUrl, json.toString());
          if (loopback) {
            events.onRemoteIceCandidate(candidate);
          }
        } else {
          // Call receiver sends ice candidates to websocket server.
          if (wsClient.getState() != WebSocketChannelClient.WebSocketConnectionState.REGISTERED) {
            reportError("Sending ICE candidate in non registered state.");
            return;
          }
          wsClient.send(json.toString());
        }
      }
    });
  }

    public boolean sendWSMessage(final String messageJSON){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (wsClient.getState() != WebSocketChannelClient.WebSocketConnectionState.REGISTERED) {
                    Log.e(TAG, "Cannot send message WebSocket in non registered state.");
                }
                try {
                    wsClient.send(messageJSON);

                } catch (Exception e) {
                    Log.e(TAG, "Could not sendWSMessage", e);
                }
            }
        });

        return true;
    }

  // --------------------------------------------------------------------
  // WebSocketChannelEvents interface implementation.
  // All events are called by WebSocketChannelClient on a local looper thread
  // (passed to WebSocket client constructor).
  @Override
  public void onWebSocketOpen() {
    Log.d(TAG, "Websocket connection completed. Registering...");
    wsClient.register();
    events.onWebSocketOpen();
  }

  @Override
  public void onWebSocketMessage(final String msg) {
    if (wsClient.getState() != WebSocketChannelClient.WebSocketConnectionState.REGISTERED) {
      Log.e(TAG, "Got WebSocket message in non registered state.");
      return;
    }
    Log.d(TAG, "Got WebSocket message: " + msg);
    try {
      JSONObject json = new JSONObject(msg);
      //String msgText = //json.getString("message");
      String errorText = json.optString("error");
      if (msg!=null && msg.length() > 0) {
        json = new JSONObject(msg);
        String type = json.optString("type");
        if (type.equals("candidate")) {
          IceCandidate candidate = new IceCandidate(
              json.getString("id"),
              json.getInt("label"),
              json.getString("candidate"));
          events.onRemoteIceCandidate(candidate);
        } else if (type.equals("answer")) {
          if (initiator) {
            SessionDescription sdp = new SessionDescription(
                SessionDescription.Type.fromCanonicalForm(type),
                json.getString("sdp"));
            events.onRemoteDescription(sdp);
          } else {
            reportError("Received answer for call initiator: " + msg);
          }
        } else if (type.equals("offer")) {
          if (!initiator) {
            SessionDescription sdp = new SessionDescription(
                SessionDescription.Type.fromCanonicalForm(type),
                json.getString("sdp"));
            events.onRemoteDescription(sdp);
          } else {
            reportError("Received offer for call receiver: " + msg);
          }
        } else if (type.equals("error")) {
            String errorCode = json.getString("code");
            String errorMsg = json.getString("message");
            Toast.makeText(TeamApp.getContext(), "Error ("+errorCode+") " + errorMsg, Toast.LENGTH_LONG);
        } else if (type.equals("presence")) {

            JSONArray usersArray = json.getJSONArray("users");
            if (usersArray!=null && usersArray.length()>0){

                for (int i=0; i<usersArray.length();i++){
                    ContactModel contact = new ContactModel();

                    JSONObject userObject = usersArray.getJSONObject(i);
                    if (!userObject.isNull("status")){
                        contact.setStatus(userObject.getString("status"));
                    }
                    JSONArray userIdArray = userObject.getJSONArray("id");
                    if (userIdArray!=null && userIdArray.length()>0){
                        for (int ii=0; ii<userIdArray.length();ii++) {
                            String userId = (String)userIdArray.get(ii);
                            if (ii==0){
                                contact.setIdTag(userId);
                            }
                        }

                        JSONArray userDevicesArray =  userObject.getJSONArray("devices");
                        if (userDevicesArray!=null && userDevicesArray.length()>0){
                            List<Device> devices = new ArrayList<Device>(userDevicesArray.length());
                            for (int ii=0; ii<userDevicesArray.length();ii++) {
                                Device device = new Device();
                                JSONObject userDeviceObject = userDevicesArray.getJSONObject(ii);
                                device.setId(userDeviceObject.getString("id"));
                                device.setDevice(userDeviceObject.getString("device"));
                                device.setLocation(userDeviceObject.getString("location"));
                                device.setStatusMessage(userDeviceObject.getString("statusMessage"));
                                devices.add(device);
                            }
                            contact.setDevices(devices);
                        }

                        ContactsStaticDataModel.addContact(contact);
                    }

                }

            }
            events.onWebSocketMessage("presence");

        } else if (type.equals("bye")) {
          events.onChannelClose();

        } else {
          events.onWebSocketMessage(msg);
          //reportError("Unexpected WebSocket message: " + msg);
        }
      } else {
        if (errorText != null && errorText.length() > 0) {
          reportError("WebSocket error message: " + errorText);
        } else {
          reportError("Unexpected WebSocket message: " + msg);
        }
      }
    } catch (JSONException e) {
      Log.e(TAG, "WebSocket message JSON parsing error", e);
      reportError("WebSocket message JSON parsing error: " + e.toString());
    }
  }

  @Override
  public void onWebSocketClose() {

      events.onChannelClose();
  }

  @Override
  public void onWebSocketError(String description) {
      events.onWebSocketError(description);
    //reportError("WebSocket error: " + description);
  }

  // --------------------------------------------------------------------
  // Helper functions.
  private void reportError(final String errorMessage) {
    Log.e(TAG, errorMessage);
    executor.execute(new Runnable() {
      @Override
      public void run() {
        if (roomState != ConnectionState.ERROR) {
          roomState = ConnectionState.ERROR;
          events.onChannelError(errorMessage);
        }
      }
    });
  }

  // Put a |key|->|value| mapping in |json|.
  private static void jsonPut(JSONObject json, String key, Object value) {
    try {
      json.put(key, value);
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

  // Send SDP or ICE candidate to a room server.
  private void sendPostMessage(
      final MessageType messageType, final String url, final String message) {
    if (messageType == MessageType.BYE) {
      Log.d(TAG, "C->GAE: " + url);
    } else {
      Log.d(TAG, "C->GAE: " + message);
    }
    AsyncHttpURLConnection httpConnection = new AsyncHttpURLConnection(
      "POST", url, message, new AsyncHttpURLConnection.AsyncHttpEvents() {
        @Override
        public void onHttpError(String errorMessage) {
          reportError("GAE POST error: " + errorMessage);
        }

        @Override
        public void onHttpComplete(String response) {
          if (messageType == MessageType.MESSAGE) {
            try {
              JSONObject roomJson = new JSONObject(response);
              String result = roomJson.getString("result");
              if (!result.equals("SUCCESS")) {
                reportError("GAE POST error: " + result);
              }
            } catch (JSONException e) {
              reportError("GAE POST JSON error: " + e.toString());
            }
          }
        }
      });
    httpConnection.send();
  }
}
