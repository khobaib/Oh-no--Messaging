package com.smartengine.ohnomessaging.receiver;

import java.util.ArrayList;

import com.ohnomessaging.R;
import com.smartengine.ohnomessaging.InboxActivity;
import com.smartengine.ohnomessaging.NewMessageActivity;
import com.smartengine.ohnomessaging.SMSPopupActivity;
import com.smartengine.ohnomessaging.dbhelper.SavedMessageDatabase;
import com.smartengine.ohnomessaging.model.Contact;
import com.smartengine.ohnomessaging.model.TextMessage;
import com.smartengine.ohnomessaging.utils.Constants;
import com.smartengine.ohnomessaging.utils.SMSPopUpUtility;


import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class SMSReceiver extends BroadcastReceiver {
	Context context;
	String number;
	String body;
	String time;
	long tid;
	int mid;
	public ArrayList<TextMessage> smsInbox = new ArrayList<TextMessage>();
	private ArrayList<String> msg=new ArrayList<String>();

	@Override
	public void onReceive(Context context, Intent intent) {
		Toast.makeText(context, "sms received", Toast.LENGTH_LONG).show();
		this.context = context;
		// showPopUp(intent);
		Intent popup = new Intent(context, SMSPopupActivity.class);
		popup.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		SmsMessage[] msgs = getMessageFromIntent(intent);
		
		number = msgs[0].getOriginatingAddress();
		time = msgs[0].getTimestampMillis() + "";
		body= msgs[0].getMessageBody().toString();
		msg.add(msgs[0].getOriginatingAddress());
		msg.add(msgs[0].getMessageBody());
		popup.putStringArrayListExtra("msg",msg);
		SMSPopUpUtility smsutil=new SMSPopUpUtility();
		//smsutil.SmsMmsMessage(context, msgs, 0);
		String address=smsutil.getAddress(context,msgs);
		tid=smsutil.findThreadIdFromAddress(context, address);
		popup.putExtra("tid",tid);
		if(isInBlockList(number))
		{
			//new DeleteSMS().execute();
			new ThreadDeleteSMS().run();
			toast("Is In BlockList");
		}
		else
			context.startActivity(popup);
	

	}

	public void showPopUp(Intent i) {
		final Dialog dialog = new Dialog(context);
		dialog.setContentView(R.layout.smspopup);
		dialog.setTitle("Sms Received");
		SmsMessage[] msgs = getMessageFromIntent(i);
		TextView txtname = (TextView) dialog.findViewById(R.id.textcnatctname);
		TextView txtmsg = (TextView) dialog.findViewById(R.id.txtmsg);
		txtname.setText(msgs[0].getOriginatingAddress());
		txtmsg.setText(msgs[0].getMessageBody());
		dialog.show();

	}

	public SmsMessage[] getMessageFromIntent(Intent intent) {
		Bundle bundle = intent.getExtras();
		if (bundle != null) {
			Object[] pdus = (Object[]) bundle.get("pdus");
			SmsMessage[] messages = new SmsMessage[pdus.length];
			for (int i = 0; i < pdus.length; i++) {
				messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);

			}
			return messages;
		} else
			return null;

	}
	public boolean isInBlockList(String number)
	{
		ArrayList<Contact> list=new ArrayList<Contact>();
		SavedMessageDatabase smDatabase=new SavedMessageDatabase(context);
		list=smDatabase.getBlockedList();
		for(int i=0;i<list.size();i++)
		{
			if(list.get(i).getPhoneNumber().equals(number) )
			{
			
				return true;
				
			}
		}
		
		return false;
		
	}
	public class DeleteSMS extends AsyncTask<Void,Void,Void>
	{

		
		@Override
		protected Void doInBackground(Void... params) {
			try {
				wait(2000);
				FindMsgId();
				deletemsg();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			toast("msg deleted");
		}
		
	}
	public void FindMsgId()
	{
		

		Uri uriSms = Uri.parse("content://sms");
		Log.v("msg","here");
		Cursor c = context.getContentResolver().query(
				uriSms,
				new String[] { "_id", "thread_id", "address", "date", "body",
						"type", "read" },
				"type=" + Constants.TYPE_INCOMING_MESSAGE + " OR type="
						+ Constants.TYPE_SENT_MESSAGE, null,
				"date" + " COLLATE LOCALIZED DESC");
		Log.v("msg","after cursor");
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
					if(threadId==tid && messageBody.equals(body))
					{
						mid=id;
						Log.v("msg","break"+mid);
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
	public void  deletemsg()
	{
		Uri deleteUri = Uri.parse("content://sms");
        context.getContentResolver().delete(deleteUri, "_id=" + mid, null);
        Log.v("msg",""+mid);
		
	}
	private Boolean isThreadIdFound(int threadId) {
		Log.v("msg","thread");
		for (TextMessage tMsg : smsInbox) {
			if (tMsg.getThreadId() == threadId) {
				tMsg.setMessageCount(tMsg.getMessageCount() + 1);
				return true;
			}
		}
		// threadIdList.add(threadId);
		return false;
	}
	
	public void toast(String str)
	{
		Toast.makeText(context.getApplicationContext(),str,Toast.LENGTH_LONG).show();
	}
	public class ThreadDeleteSMS implements Runnable
	{

		@Override
		public void run() {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			FindMsgId();
			deletemsg();
			
		}
		
	}

}
