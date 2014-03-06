package com.smartengine.ohnomessaging.receiver;

import java.util.ArrayList;

import com.ohnomessaging.R;
import com.smartengine.ohnomessaging.InboxActivity;
import com.smartengine.ohnomessaging.NewMessageActivity;
import com.smartengine.ohnomessaging.SMSPopupActivity;
import com.smartengine.ohnomessaging.utils.SMSPopUpUtility;


import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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
		long tid=smsutil.findThreadIdFromAddress(context, address);
		popup.putExtra("tid",tid);
		//long mid=smsutil.findMessageId(context, tid,0,smsutil.getBody(context, msgs),0);
		//smsutil.setMessageRead(context, mid, 0);
		//smsutil.deleteMessage(context, mid, tid, 0);*/
		
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
	
	

	

}
