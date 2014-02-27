package com.smartengine.ohnomessaging.adapter;

import java.util.List;
import java.util.jar.Attributes.Name;

import com.ohnomessaging.R;
import com.smartengine.ohnomessaging.model.Contact;

import android.app.Activity;
import android.content.Context;
import android.provider.ContactsContract.Contacts;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ContactListadapter extends ArrayAdapter<Contact> {
	public Context context;

	public ContactListadapter(Context context,
			int textViewResourceId, List<Contact> items) {
		super(context, textViewResourceId, items);
		this.context = context;
		// TODO Auto-generated constructor stub
	}

	private class ViewHolder {

		TextView name;
		TextView phoneNumber;

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
			holder.name=(TextView)convertView.findViewById(R.id.textViewname);
			holder.phoneNumber=(TextView)convertView.findViewById(R.id.textViewNumber);
			convertView.setTag(holder);
			
		}
		else
			holder = (ViewHolder) convertView.getTag();
		//Log.v("msg","hello");
		Contact contact=getItem(position);
		holder.name.setText(contact.getDisplayName());
		holder.phoneNumber.setText(contact.getPhoneNumber());

		return convertView;

	}

}