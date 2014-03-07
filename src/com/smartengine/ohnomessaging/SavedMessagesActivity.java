package com.smartengine.ohnomessaging;

import java.util.ArrayList;

import com.ohnomessaging.R;
import com.smartengine.ohnomessaging.dbhelper.SavedMessageDatabase;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SavedMessagesActivity  extends Activity{
	private ListView listmsg;
	private ArrayList<String> msgList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.savedmessageactivity);
		listmsg=(ListView)findViewById(R.id.listViewsavedMessages);
		SavedMessageDatabase smdatabase=new SavedMessageDatabase(SavedMessagesActivity.this);
		msgList=smdatabase.getsavedMessages();
		ArrayAdapter<String> adapter=new ArrayAdapter<String>(getApplicationContext(),R.layout.savemessage_list_row,msgList);
		listmsg.setAdapter(adapter);
		
	}

}
