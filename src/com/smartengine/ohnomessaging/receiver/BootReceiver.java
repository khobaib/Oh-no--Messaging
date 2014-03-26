package com.smartengine.ohnomessaging.receiver;

import java.util.Calendar;

import com.smartengine.ohnomessaging.NewMessageActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class BootReceiver extends BroadcastReceiver{

	private Context context;
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
			
			this.context=context;
			setAlarm(context);
            
        }

		
	}
	private void setAlarm(Context context) {

		if (isAlarmSet()) {
			AlarmManager alarmManager = (AlarmManager) context
					.getSystemService(Context.ALARM_SERVICE);
			Intent intent = new Intent(context,
					AlarmReceiver.class);
			PendingIntent alarmIntent = PendingIntent.getBroadcast(
					context, 0, intent, 0);
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(System.currentTimeMillis());
			calendar.set(Calendar.HOUR_OF_DAY,00);
			calendar.set(Calendar.MINUTE,02);
			alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
					calendar.getTimeInMillis(),AlarmManager.INTERVAL_DAY, alarmIntent);

			SharedPreferences.Editor editor = PreferenceManager
					.getDefaultSharedPreferences(context)
					.edit();
			editor.putString("alarm", "1");
			editor.commit();
			//toast("alarm is set now");

		}

	}

	private boolean isAlarmSet() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		String isAlarm = prefs.getString("alarm", "-1");
		if (isAlarm.equals("-1"))
			return false;
		else
			return true;

	}

	private void toast(String str) {
		Toast.makeText(context, str, Toast.LENGTH_LONG).show();
	}
	
	// test

}
