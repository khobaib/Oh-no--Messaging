package com.smartengine.ohnomessaging;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.ohnomessaging.R;
import com.smartengine.ohnomessaging.model.TextMessage;
import com.smartengine.ohnomessaging.utils.Constants;
import com.smartengine.ohnomessaging.utils.SMSPopUpUtility;

public class SMSPopupActivity extends Activity implements OnClickListener{
	
	private TextView txtName;
	private TextView txtMsgBody;
	private ArrayList<String> smsmsg;
	ArrayList<TextMessage> smsInbox;
	private Button btnClose,btnReply,btnDelete;
	int mid=0;
	long threadid;
	String msgBody;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.smspopup);
		txtName=(TextView)findViewById(R.id.textcnatctname);
		txtMsgBody=(TextView)findViewById(R.id.txtmsg);
		btnClose=(Button)findViewById(R.id.buttonclose);
		btnReply=(Button)findViewById(R.id.buttonreply);
		btnDelete=(Button)findViewById(R.id.buttondelete);
		Intent intent=getIntent();
		smsmsg=intent.getStringArrayListExtra("msg");
		txtName.setText(smsmsg.get(0));
		txtMsgBody.setText(smsmsg.get(1));
		msgBody=smsmsg.get(1);
		threadid= intent.getLongExtra("tid",-1);
		btnDelete.setOnClickListener(this);
		//SMSPopUpUtility smsutil=new SMSPopUpUtility();
		//smsutil.SmsMmsMessage(context, msgs, 0);
		//String address=smsutil.getAddress(context,msgs);
		//long tid=smsutil.findThreadIdFromAddress(context, address);
		//long mid=smsutil.findMessageId(this, 30,0,"s",0);
		//smsutil.setMessageRead(context, mid, 0);
		//smsutil.deleteMessage(this, mid,30, 0);
			//getList();
		
		
		
	}

	@Override
	public void onClick(View v) {
		
		if(v.getId()==R.id.buttonclose){
			finish();
		}
		else if(v.getId()==R.id.buttonreply)
		{
			
		}
		else if(v.getId()==R.id.buttondelete)
		{
			getList();
			finish();
		}
		
	}
	public void getList()
	{
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
					if(threadId==threadid && messageBody.equals(msgBody))
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

					// Uri lookupUri =
					// Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
					// Uri.encode(contactNumber));
					// Cursor nameCursor = getContentResolver().query(lookupUri,
					// new String[]{PhoneLookup.DISPLAY_NAME,
					// PhoneLookup._ID},null,null,null);
					// try {
					// nameCursor.moveToFirst();
					// String displayName =
					// nameCursor.getString(nameCursor.getColumnIndexOrThrow(PhoneLookup.DISPLAY_NAME));
					// int contactId =
					// nameCursor.getInt(nameCursor.getColumnIndexOrThrow(PhoneLookup._ID));
					// Log.e(">>>>>", "contact id for " + contactNumber +
					// " is = " + contactId);
					// message.setContactName(displayName);
					// message.setContactId(contactId);
					//
					// } catch (Exception e) {
					// }finally{
					// nameCursor.close();
					// }
				}
				c.moveToNext();
			}
		}
		 Uri deleteUri = Uri.parse("content://sms");
         getContentResolver().delete(deleteUri, "_id=" + mid, null);
         Log.v("msg",""+mid);
		c.close();
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

		


}
