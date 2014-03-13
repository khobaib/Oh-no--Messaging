package com.smartengine.ohnomessaging;

import java.util.ArrayList;

import com.ohnomessaging.R;
import com.smartengine.ohnomessaging.dbhelper.SavedMessageDatabase;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SavedMessagesActivity  extends Activity{
	private static final int BUTTON_POSITIVE = -1;
	private static final int BUTTON_NEGATIVE = -2;
	private ListView listmsg;
	private ArrayList<String> msgList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.savedmessageactivity);
		listmsg=(ListView)findViewById(R.id.listViewsavedMessages);
		setAadapter();
		listmsg.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View v,
					int position, long id) {
				showDeleteDialog(msgList.get(position));
				
				return false;
			}
		});
	}

	private void showDeleteDialog(final String number) {

		final String num=number;
		AlertDialog Alert = new AlertDialog.Builder(SavedMessagesActivity.this)
				.create();
		Alert.setTitle("Delete");
		Alert.setMessage("Want to Revome this text thread?");

		Alert.setButton(BUTTON_POSITIVE, "Remove",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						SavedMessageDatabase smDatabase=new SavedMessageDatabase(SavedMessagesActivity.this);
						smDatabase.deleteSavedMessage(number);
						setAadapter();
						dialog.cancel();

						
					}
				});

		Alert.setButton(BUTTON_NEGATIVE, "Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.cancel();

					}
				});
		Alert.show();
	}
	public void setAadapter()
	{
		SavedMessageDatabase smdatabase=new SavedMessageDatabase(SavedMessagesActivity.this);
		msgList=smdatabase.getsavedMessages();
		ArrayAdapter<String> adapter=new ArrayAdapter<String>(getApplicationContext(),R.layout.savemessage_list_row,msgList);
		listmsg.setAdapter(adapter);
	}
}
