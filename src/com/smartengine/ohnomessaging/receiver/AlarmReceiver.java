package com.smartengine.ohnomessaging.receiver;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.HttpMethod;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionLoginBehavior;
import com.facebook.Session.OpenRequest;
import com.facebook.model.GraphObject;
import com.smartengine.ohnomessaging.BirthDayPopUpActivity;
import com.smartengine.ohnomessaging.Facebook__Login_Activity;
import com.smartengine.ohnomessaging.NewMessageActivity;
import com.smartengine.ohnomessaging.SMSPopupActivity;
import com.smartengine.ohnomessaging.dbhelper.SavedMessageDatabase;
import com.smartengine.ohnomessaging.model.Friend;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
	private Context context;
	private String[] months = { "January", "February", "March", " April",
			"May", "June", "July", "August", "September", "October",
			"November", "December" };
	ArrayList<Friend> freind = new ArrayList<Friend>();
	ArrayList<String> list;

	@Override
	public void onReceive(Context context, Intent intent) {

		this.context = context;
		// toast("alarm Received");
		Time time = new Time();
		time.setToNow();

		SavedMessageDatabase smDatabase = new SavedMessageDatabase(context);
		list = smDatabase.getBirthDay(months[time.month], "" + (time.monthDay));
		/*
		 * for (int i = 0; i < freind.size(); i++) { Log.v("birtdays",
		 * freind.get(i).getName() + freind.get(i).getBirthDay() +
		 * time.monthDay); }
		 */
		if (list.size() >0) {
			Intent popup = new Intent(context, BirthDayPopUpActivity.class);
			popup.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
			popup.putStringArrayListExtra("friend", list);
			context.startActivity(popup);
			// toast(""+list.size());
		}
		// test
		checkNeyWorkConnection();
		setUpdateDaysCount();
		
	}

	private void toast(String str) {
		Toast.makeText(context, str, Toast.LENGTH_LONG).show();
	}

	public void setUpdateDaysCount() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		int days = prefs.getInt("days", -1);
		if (days >=15)
		{
			if(hasInternet(context))
				updateFriendList();
		}
		else {

			SharedPreferences.Editor editor = PreferenceManager
					.getDefaultSharedPreferences(context).edit();
			editor.putInt("days", days + 1);
			Log.v("days",""+days);
			editor.commit();
		}
		//Toast.makeText(context,""+days,Toast.LENGTH_LONG).show();

	}

	public void setDaysUpdate() {

		SharedPreferences.Editor editor = PreferenceManager
				.getDefaultSharedPreferences(context).edit();
		editor.putInt("days", 0);
		editor.commit();
	}

	public void updateFriendList() {
		Log.v("inside","updatefriendlist");
		Session session = Session.getActiveSession();
		if (session.isOpened()) {
			getFrinedsBirthDayandUpdate();
		}

	}

	public void getFrinedsBirthDayandUpdate() {
		Session session = Session.getActiveSession();
		
		Log.v("inside","insidebirthdays");
		// Session.NewPermissionsRequest newPermissionsRequest=new
		// Session.NewPermissionsRequest(this,Arrays.asList("friends_birthday"));
		// session.requestNewReadPermissions(newPermissionsRequest);
		String fqlQuery = "select uid, name,birthday,pic from user where uid in (select uid2 from friend where uid1 = me())";
		Bundle params = new Bundle();
		params.putString("q", fqlQuery);
		final SavedMessageDatabase smDatabase = new SavedMessageDatabase(
				context);
		final HashMap<String, String> map = smDatabase.getFriendList();

		session = Session.getActiveSession();
		com.facebook.Request request = new com.facebook.Request(session,
				"/fql", params, HttpMethod.GET,
				new com.facebook.Request.Callback() {

					@Override
					public void onCompleted(Response response) {
						Log.v("response", response.toString());

						try {

							GraphObject go = response.getGraphObject();

							JSONObject jso = go.getInnerJSONObject();
							JSONArray jarray = jso.getJSONArray("data");
							for (int i = 0; i < jarray.length(); i++) {

								JSONObject jObject = (JSONObject) jarray.get(i);
								// list.add(jObject.getString("name")+jObject.get("birthday"));
								Log.v("info",
										jObject.getString("name")
												+ jObject.get("birthday")
												+ jObject.get("pic"));
								if (!jObject.getString("birthday").equals(
										"null")
										&& !jObject.getString("birthday")
												.equals(""))
									if (!map.containsKey(jObject
											.getString("uid"))) {
										freind.add(new Friend(jObject
												.getString("name"), jObject
												.getString("uid"), jObject
												.getString("birthday"), jObject
												.getString("pic")));
									}

							}
						} catch (JSONException e) {

							e.printStackTrace();
						} catch (NullPointerException e) {
							e.printStackTrace();
						}
						Log.v("birthdays", "" + freind.size());

						smDatabase.insertBirthDays(freind);
						setDaysUpdate();

					}

				});
		com.facebook.Request.executeBatchAsync(request);
	}

	public boolean checkNeyWorkConnection() {
		Log.v("connection","inside network connection");
		ConnectivityManager connManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mobilenet=connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		
		Log.v("connection",""+mWifi.getState()+mobilenet.getState());
		if (mWifi.isConnected() || mobilenet.isConnected()) 
			return true;
			
		else
			return false;
	}
	 public static boolean hasInternet(Context context) {
	        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	        if (connectivity != null){
	            NetworkInfo[] info = connectivity.getAllNetworkInfo();
	            if (info != null){
	                for (int i = 0; i < info.length; i++){
	                    if (info[i].getState() == NetworkInfo.State.CONNECTED){
	                        return true;
	                    }
	                }
	            }
	        }
	        return false;
	    }
	

}
