package com.smartengine.ohnomessaging;

import java.util.ArrayList;

import com.ohnomessaging.R;
import com.smartengine.ohnomessaging.adapter.ContactListadapter;
import com.smartengine.ohnomessaging.dbhelper.SavedMessageDatabase;
import com.smartengine.ohnomessaging.model.Contact;
import com.smartengine.ohnomessaging.model.TextMessage;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class CreateBlockListActivity extends Activity implements OnClickListener {
	private static final int BUTTON_POSITIVE = -1;
	private static final int BUTTON_NEGATIVE = -2;
	

	private ListView lv_BlockList;
	private ArrayList<Contact> blockList;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_blocklist);
	
		lv_BlockList=(ListView)findViewById(R.id.listViewBockList);
		setAdapter();
		lv_BlockList.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View v,
					int position, long id) {
				showDeleteDialog(blockList.get(position).getPhoneNumber());
				
				return false;
			}
		});
		

	}
	private void showDeleteDialog(String number) {

		final String num=number;
		AlertDialog Alert = new AlertDialog.Builder(CreateBlockListActivity.this)
				.create();
		Alert.setTitle("Delete");
		Alert.setMessage("Want to delete this text thread?");

		Alert.setButton(BUTTON_POSITIVE, "Delete",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						SavedMessageDatabase smDatabase=new SavedMessageDatabase(CreateBlockListActivity.this);
						smDatabase.deleteEntryFromBlockList(num);
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
	public void setAdapter() {
		SavedMessageDatabase smdatabase = new SavedMessageDatabase(
				CreateBlockListActivity.this);
		blockList = smdatabase.getBlockedList();
		 ContactListadapter adapter = new ContactListadapter(
					getApplicationContext(), R.layout.list_view_contact_row,
					blockList);
		lv_BlockList.setAdapter(adapter);
	}
	@Override
	public void onClick(View v) {
		if(v.getId()==R.id.btnaddnew)
		{
			showDialog();
		}
		
	}
	public void showDialog() {
		final Dialog dialog = new Dialog(CreateBlockListActivity.this);
		dialog.setContentView(R.layout.list_view_contact);
		dialog.setTitle("Pic Contact");
		ListView listView = (ListView) dialog.findViewById(R.id.lc_contacts);
		final ArrayList<Contact> contactList = getContacts();
		Log.v("msg",""+contactList.size());
		 ContactListadapter adapter = new ContactListadapter(
				getApplicationContext(), R.layout.list_view_contact_row,
				contactList);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				
				SavedMessageDatabase smDatabase=new SavedMessageDatabase(CreateBlockListActivity.this);
				smDatabase.insertItemToBlockList(contactList.get(position).getDisplayName(),contactList.get(position).getPhoneNumber());
				toast("Added to blocklist");
				dialog.dismiss();
				setAdapter();
				
			}
			
		});
		dialog.show();
		/*DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		dialog.getWindow().setLayout(metrics.heightPixels, metrics.widthPixels);*/
	}
	public ArrayList<Contact> getContacts() {

		ArrayList<Contact> contactList = new ArrayList<Contact>();
		Cursor phones = getContentResolver().query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null,
				null, null);
		while (phones.moveToNext()) {
			Contact contact = new Contact();
			String name = phones
					.getString(phones
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
			String phoneNumber = phones
					.getString(phones
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			// Log.v("msg",name+phoneNumber);
			contact.setDisplayName(name);
			contact.setPhoneNumber(phoneNumber);
			contactList.add(contact);

		}
		phones.close();

		return contactList;
	}
	public void toast(String str)
	{
		Toast.makeText(getApplicationContext(), str,Toast.LENGTH_LONG).show();
	}


}
