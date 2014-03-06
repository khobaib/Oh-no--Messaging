package com.smartengine.ohnomessaging.receiver;

import com.ohnomessaging.R;
import com.smartengine.ohnomessaging.InboxActivity;
import com.smartengine.ohnomessaging.NewMessageActivity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.TextView;
import android.widget.Toast;

public class SMSReceiver extends BroadcastReceiver{
	Context context;

	@Override
	public void onReceive(Context context, Intent intent) {
		Toast.makeText(context,"sms received",Toast.LENGTH_LONG).show();
		this.context=context;
		//showPopUp(intent);
		Intent popup=new Intent(context, NewMessageActivity.class);
		 popup.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		context.startActivity(popup);
		
	}
	public void showPopUp(Intent i)
	{
		final Dialog dialog = new Dialog(context);
		dialog.setContentView(R.layout.smspopup);
		dialog.setTitle("Sms Received");
		SmsMessage[] msgs=getMessageFromIntent(i);
		TextView txtname=(TextView)dialog.findViewById(R.id.textViewName);
		TextView txtmsg=(TextView)dialog.findViewById(R.id.txtmsg);
		txtname.setText(msgs[0].getOriginatingAddress());
		txtmsg.setText(msgs[0].getMessageBody());
		//dialog.show();
		
		
	}
	public SmsMessage[] getMessageFromIntent(Intent intent)
	{
		Bundle bundle=intent.getExtras();
		if(bundle!=null)
		{
			 Object[] pdus = (Object[])bundle.get("pdus");
              SmsMessage[] messages = new SmsMessage[pdus.length];
             for (int i = 0; i < pdus.length; i++) 
             {
                 messages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                 
             }
             return messages;
		}
		else
			return null;
			
		
	}

}
