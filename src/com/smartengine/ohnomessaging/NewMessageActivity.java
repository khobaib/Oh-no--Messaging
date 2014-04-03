package com.smartengine.ohnomessaging;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import android.R.integer;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.telephony.SmsManager;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;
import com.ohnomessaging.R;
import com.smartengine.ohnomessaging.adapter.ContactListadapter;
import com.smartengine.ohnomessaging.adapter.ContactsAdapter;
import com.smartengine.ohnomessaging.comparator.SortContactsByName;
import com.smartengine.ohnomessaging.dbhelper.SavedMessageDatabase;
import com.smartengine.ohnomessaging.model.Contact;
import com.smartengine.ohnomessaging.model.Friend;
import com.smartengine.ohnomessaging.receiver.AlarmReceiver;
import com.smartengine.ohnomessaging.utils.Constants;
import com.smartengine.ohnomessaging.utils.Utility;
import com.smartengine.ohnomessaging.view.ContactsCompletionView;
import com.smartengine.ohnomessaging.view.TokenCompleteTextView;

public class NewMessageActivity extends Activity implements
		TokenCompleteTextView.TokenListener {

	private static int MAX_SMS_MESSAGE_LENGTH = 160;

	RelativeLayout rlSendTo;
	TextView tvSendTo;
	ImageView ivSendTo;

	// ContactsEditText contactNos;

	ContactsCompletionView completionView;
	ContactsAdapter cAdapter;
	EditText MessageBody;

	// String strContacts;
	// List<String> phoneNumbers;

	Bitmap defaultUserPic;

	int fromActivity;

	BroadcastReceiver sentMessageReceiver;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		BugSenseHandler.initAndStartSession(this, Constants.BUGSENSE_API_KEY);

		setContentView(R.layout.activity_new_message);

		// strContacts = null;
		// phoneNumbers = new ArrayList<String>();
		// setAlarm();
		
		defaultUserPic = BitmapFactory.decodeResource(getResources(),
				R.drawable.ic_contact_picture2);

		cAdapter = new ContactsAdapter(this);

		completionView = (ContactsCompletionView) findViewById(R.id.searchView);
		completionView.setAdapter(cAdapter);
		completionView.setTokenListener(this);

		if (savedInstanceState == null) {
			completionView.setPrefix("");
			// completionView.addObject(people[0]);
			// completionView.addObject(people[1]);
		}

		// contactNos = (ContactsEditText) findViewById(R.id.act_to);
		MessageBody = (EditText) findViewById(R.id.et_msg_body);
		
		setSavedMessage();

		tvSendTo = (TextView) findViewById(R.id.tv_send_to);
		ivSendTo = (ImageView) findViewById(R.id.iv_send_to);

		rlSendTo = (RelativeLayout) findViewById(R.id.rl_send_to);
		rlSendTo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// String sendTo = contactNos.getText().toString().trim();
				String msgBody = MessageBody.getText().toString().trim();

				if (completionView.getObjects().size() == 0)
					Toast.makeText(NewMessageActivity.this,
							"No contact is selected.", Toast.LENGTH_SHORT)
							.show();
				else if (msgBody == null || msgBody.equals(""))
					Toast.makeText(NewMessageActivity.this,
							"Message is empty.", Toast.LENGTH_SHORT).show();
				else {
					for (Object obj : completionView.getObjects()) {
						Contact contact = (Contact) obj;
						String sendTo = contact.getPhoneNumber();

						ContentValues values = new ContentValues();
						values.put("address", sendTo);
						values.put("body", msgBody);
						getApplicationContext().getContentResolver().insert(
								Uri.parse("content://sms/sent"), values);

						sendSMS(sendTo, msgBody);
					}
				}
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		BugSenseHandler.startSession(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		BugSenseHandler.closeSession(this);
	}

	private void sendSMS(final String phoneNumber, final String message) {
		String SENT = "SMS_SENT";
		// String DELIVERED = "SMS_DELIVERED";

		PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(
				SENT), 0);

		// PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new
		// Intent(DELIVERED), 0);

		sentMessageReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					Toast.makeText(getBaseContext(), "SMS sent",
							Toast.LENGTH_SHORT).show();

					// ContentValues values = new ContentValues();
					// values.put("address", phoneNumber);
					// values.put("body", message);
					// getApplicationContext().getContentResolver().insert(Uri.parse("content://sms/sent"),
					// values);

					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					Toast.makeText(getBaseContext(), "Generic failure",
							Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
					Toast.makeText(getBaseContext(), "No service",
							Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_NULL_PDU:
					Toast.makeText(getBaseContext(), "Null PDU",
							Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					Toast.makeText(getBaseContext(), "Radio off",
							Toast.LENGTH_SHORT).show();
					break;
				}

				try {
					unregisterReceiver(sentMessageReceiver);
					NewMessageActivity.this.finish();
				} catch (Exception e) {
					Log.e("", "exception in unregistering broadcastreceiver");
				}
			}
		};

		// ---when the SMS has been sent---
		registerReceiver(sentMessageReceiver, new IntentFilter(SENT));

		SmsManager sms = SmsManager.getDefault();

		int length = message.length();
		if (length > MAX_SMS_MESSAGE_LENGTH) {
			ArrayList<String> messagelist = sms.divideMessage(message);

			ArrayList<PendingIntent> sendPIs = new ArrayList<PendingIntent>();
			sendPIs.add(sentPI);
			int msgCount = messagelist.size();
			for (int i = 1; i < msgCount; i++) {
				sendPIs.add(null);
			}

			sms.sendMultipartTextMessage(phoneNumber, null, messagelist,
					sendPIs, null);
		} else
			sms.sendTextMessage(phoneNumber, null, message, sentPI, null);
	}

	public void onClickInbox(View v) {
		Intent i = new Intent(NewMessageActivity.this, InboxActivity.class);
		startActivity(i);
		finish();
	}

	private void updateUI() {
		String strContacts = null;
		if (completionView.getObjects().size() == 0) {
			strContacts = "Send";
			ivSendTo.setImageBitmap(defaultUserPic);
			tvSendTo.setVisibility(View.INVISIBLE);
		} else if (completionView.getObjects().size() == 1) {
			strContacts = ""
					+ ((Contact) completionView.getObjects().get(0))
							.getDisplayName();
			ivSendTo.setVisibility(View.VISIBLE);
			ivSendTo.setImageBitmap(((Contact) completionView.getObjects().get(
					0)).getImage());
			tvSendTo.setVisibility(View.VISIBLE);
		} else if (completionView.getObjects().size() > 1) {
			ivSendTo.setVisibility(View.GONE);
			strContacts = "";
			for (Object obj : completionView.getObjects()) {
				Contact contact = (Contact) obj;
				strContacts = strContacts + contact.getDisplayName() + ", ";
			}
			strContacts = Utility.trimLastComma(strContacts);
			tvSendTo.setVisibility(View.VISIBLE);
		}

		tvSendTo.setText(strContacts);
	}

	private void updateTokenConfirmation() {
		updateUI();
	}

	@Override
	public void onTokenAdded(Object token) {
		// ((TextView)findViewById(R.id.lastEvent)).setText("Added: " + token);
		Log.e("onTokenAdded", "Token Added");
		updateTokenConfirmation();
	}

	public void showDialog() {
		final Dialog dialog = new Dialog(NewMessageActivity.this);
		dialog.setContentView(R.layout.list_view_contact);
		dialog.setTitle("Pic Contact");
		ListView listView = (ListView) dialog.findViewById(R.id.lc_contacts);
		AutoCompleteTextView atv = (AutoCompleteTextView) dialog
				.findViewById(R.id.autoCompleteTextViewSearch);
		final ArrayList<Contact> contactList = getContacts();
		Collections.sort(contactList, new SortContactsByName());
		ContactListadapter adapter = new ContactListadapter(
				getApplicationContext(), R.layout.list_view_contact_row,
				contactList);

		listView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		atv.setAdapter(adapter);
		atv.setThreshold(1);
		atv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				TextView tv = (TextView) parent.findViewById(R.id.textViewname);
				TextView tvNumber = (TextView) parent
						.findViewById(R.id.textViewNumber);
				String text = MessageBody.getText().toString();
				text = text + "\n" + "Name: " + tv.getText().toString()
						+ "\n phone Number:" + tvNumber.getText().toString();
				MessageBody.setText(text);
				dialog.dismiss();

			}
		});
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String text = MessageBody.getText().toString();
				text = text + "\n" + "Name: "
						+ contactList.get(position).getDisplayName()
						+ "\n phone Number:"
						+ contactList.get(position).getPhoneNumber();
				MessageBody.setText(text);
				dialog.dismiss();

			}

		});
		dialog.show();
		/*
		 * DisplayMetrics metrics = new DisplayMetrics();
		 * getWindowManager().getDefaultDisplay().getMetrics(metrics);
		 * 
		 * dialog.getWindow().setLayout(metrics.heightPixels,
		 * metrics.widthPixels);
		 */
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
			long contactId = phones.getLong(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
			contact.setId(contactId);
			// String
			// avatar=phones.getColumnIndex(ContactsContract.CommonDataKinds.Photo.);
			//contact.setImage(getContactPhoto(contactId));
			// Log.v("msg",name+phoneNumber);
			contact.setDisplayName(name);
			contact.setPhoneNumber(phoneNumber);
			contactList.add(contact);

		}
		phones.close();

		return contactList;
	}

	@Override
	public void onTokenRemoved(Object token) {
		Log.e("onTokenAdded", "Token removed");
		// ((TextView)findViewById(R.id.lastEvent)).setText("Removed: " +
		// token);
		updateTokenConfirmation();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == R.id.insert_contact) {
			//showDialog();
			new GetContacts().execute();

		} else if (item.getItemId() == R.id.action_settings) {
			Intent intent = new Intent(getApplicationContext(),
					SettingsActivity.class);
			startActivity(intent);
		} else if (item.getItemId() == R.id.saved_messages) {
			Intent intent = new Intent(getApplicationContext(),
					SavedMessagesActivity.class);
			startActivity(intent);

		} else if (item.getItemId() == R.id.preset_messages) {
			Intent intent = new Intent(getApplicationContext(),
					PresetMessagesActivity.class);
			startActivity(intent);

		} else if (item.getItemId() == R.id.create_blocklist) {
			Intent intent = new Intent(getApplicationContext(),
					CreateBlockListActivity.class);
			startActivity(intent);

		} else if (item.getItemId() == R.id.fb_frined_birthdays) {
			Intent intent = new Intent(getApplicationContext(),
					Facebook__Login_Activity.class);
			startActivity(intent);

		}
		 else if (item.getItemId() == R.id.inbox) {
			 Intent i = new Intent(NewMessageActivity.this, InboxActivity.class);
				startActivity(i);
				finish();

			}

		return super.onOptionsItemSelected(item);
	}

	private void toast(String str) {
		Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG).show();
	}

	// test
	public Bitmap getContactPhoto(long contactId) {
		if (contactId == -1)
			return defaultUserPic;
		Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI,
				contactId);
		Uri photoUri = Uri.withAppendedPath(contactUri,
				Contacts.Photo.CONTENT_DIRECTORY);
		Cursor cursor = getContentResolver().query(photoUri,
				new String[] { Contacts.Photo.PHOTO }, null, null, null);
		if (cursor == null) {
			return null;
		}
		try {
			Bitmap thumbnail = defaultUserPic;
			if (cursor.moveToFirst()) {
				byte[] data = cursor.getBlob(0);
				if (data != null) {
					thumbnail = BitmapFactory.decodeByteArray(data, 0,
							data.length);
				}
			}
			return thumbnail;
		} finally {
			cursor.close();
		}
	}
	public class GetContacts extends AsyncTask<Void,Void,Void>
	{
		ArrayList<Contact> contactList;
		ProgressDialog pd ;
		@Override
		protected void onPreExecute() {
		  pd = new ProgressDialog(
					NewMessageActivity.this);
		  pd.setMessage("Getting Contacts");
		  pd.show();
			super.onPreExecute();
		}
		@Override
		protected Void doInBackground(Void... arg0) {
			
			
			contactList = getContacts();
			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			pd.dismiss();
			showDialog2();
			super.onPostExecute(result);
		}
		public void showDialog2() {
			final Dialog dialog = new Dialog(NewMessageActivity.this);
			dialog.setContentView(R.layout.list_view_contact);
			dialog.setTitle("Pic Contact");
			ListView listView = (ListView) dialog.findViewById(R.id.lc_contacts);
			AutoCompleteTextView atv = (AutoCompleteTextView) dialog
					.findViewById(R.id.autoCompleteTextViewSearch);
			Collections.sort(contactList, new SortContactsByName());
			ContactListadapter adapter = new ContactListadapter(
					getApplicationContext(), R.layout.list_view_contact_row,
					contactList);

			listView.setAdapter(adapter);
			adapter.notifyDataSetChanged();
			atv.setAdapter(adapter);
			atv.setThreshold(1);
			atv.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View v,
						int position, long id) {
					TextView tv = (TextView) parent.findViewById(R.id.textViewname);
					TextView tvNumber = (TextView) parent
							.findViewById(R.id.textViewNumber);
					String text = MessageBody.getText().toString();
					text = text + "\n" + "Name: " + tv.getText().toString()
							+ "\n phone Number:" + tvNumber.getText().toString();
					MessageBody.setText(text);
					dialog.dismiss();

				}
			});
			listView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					String text = MessageBody.getText().toString();
					text = text + "\n" + "Name: "
							+ contactList.get(position).getDisplayName()
							+ "\n phone Number:"
							+ contactList.get(position).getPhoneNumber();
					MessageBody.setText(text);
					dialog.dismiss();

				}

			});
			dialog.show();
			
		}
		
	}
	public void setSavedMessage()
	{
	
		
		Intent intent=getIntent();
		long flag=intent.getLongExtra("flag",0);
		if(flag==1)
			MessageBody.setText(intent.getStringExtra("msg"));
		
	}
}
