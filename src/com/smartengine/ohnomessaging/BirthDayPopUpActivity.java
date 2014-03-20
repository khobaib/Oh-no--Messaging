package com.smartengine.ohnomessaging;

import java.util.ArrayList;

import com.ohnomessaging.R;
import com.smartengine.ohnomessaging.adapter.FriendBirthDayList;
import com.smartengine.ohnomessaging.model.Friend;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class BirthDayPopUpActivity extends Activity {
	private ListView listView;
	ArrayList<Friend> friends=new ArrayList<Friend>();
	ArrayList<String> list;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_view_contact);
		listView = (ListView) findViewById(R.id.lc_contacts);
		Intent intent = getIntent();
		list = intent.getStringArrayListExtra("friend");
		for (int i = 0; i < list.size(); i++) {
			String[] str = list.get(i).split(",");
			friends.add(new Friend(str[0], "", str[1],""));
			Log.v("msg",list.get(i));
		}

		FriendBirthDayList adapter = new FriendBirthDayList(
				this, R.layout.list_view_contact_row,
				friends);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent=new Intent(getApplicationContext(),NewMessageActivity.class);
				startActivity(getIntent());

			}

		});
	}

}
