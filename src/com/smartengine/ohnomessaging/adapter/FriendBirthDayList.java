package com.smartengine.ohnomessaging.adapter;

import java.util.List;

import com.ohnomessaging.R;

import com.smartengine.ohnomessaging.model.Contact;
import com.smartengine.ohnomessaging.model.Friend;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FriendBirthDayList extends ArrayAdapter<Friend> {

	private Context context;

	public FriendBirthDayList(Context context, int textViewResourceId,
			List<Friend> items) {
		super(context, textViewResourceId, items);
		this.context = context;
	}

	private class ViewHolder {

		TextView name;
		TextView birthdyay;

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		LayoutInflater mInflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_view_contact_row,
					null);
			holder = new ViewHolder();
			holder.name = (TextView) convertView
					.findViewById(R.id.textViewname);
			holder.birthdyay = (TextView) convertView
					.findViewById(R.id.textViewNumber);
			convertView.setTag(holder);

		} else
			holder = (ViewHolder) convertView.getTag();
		// Log.v("msg","hello");
		Friend friend = getItem(position);
		holder.name.setText(friend.getName());
		holder.birthdyay.setText(friend.getBirthDay());

		return convertView;
	}

}
