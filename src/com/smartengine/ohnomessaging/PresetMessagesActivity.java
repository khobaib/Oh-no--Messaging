package com.smartengine.ohnomessaging;

import java.util.ArrayList;

import com.ohnomessaging.R;

import com.smartengine.ohnomessaging.dbhelper.SavedMessageDatabase;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class PresetMessagesActivity extends Activity implements OnClickListener {
	private ListView listpresetmsg;
	private Button btnAddnew;
	private ArrayList<String> msgList;
	private static final int BUTTON_POSITIVE = -1;
	private static final int BUTTON_NEGATIVE = -2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preset_message);
		listpresetmsg = (ListView) findViewById(R.id.listViewsavedMessages);
		btnAddnew = (Button) findViewById(R.id.btnaddnew);
		setAdapter();
		btnAddnew.setOnClickListener(this);
		listpresetmsg.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View v,
					int position, long id) {
				showDeleteDialog(msgList.get(position));
				return false;
			}
		});
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btnaddnew) {
			showPresetMsgDialog();

		}

	}
	private void showDeleteDialog(final String number) {

		final String num=number;
		AlertDialog Alert = new AlertDialog.Builder(PresetMessagesActivity.this)
				.create();
		Alert.setTitle("Delete");
		Alert.setMessage("Want to Revome this text msg?");

		Alert.setButton(BUTTON_POSITIVE, "Remove",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						SavedMessageDatabase smDatabase=new SavedMessageDatabase(PresetMessagesActivity.this);
						smDatabase.deletePresetMessage(num);
						setAdapter();
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
