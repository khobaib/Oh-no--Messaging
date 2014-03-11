package com.smartengine.ohnomessaging.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
	private Context context;

	@Override
	public void onReceive(Context context, Intent intent) {
		
		this.context=context;
		toast("alarm Received");
	}
	private void toast(String str)
	{
		Toast.makeText(context,str,Toast.LENGTH_LONG).show();
	}

}

