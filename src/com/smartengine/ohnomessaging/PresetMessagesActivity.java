package com.smartengine.ohnomessaging;

import java.util.ArrayList;

import com.ohnomessaging.R;

import com.smartengine.ohnomessaging.dbhelper.SavedMessageDatabase;

import android.app.Activity;
import android.app.Dialog;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class PresetMessagesActivity extends Activity implements OnClickListener {
	private ListView listpresetmsg;
	private Button btnAddnew;
	private ArrayList<String> msgList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preset_message);
		listpresetmsg = (ListView) findViewById(R.id.listViewsavedMessages);
		btnAddnew = (Button) findViewById(R.id.btnaddnew);
		setAdapter();
		btnAddnew.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btnaddnew) {
			showPresetMsgDialog();

		}

	}

	public void showPresetMsgDialog() {
		final Dialog dialog = new Dialog(PresetMessagesActivity.this);
		dialog.setContentView(R.layout.add_new_preset);
		dialog.setTitle("Add Preset");
		final EditText edtmsg = (EditText) dialog
				.findViewById(R.id.editTextmsg);
		Button btnadd = (Button) dialog.findViewById(R.id.buttonadd);
		btnadd.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SavedMessageDatabase smdatabase = new SavedMessageDatabase(
						PresetMessagesActivity.this);
				smdatabase.insertPresetmessage(edtmsg.getText().toString());
				Toast.makeText(getApplicationContext(), "Preset Saved",
						Toast.LENGTH_LONG).show();
				dialog.dismiss();
				setAdapter();

			}
		});

		dialog.show();

	}

	public void setAdapter() {
		SavedMessageDatabase smdatabase = new SavedMessageDatabase(
				PresetMessagesActivity.this);
		msgList = smdatabase.getPresetMessages();
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				getApplicationContext(), R.layout.savemessage_list_row, msgList);
		listpresetmsg.setAdapter(adapter);
	}

}
