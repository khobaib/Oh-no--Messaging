package com.smartengine.ohnomessaging.adapter;

import java.util.List;

import com.ohnomessaging.R;

import com.smartengine.ohnomessaging.Facebook__Login_Activity;
import com.smartengine.ohnomessaging.lazylist.ImageLoader;
import com.smartengine.ohnomessaging.model.Contact;
import com.smartengine.ohnomessaging.model.Friend;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
		ImageView imageView;

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
			holder.imageView=(ImageView)convertView.findViewById(R.id.imageView1);
			convertView.setTag(holder);

		} else
			holder = (ViewHolder) convertView.getTag();
		// Log.v("msg","hello");
		Friend friend = getItem(position);
		holder.name.setText(friend.getName());
		holder.birthdyay.setText(friend.getBirthDay());
		ImageLoader imageLoader=new ImageLoader(getContext());
		Log.v("url",friend.getPicUrl());
		//holder.imageView.setImageURI(Uri.parse(friend.getPicUrl()));
		imageLoader.DisplayImage(friend.getPicUrl(),holder.imageView);

		return convertView;
	}

}
