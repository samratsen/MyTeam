package com.mportal.team.myteam.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mportal.team.myteam.R;
import com.mportal.team.myteam.TeamApp;
import com.mportal.team.myteam.adapters.ContactsAdapter;
import com.mportal.team.myteam.model.ContactModel;
import com.mportal.team.myteam.model.ContactsStaticDataModel;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ContactsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class ContactsFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private RecyclerView recList;
    private ContactsAdapter adapter;

    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        recList = (RecyclerView) view.findViewById(R.id.contactsList);
        //recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        adapter = new ContactsAdapter(ContactsStaticDataModel.getContacts());
        recList.setAdapter(adapter);
        //refreshList();

        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
        adapter.loadData(ContactsStaticDataModel.getContacts());
    }

    public synchronized void refreshList(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.loadData(ContactsStaticDataModel.getContacts());
            }
        });
    }

    // TODO: Rename method, update argument and hook method into UI event
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String message);
    }

    // Called from ContactsAdapter->MainActivity
    public void onContactClicked(int position){
        if (adapter==null) return;
        List<ContactModel> contacts = adapter.getData();
        if (contacts==null || contacts.isEmpty()) return;
        ContactModel contact = contacts.get(position);
        if (contact!=null){
            if (contact.getStatus()==null||!contact.getStatus().equalsIgnoreCase("online")){
                Toast.makeText(TeamApp.getContext(), "User is not online", Toast.LENGTH_SHORT).show();
                return;
            }
            MessagesFragment messagesFragment = MessagesFragment.newInstance(contact.getIdTag());
            getFragmentManager().beginTransaction().replace(R.id.container, messagesFragment).addToBackStack(null).commit();
        }
    }

}
