package com.mportal.team.myteam.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mportal.team.myteam.R;
import com.mportal.team.myteam.activities.MainActivity;
import com.mportal.team.myteam.model.ContactModel;

import java.util.List;

/**
 * Created by samratsen on 2/2/15.
 */
public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

    private List<ContactModel> data;

    public ContactsAdapter(List<ContactModel> inData){

        data = inData;
    }

    public void loadData(List<ContactModel> data){
        this.data=data;
        notifyDataSetChanged();
    }

    public List<ContactModel> getData(){
        return data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.contacts_card_row, viewGroup, false);
        return new ViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        ContactModel contact = data.get(i);
        viewHolder.textViewName.setText(contact.getName() + "("+contact.getStatus() +")");
        viewHolder.textViewIdTag.setText(contact.getIdTag());
    }

    @Override
    public int getItemCount() {
        if (data==null) return 0;
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewName;
        public TextView textViewIdTag;
        public ViewHolder(View v) {
            super(v);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context ctx = v.getContext();
                    if (ctx==null) return;
                    MainActivity activity = (MainActivity)ctx;
                    activity.onContactClicked(getPosition());
                }
            });
            textViewName = (TextView)v.findViewById(R.id.text_view_contact_name);
            textViewIdTag = (TextView)v.findViewById(R.id.text_view_contact_tag);
        }
    }
}
