package com.mportal.team.myteam.fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.Toast;

import com.mportal.team.myteam.R;
import com.mportal.team.myteam.adapters.MessageDetailListAdapter;
import com.mportal.team.myteam.apprtc.utils.PrefManagerBase;
import com.mportal.team.myteam.model.ContactModel;
import com.mportal.team.myteam.model.ContactsStaticDataModel;
import com.mportal.team.myteam.model.MessageModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class MessagesFragment extends Fragment implements View.OnClickListener {
    private static final String ARG_PARAM1 = "tagIdName";
    private static final String TAG = "MessagesFragment";
    private String tagIdName;

    private OnFragmentInteractionListener mListener;

    private Button sendButton;
    private TextView tvTo;

    private EditText etMessage;
    private ContactModel contact;
    private TextView tvChatemptylist;

    private MessageDetailListAdapter chatAdapter;
    private ListView listView;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param tagIdName Parameter 1.
     * @return A new instance of fragment MessagesFragment.
     */
    public static MessagesFragment newInstance(String tagIdName) {
        MessagesFragment fragment = new MessagesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, tagIdName);
        fragment.setArguments(args);
        return fragment;
    }

    public MessagesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tagIdName = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_message, container, false);
        sendButton = (Button)view.findViewById(R.id.sendChatMsg);
        sendButton.setOnClickListener(this);
        tvTo = (TextView)view.findViewById(R.id.To);
        etMessage = (EditText)view.findViewById(R.id.chatMessageArea);
        contact = ContactsStaticDataModel.getContactByIdTag(tagIdName);
        tvChatemptylist = (TextView)view.findViewById(R.id.chatemptylist);
        listView = (ListView) view.findViewById(R.id.chatlistview);
        listView.setVisibility(View.VISIBLE);
        if (chatAdapter == null) {
            chatAdapter = new MessageDetailListAdapter();
        }

        listView.setAdapter(chatAdapter);

        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
        if (contact!=null){
            String toName = (contact.getName()!=null && !contact.getName().equalsIgnoreCase("Unknown"))?contact.getName():contact.getIdTag();
            tvTo.setText("To: " + toName);
        }
    }

    public void onSendButtonPressed(String message) {
        if (mListener != null) {
            mListener.onFragmentInteraction(message);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.sendChatMsg){
            String message = etMessage.getText().toString();
            if (contact==null){
                Toast.makeText(getActivity(), "Please select a sender", Toast.LENGTH_SHORT).show();
                return;
            }
            if (message==null||message.trim().isEmpty()){
                Toast.makeText(getActivity(), "Please type a message to send", Toast.LENGTH_SHORT).show();
            } else {
                ContactModel loginUser = ContactsStaticDataModel.getLogInUser();
                String jsonStr = "{\"fragment\":\"MessagesFragment\", \"message\":{\"type\":\"message\", \"to\":[\""+contact.getIdTag()+"\"], \"from\":\""+loginUser.getIdTag()+"\", \"message\":\""+message+"\"}}";
                onSendButtonPressed(jsonStr);
                tvChatemptylist.setText("");
                drawChatBubble(message, contact.getIdTag(), "O");
                etMessage.setText("");
            }
        }
    }

    public void onNewMessageReceived(String message, String from, String to){
        Log.d(TAG, "onNewMessageReceived " + message +"," + from + "," + to);
        showReceivedMessage(message, from, to);
    }

    private void showReceivedMessage(final String message, final String from, final String to){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {


                if (contact==null) return;
                if (contact.getIdTag().equalsIgnoreCase(from)) {
                    try {
                        JSONObject jsonObject = new JSONObject(message);
                        if (jsonObject.isNull("message")) {
                            return;
                        }
                        String msg = jsonObject.getString("message");
                        drawChatBubble(msg, contact.getIdTag(), "I");

                    } catch (JSONException e){
                        Log.d(TAG, "json exception", e);
                    } catch (Exception e){
                        Log.d(TAG, "exception", e);
                    }
                }

            }
        });
    }

    /**
     *
     * @param message
     * @param to
     * @param type - I - incoming, O - outgoing
     */
    private void drawChatBubble(String message, String to, String type){
        PrefManagerBase prefMgr = new PrefManagerBase();

        MessageModel messageModel = new MessageModel();
        messageModel.setBody(message);
        messageModel.setMid(prefMgr.getNextId());
        messageModel.setContactId(to);
        messageModel.setDir(type);
        ArrayList<MessageModel> list = new ArrayList<MessageModel>();
        list.add(messageModel);
        chatAdapter.addData(list, false);

    }

    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(String message);
    }

}
