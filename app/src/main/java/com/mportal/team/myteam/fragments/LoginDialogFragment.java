package com.mportal.team.myteam.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.mportal.team.myteam.R;
import com.mportal.team.myteam.apprtc.utils.PrefManagerBase;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoginDialogFragment extends DialogFragment {

    private OnLoginDialogListener mListener;

    public interface OnLoginDialogListener {
        public void OnLoginDialogEnd(String message);
    }

    public LoginDialogFragment() {
        // Required empty public constructor
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.fragment_login_dialog, null);

        AlertDialog.Builder builder = new Builder(getActivity());
        builder.setView(dialoglayout);

        final EditText loginText = (EditText) dialoglayout.findViewById(R.id.editTextLogin);
        final EditText loginPwd = (EditText) dialoglayout.findViewById(R.id.editTextPassword);

        final Button loginButton = (Button) dialoglayout.findViewById(R.id.buttonLogin);
        final Button cnclButton = (Button) dialoglayout.findViewById(R.id.buttonCancel);

        PrefManagerBase prefMgr = new PrefManagerBase();
        if (prefMgr.getUser()!=null ){
            loginText.setText(prefMgr.getUser());
            loginButton.setText("Retry");
        }
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = "{ \"userName\"=\"" +loginText.getText().toString() +"\", \"credential\":\""+loginPwd.getText().toString()+"\"}";
                mListener.OnLoginDialogEnd(msg);
                dismiss();
            }
        });

        cnclButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                getActivity().finish();

            }
        });

        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnLoginDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnLoginDialogListener");
        }
    }

}
