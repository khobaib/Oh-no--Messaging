package com.smartengine.ohnomessaging.receiver;

import java.util.ArrayList;
import java.util.Calendar;

import com.smartengine.ohnomessaging.BirthDayPopUpActivity;
import com.smartengine.ohnomessaging.Facebook__Login_Activity;
import com.smartengine.ohnomessaging.NewMessageActivity;
import com.smartengine.ohnomessaging.SMSPopupActivity;
import com.smartengine.ohnomessaging.dbhelper.SavedMessageDatabase;
import com.smartengine.ohnomessaging.model.Friend;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
	private Context context;
	private String[] months = { "January", "February", "March", " April",
			"May", "June", "July", "August", "September", "October",
			"November", "December" };
	ArrayList<Friend> freind; 
	ArrayList<String> list;

	@Override
	public void onReceive(Context context, Intent intent) {

		this.context = context;
		//toast("alarm Received");
		Time time = new Time();
		time.setToNow();

		SavedMessageDatabase smDatabase = new SavedMessageDatabase(context);
		list = smDatabase.getBirthDay(months[time.month], "" + (time.monthDay));
		/*for (int i = 0; i < freind.size(); i++) {
			Log.v("birtdays", freind.get(i).getName()
					+ freind.get(i).getBirthDay() + time.monthDay);
		}*/
		if(list.size()>0)
		{
		Intent popup = new Intent(context, BirthDayPopUpActivity.class);
		popup.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		popup.putStringArrayListExtra("friend",list);
		context.startActivity(popup);
		//toast(""+list.size());
		}
		

	}

	private void toast(String str) {
		Toast.makeText(context, str, Toast.LENGTH_LONG).show();
	}

}
