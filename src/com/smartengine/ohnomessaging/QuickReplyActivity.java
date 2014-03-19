package com.smartengine.ohnomessaging;

import java.util.ArrayList;

import com.ohnomessaging.R;
import com.smartengine.ohnomessaging.dbhelper.SavedMessageDatabase;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class QuickReplyActivity extends Activity implements OnClickListener{
	private TextView txtsendto;
	private TextView txtname;
	private EditText edtmsg;
	private Button btnsend,btnPreset;
	ImageView imageView;
	String number="";
	BroadcastReceiver sentMessageReceiver;
	private static int MAX_SMS_MESSAGE_LENGTH = 160;
	Bitmap defaultUserPic;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.quickreply);
		txtsendto=(TextView)findViewById(R.id.textcnatctnumber);
		txtname=(TextView)findViewById(R.id.textcnatctname);
		edtmsg=(EditText)findViewById(R.id.editTextmsg);
		btnsend=(Button)findViewById(R.id.buttonsend);
		btnPreset=(Button)findViewById(R.id.buttonPreset);
		imageView=(ImageView)findViewById(R.id.imageView);
		defaultUserPic = BitmapFactory.decodeResource(getResources(),
				R.drawable.ic_contact_picture2);
		
		btnPreset.setOnClickListener(this);
		btnsend.setOnClickListener(this);
		
		Intent intent=getIntent();
		number=intent.getStringExtra("number");
		txtsendto.setText("To: "+number);
		txtname.setText(intent.getStringExtra("name"));
		Log.v("msg","hello");
		imageView.setImageBitmap(getContactPhoto(intent.getLongExtra("id",-1)));
	}
	@Override
	public void onClick(View v) {
		
		if(v.getId()==R.id.buttonsend)
		{
			if(edtmsg.getText().toString().length()==0)
				toast("Empty sms body.");
			else
			{
				ContentValues values = new ContentValues();
				values.put("address", number);
				values.put("body",edtmsg.getText().toString());
				getApplicationContext().getContentResolver().insert(
						Uri.parse("content://sms/sent"), values);
				sendSMS(number,edtmsg.getText().toString());
				
			}
				
		}
		if(v.getId()==R.id.buttonPreset)
		{
			showPresetDialog();
			
		}
	}
	public void showPresetDialog()
	{
		final Dialog dialog = new Dialog(QuickReplyActivity.this);
		dialog.setContentView(R.layout.dialog_choose_preset);
		dialog.setTitle("Choose Message");
		ListView listmsg=(ListView)dialog.findViewById(R.id.listViewsavedMessages);
		SavedMessageDatabase smdatabase=new SavedMessageDatabase(QuickReplyActivity.this);
		final ArrayList<String> msgList=smdatabase.getPresetMessages();
		ArrayAdapter<String> adapter=new ArrayAdapter<String>(getApplicationContext(),R.layout.savemessage_list_row,msgList);
		listmsg.setAdapter(adapter);
		listmsg.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {

				edtmsg.setText(edtmsg.getText().toString()+" "+msgList.get(position));
				dialog.dismiss();
				
			}
		});
		dialog.show();
	}
	public Bitmap getContactPhoto(long contactId) {
		if (contactId == -1)
			return defaultUserPic;
		Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI,
				contactId);
		Uri photoUri = Uri.withAppendedPath(contactUri,
				Contacts.Photo.CONTENT_DIRECTORY);
		Cursor cursor = this.getContentResolver().query(photoUri,
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
	public void toast(String str)
	{
		Toast.makeText(getApplicationContext(),str,Toast.LENGTH_LONG).show();
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
					finish();
					
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

}
