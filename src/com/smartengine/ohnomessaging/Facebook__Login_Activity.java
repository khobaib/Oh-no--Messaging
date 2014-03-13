package com.smartengine.ohnomessaging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.HttpMethod;
import com.facebook.LoggingBehavior;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.model.GraphObject;
import com.ohnomessaging.R;
import com.smartengine.ohnomessaging.adapter.ContactListadapter;
import com.smartengine.ohnomessaging.adapter.FriendBirthDayList;
import com.smartengine.ohnomessaging.comparator.SortFbFriendByName;
import com.smartengine.ohnomessaging.dbhelper.SavedMessageDatabase;
import com.smartengine.ohnomessaging.model.Contact;
import com.smartengine.ohnomessaging.model.Friend;
import com.smartengine.ohnomessaging.receiver.AlarmReceiver;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class Facebook__Login_Activity extends Activity {
	private Button btnLoginlogout;
	private Button btnShowFreindBirthDays;
	private Button btngetFriends;
	private Session.StatusCallback statusCallback = new SessionStatusCallback();
	ArrayList<Friend> list = new ArrayList<Friend>();
	ProgressDialog pdiaDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.facebook_login_activity);
		btnLoginlogout = (Button) findViewById(R.id.buttonfacebooklogin);
		btnShowFreindBirthDays = (Button) findViewById(R.id.buttonshowfriendbirthdays);
		btngetFriends=(Button)findViewById(R.id.buttongetbirthdays);
		btngetFriends.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//pdiaDialog=new ProgressDialog(getApplicationContext());
				//pdiaDialog.setTitle("Fetching Friend BirthDays");
				//pdiaDialog.show();
				getFrinedsBirthDays();
				
			}
		});
		btnShowFreindBirthDays.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				showDialog();

			}
		});
		Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

		Session session = Session.getActiveSession();
		if (session == null) {
			if (savedInstanceState != null) {
				session = Session.restoreSession(this, null, statusCallback,
						savedInstanceState);
			}
			if (session == null) {
				session = new Session(this);
			}
			Session.setActiveSession(session);
			if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
				session.openForRead(new Session.OpenRequest(this)
						.setCallback(statusCallback));
			}
		}
		updateViwes();
	}

	@Override
	public void onStart() {
		super.onStart();
		Session.getActiveSession().addCallback(statusCallback);
	}

	@Override
	public void onStop() {
		super.onStop();
		Session.getActiveSession().removeCallback(statusCallback);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode,
				resultCode, data);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Session session = Session.getActiveSession();
		Session.saveSession(session, outState);
	}

	public void updateViwes() {
		Session session = Session.getActiveSession();
		if (session.isOpened()) {

			btnLoginlogout.setText("Log out");
			btngetFriends.setVisibility(View.VISIBLE);
			btnLoginlogout.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					onClickLogout();
				}
			});
		} else {
			btnLoginlogout.setText("Log in");
			btngetFriends.setVisibility(View.GONE);
			btnLoginlogout.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					onClickLogin();
				}
			});
		}

	}

	public void getFrinedsBirthDays() {
		Session session = Session.getActiveSession();
		// Session.NewPermissionsRequest newPermissionsRequest=new
		// Session.NewPermissionsRequest(this,Arrays.asList("friends_birthday"));
		// session.requestNewReadPermissions(newPermissionsRequest);
		final ProgressDialog pd = new ProgressDialog(Facebook__Login_Activity.this);
		pd.setMessage("loading");
		pd.show();
		String fqlQuery = "select uid, name,birthday, is_app_user from user where uid in (select uid2 from friend where uid1 = me())";
		Bundle params = new Bundle();
		params.putString("q", fqlQuery);

		session = Session.getActiveSession();
		com.facebook.Request request = new com.facebook.Request(session,
				"/fql", params, HttpMethod.GET,
				new com.facebook.Request.Callback() {

					@Override
					public void onCompleted(Response response) {
						try {
							list.clear();
							GraphObject go = response.getGraphObject();
							JSONObject jso = go.getInnerJSONObject();
							JSONArray jarray = jso.getJSONArray("data");
							for (int i = 0; i < jarray.length(); i++) {

								JSONObject jObject = (JSONObject) jarray.get(i);
								// list.add(jObject.getString("name")+jObject.get("birthday"));
								Log.v("info", jObject.getString("name")
										+ jObject.get("birthday"));
								if(!jObject.getString("birthday").equals("null") && !jObject.getString("birthday").equals(""))
									list.add(new Friend(jObject.getString("name"),jObject.getString("uid"),jObject.getString("birthday")));
								

							}
						} catch (JSONException e) {
							
							e.printStackTrace();
						}
						
						Log.v("birthdays", response.toString());
						Log.v("birthdays", ""+list.size());
						SavedMessageDatabase smDatabase=new SavedMessageDatabase(Facebook__Login_Activity.this);
						smDatabase.deleteAllRecordsFromBirthdays();
						smDatabase.insertBirthDays(list);
						setAlarm(getApplicationContext());
						toast("saved in database");
						pd.dismiss();
						// showDialog();

					}
				});
		com.facebook.Request.executeBatchAsync(request);
	}

	private void onClickLogin() {
		Session session = Session.getActiveSession();

		if (!session.isOpened() && !session.isClosed()) {
			session.openForRead(new Session.OpenRequest(this)
					.setCallback(statusCallback));
		} else {
			Session.openActiveSession(this, true, statusCallback);
		}
	}

	private void onClickLogout() {
		Session session = Session.getActiveSession();
		if (!session.isClosed()) {
			session.closeAndClearTokenInformation();
		}
	}

	private class SessionStatusCallback implements Session.StatusCallback {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			if (!session.getPermissions().contains("friends_birthday")
					&& session.isOpened()) {
				Toast.makeText(getApplicationContext(),"no birthday persmiion",Toast.LENGTH_LONG).show();
				Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(
						Facebook__Login_Activity.this,
						Arrays.asList("friends_birthday"));
				session.requestNewReadPermissions(newPermissionsRequest);
			}
			updateViwes();
		}
	}

	public void showDialog() {
		SavedMessageDatabase smDatabase=new SavedMessageDatabase(Facebook__Login_Activity.this);
		ArrayList<Friend> flist=smDatabase.getFriendBirthDays();
		Collections.sort(flist,new SortFbFriendByName());
		final Dialog dialog = new Dialog(Facebook__Login_Activity.this);
		dialog.setContentView(R.layout.list_view_contact);
		dialog.setTitle("Friends");
		ListView listView = (ListView) dialog.findViewById(R.id.lc_contacts);
		
		 FriendBirthDayList adapter = new FriendBirthDayList(
				getApplicationContext(), R.layout.list_view_contact_row,flist);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				
				dialog.dismiss();
				
			}
			
		});
		dialog.show();

	}
	private void toast(String str) {
		Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG).show();
	}
	private void setAlarm(Context context) {

		//if (!isAlarmSet()) {
			AlarmManager alarmManager = (AlarmManager) context
					.getSystemService(Context.ALARM_SERVICE);
			Intent intent = new Intent(context,
					AlarmReceiver.class);
			PendingIntent alarmIntent = PendingIntent.getBroadcast(
					context, 0, intent, 0);
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(System.currentTimeMillis());
			calendar.set(Calendar.HOUR_OF_DAY, 00);
			calendar.set(Calendar.MINUTE, 02);
			alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
					calendar.getTimeInMillis(),AlarmManager.INTERVAL_DAY, alarmIntent);

			SharedPreferences.Editor editor = PreferenceManager
					.getDefaultSharedPreferences(context)
					.edit();
			editor.putString("alarm", "1");
			editor.commit();
			toast("alarm is set now");

		//}

	}

	private boolean isAlarmSet() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		String isAlarm = prefs.getString("alarm", "-1");
		if (isAlarm.equals("-1"))
			return false;
		else
			return true;

	}

	
}
