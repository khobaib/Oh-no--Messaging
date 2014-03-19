package com.smartengine.ohnomessaging;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ohnomessaging.R;
import com.smartengine.ohnomessaging.model.Contact;
import com.smartengine.ohnomessaging.model.TextMessage;
import com.smartengine.ohnomessaging.utils.Constants;
import com.smartengine.ohnomessaging.utils.SMSPopUpUtility;

public class SMSPopupActivity extends Activity implements OnClickListener {

	private TextView txtName;
	private TextView txtMsgBody;
	private TextView txtNumber;
	private ArrayList<String> smsmsg;
	ArrayList<TextMessage> smsInbox;
	private Button btnClose, btnReply, btnDelete;
	int mid = 0;
	long threadid;
	String msgBody;
	long contactId=-1;
	Bitmap defaultUserPic;
	ImageView imageview;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.smspopup);
		txtName = (TextView) findViewById(R.id.textcnatctname);
		txtNumber = (TextView) findViewById(R.id.textcnatctnumber);
		txtMsgBody = (TextView) findViewById(R.id.txtmsg);
		btnClose = (Button) findViewById(R.id.buttonclose);
		btnReply = (Button) findViewById(R.id.buttonreply);
		btnDelete = (Button) findViewById(R.id.buttondelete);
		imageview=(ImageView)findViewById(R.id.imageView);

		defaultUserPic = BitmapFactory.decodeResource(getResources(),
				R.drawable.ic_contact_picture2);

		Intent intent = getIntent();
		smsmsg = intent.getStringArrayListExtra("msg");
		txtNumber.setText(smsmsg.get(0));
		txtMsgBody.setText(smsmsg.get(1));
		setName();
		msgBody = smsmsg.get(1);
		threadid = intent.getLongExtra("tid", -1);
		btnClose.setOnClickListener(this);
		btnDelete.setOnClickListener(this);
		btnReply.setOnClickListener(this);

	}

	public void setName() {

		Cursor phones = getContentResolver().query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null,
				null, null);
		while (phones.moveToNext()) {
			String name = phones
					.getString(phones
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
			String phoneNumber = phones
					.getString(phones
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			if(smsmsg.get(0).contains(phoneNumber))
			{
				Log.v("phone",phoneNumber);
				contactId = phones.getLong(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
				txtName.setText(name);
				imageview.setImageBitmap(getContactPhoto(contactId));
				break;
			}

		}
		phones.close();


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
	@Override
	public void onClick(View v) {

		if (v.getId() == R.id.buttonclose) {
			new MarkSMSAsRead().execute();

		} else if (v.getId() == R.id.buttonreply) {
			Intent intent = new Intent(getApplicationContext(),
					QuickReplyActivity.class);
			// finish();
			new MarkSMSAsRead().execute();
			intent.putExtra("number", smsmsg.get(0));
			intent.putExtra("name",txtName.getText().toString());
			intent.putExtra("id",contactId);
			startActivity(intent);

		} else if (v.getId() == R.id.buttondelete) {
			new DeleteSMS().execute();
			finish();
		}

	}

	public void FindMsgId() {
		smsInbox = new ArrayList<TextMessage>();

		Uri uriSms = Uri.parse("content://sms");

		Cursor c = this.getContentResolver().query(
				uriSms,
				new String[] { "_id", "thread_id", "address", "date", "body",
						"type", "read" },
				"type=" + Constants.TYPE_INCOMING_MESSAGE + " OR type="
						+ Constants.TYPE_SENT_MESSAGE, null,
				"date" + " COLLATE LOCALIZED DESC");
		if (c != null && c.getCount() > 0) {
			c.moveToFirst();
			while (!c.isAfterLast()) {
				int threadId = c.getInt(c.getColumnIndexOrThrow("thread_id"));
				// Log.e(">>>>>>", "msgId = " +
				// c.getInt(c.getColumnIndexOrThrow("_id")) + " AND msgBody = "
				// + c.getString(c.getColumnIndexOrThrow("body")));
				if (!isThreadIdFound(threadId)) {
					int id = c.getInt(c.getColumnIndexOrThrow("_id"));
					String contactNumber = c.getString(c
							.getColumnIndexOrThrow("address"));
					String messageBody = c.getString(c
							.getColumnIndexOrThrow("body"));
					if (threadId == threadid && messageBody.equals(msgBody)) {
						mid = id;
						Log.v("msg", "break" + mid);
						break;
					}
					int msgType = c.getInt(c.getColumnIndexOrThrow("type"));
					String date = c.getString(c.getColumnIndexOrThrow("date"));

					// Log.e("?????????", "date =" + date);

					// initially contact-name = number to cover those contact
					// who don't have name
					// initially contactId = -1;
					TextMessage message = new TextMessage(contactNumber, null,
							-1, id, threadId, msgType, messageBody, date);
					message.setMessageCount(1);
					smsInbox.add(message);

				}
				c.moveToNext();
			}
		}

		c.close();
	}

	public void deletemsg() {
		Uri deleteUri = Uri.parse("content://sms");
		getContentResolver().delete(deleteUri, "_id=" + mid, null);
		Log.v("msg", "" + mid);

	}

	private Boolean isThreadIdFound(int threadId) {
		for (TextMessage tMsg : smsInbox) {
			if (tMsg.getThreadId() == threadId) {
				tMsg.setMessageCount(tMsg.getMessageCount() + 1);
				return true;
			}
		}
		// threadIdList.add(threadId);
		return false;
	}

	public class DeleteSMS extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			FindMsgId();
			deletemsg();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			toast("msg deleted");
		}

	}

	public void toast(String str) {
		Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG).show();
	}

	public class MarkSMSAsRead extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			FindMsgId();

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			ContentValues values = new ContentValues();
			values.put("read", true);
			getContentResolver().update(Uri.parse("content://sms/inbox"),
					values, "_id=" + mid, null);
			finish();

		}

	}
	public long getContactId(String phone) {

		long  contactId=-1;
		Cursor phones = getContentResolver().query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null,
				null, null);
		while (phones.moveToNext()) {
	
			String name = phones
					.getString(phones
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
			String phoneNumber = phones
					.getString(phones
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			if(phone.contains(phoneNumber))
			{
				contactId = phones.getLong(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
			}
			

		}
		phones.close();

		return contactId;
	}

}
