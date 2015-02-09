package com.mportal.team.myteam.adapters;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.net.Uri;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.mportal.team.myteam.R;
import com.mportal.team.myteam.TeamApp;
import com.mportal.team.myteam.apprtc.utils.CircularImageView;
import com.mportal.team.myteam.model.MessageModel;


public class MessageDetailListAdapter extends BaseAdapter {

	private List<MessageModel> messages;
    private boolean isSent = false;
	public MessageDetailListAdapter() {

	}

	public void loadData(List<MessageModel> messages, boolean isSent) {
		this.isSent = isSent;
		this.messages = messages;
		notifyDataSetChanged();
		
	}

	public void addData(List<MessageModel> messages2, boolean isSent) {

		if (messages2 != null && !messages2.isEmpty()) {
			if (messages == null) {
				this.messages = messages2;
			} else {
				for (MessageModel message : messages2) {
					messages.add(message);
				}
			}
			this.isSent = isSent;
			notifyDataSetChanged();
		}
	}

	@Override
	public int getCount() {
		if (messages == null)
			return 0;
		return messages.size();
	}

	@Override
	public Object getItem(int position) {
		if (messages == null)
			return null;
		return messages.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, final ViewGroup parent) {

		final MessageModel d = (MessageModel) getItem(position);
		TextView deliveryStatus = null;
		ImageButton smsResend = null;

		if (view == null) {
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			if (d != null && d.getDir().equalsIgnoreCase("I")) {
				view = inflater.inflate(R.layout.sms_chat_from, parent, false);
			} else {
				view = inflater.inflate(R.layout.sms_chat_to, parent, false);
				deliveryStatus = (TextView) view
						.findViewById(R.id.smsDeliveryStatusText);
				smsResend = (ImageButton) view
						.findViewById(R.id.smsResendButton);
			}

		} else {
            int viewid = view.getId();
			if ((d != null) && d.getDir().equalsIgnoreCase("I")
                    && (viewid != R.layout.sms_chat_from)) {
				LayoutInflater inflater = LayoutInflater.from(parent
						.getContext());
				view = inflater.inflate(R.layout.sms_chat_from, parent, false);
			}
			if (d != null && d.getDir().equalsIgnoreCase("O")
					&& (viewid != R.layout.sms_chat_to)) {
				LayoutInflater inflater = LayoutInflater.from(parent
						.getContext());
				view = inflater.inflate(R.layout.sms_chat_to, parent, false);
				deliveryStatus = (TextView) view
						.findViewById(R.id.smsDeliveryStatusText);
				//deliveryStatus.setTypeface(FontManager.getRegular());
				smsResend = (ImageButton) view
						.findViewById(R.id.smsResendButton);

			}

		}

		// Ignoring view holder pattern for now
		// TextView name = (TextView)view.findViewById(R.id.smsName);
		TextView dateValue = (TextView) view.findViewById(R.id.smsTime);
		//dateValue.setTypeface(FontManager.getRegular());
		TextView dateTimeValue = (TextView) view.findViewById(R.id.smsDateTime);
		//dateTimeValue.setTypeface(FontManager.getRegular());
		TextView message = (TextView) view.findViewById(R.id.smsText);
		//message.setTypeface(FontManager.getRegular());
		CircularImageView contactImage = (CircularImageView) view
				.findViewById(R.id.smsProfileImage);

		if (d != null) {
			String titleText = d.getTn();

			String dateStr = "";
			String dateStr2 = "";
			// MPLogger.d("MessageListAdapter", "date " + dateStr);
			if (d.getTime() > 0) {
				try {
					if (d.getDateBreakTime() > 0) {
						dateStr2 = getDisplayDividerTime(d.getDateBreakTime());
					}
					dateStr = DateUtils.formatDateTime(
							TeamApp.getContext(), d.getTime(),
							DateUtils.FORMAT_SHOW_TIME
									| DateUtils.FORMAT_ABBREV_TIME);

				} catch (Exception e) {
					Log.d("MessageDetailListAdapter", "List Adapter Exception while parsing date", e);
				}
			}

			dateValue.setText(dateStr);
			if (dateStr2 != null) {
				dateTimeValue.setText(dateStr2);
				dateTimeValue.setVisibility(View.VISIBLE);
			} else {
				dateTimeValue.setVisibility(View.GONE);
			}
			message.setText(d.getBody());
			if (d.getContactPhotoURI() == null) {
				contactImage.setImageResource(R.drawable.icon_avatar);
			} else {
				Uri uri = Uri.parse(d.getContactPhotoURI());

				contactImage.setImageURI(uri);
			}
			if (d != null && d.getDir().equalsIgnoreCase("O")) {
				if (d.isFailed()) {
					deliveryStatus.setVisibility(View.VISIBLE);
					deliveryStatus.setText("Error:Message Not Sent");
					deliveryStatus.setTextColor(Color.parseColor("#FB543F"));
					smsResend.setVisibility(View.VISIBLE);
				} else {

					smsResend.setVisibility(View.GONE);
					deliveryStatus.setVisibility(View.VISIBLE);
					if ((position+1==messages.size())
							&& isSent) {
						deliveryStatus.setText("Delivered");
						deliveryStatus.setTextColor(Color.parseColor("#2EA0DD"));
					} else {
						deliveryStatus.setText("");
					}
					
				}
			}

		}
		return view;
	}

	private String getDisplayDividerTime(long datetime) {
		try {
			Calendar thenCal = new GregorianCalendar();
			thenCal.setTimeInMillis(datetime);
			Date thenDate = thenCal.getTime();
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"E MM/dd/yy h:m a");
			return dateFormat.format(thenDate);
		} catch (Exception e) {

		}
		return datetime + "";
	}
}
