package com.smartengine.ohnomessaging;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import com.facebook.LoggingBehavior;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.ohnomessaging.R;
import com.smartengine.ohnomessaging.adapter.FriendBirthDayList;
import com.smartengine.ohnomessaging.dbhelper.SavedMessageDatabase;
import com.smartengine.ohnomessaging.model.Friend;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class BirthDayPopUpActivity extends Activity {
	private int BUTTON_POSITIVE = -1;
	private int BUTTON_NEGATIVE = -2;
	private ListView listView;
	ArrayList<Friend> friends=new ArrayList<Friend>();
	ArrayList<String> list;
	private String[] months = { "January", "February", "March", " April",
			"May", "June", "July", "August", "September", "October",
			"November", "December" };
	private Session.StatusCallback statusCallback = new SessionStatusCallback();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_view_contact);
		listView = (ListView) findViewById(R.id.lc_contacts);
		Time time = new Time();
		time.setToNow();

		SavedMessageDatabase smDatabase = new SavedMessageDatabase(this);
		list = smDatabase.getBirthDay(months[time.month], "" + (time.monthDay));
		for (int i = 0; i < list.size(); i++) {
			String[] str = list.get(i).split(",");
			friends.add(new Friend(str[0], "", str[1],""));
			Log.v("msg",list.get(i));
		}
		
		FriendBirthDayList adapter = new FriendBirthDayList(
				this, R.layout.list_view_contact_row,
				friends);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				showDialog(friends.get(position).getName());
				//Intent intent=new Intent(getApplicationContext(),NewMessageActivity.class);
				//startActivity(getIntent());

			}

		});
		//facebook time line post
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
	}
	private void showDialog(final String name) {

		AlertDialog Alert = new AlertDialog.Builder(BirthDayPopUpActivity.this)
				.create();
		Alert.setTitle("Birthday");
		Alert.setMessage("Want to Wish "+ name+" ?");

		Alert.setButton(BUTTON_POSITIVE, "Yes",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						publishOnTimeLine();

						
					}
				});

		Alert.setButton(BUTTON_NEGATIVE, "Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.cancel();

					}
				});
		Alert.show();
	}
	private class SessionStatusCallback implements Session.StatusCallback {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {

		}
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
	private void publishOnTimeLine()
	{
		Facebook facebookObj=new Facebook("304156206399452");
		AsyncFacebookRunner asyncFacebookRunner=new AsyncFacebookRunner(facebookObj);
		final Handler facebookHandler = new Handler();
		try {
			//boolean restoreVal =SessionStore.
			//Log.d(TAG, "restore val in ToTUtility = " + restoreVal);
			Session.restoreSession(getApplicationContext(),null,null,null);
			Session session=Session.getActiveSession();
			facebookObj.setSession(session);
			if(facebookObj != null) {
				if (facebookObj.isSessionValid()) {
					//facebookObj.extendAccessTokenIfNeeded(this, null);
					Bundle params = new Bundle();
					params.putString("message","hlw");
					
					asyncFacebookRunner.request(" 100001256435624" + "/feed", params, "POST", new RequestListener() {
						@Override
						public void onIOException(IOException e, Object state) {
							// TODO Auto-generated method stub

						}
						@Override
						public void onFileNotFoundException(FileNotFoundException e,
								Object state) {
							// TODO Auto-generated method stub

						}
						@Override
						public void onMalformedURLException(MalformedURLException e,
								Object state) {
							// TODO Auto-generated method stub

						}
						@Override
						public void onFacebookError(FacebookError e, Object state) {
							// TODO Auto-generated method stub

						}
						@Override
						public void onComplete(String response, Object state) {
							Log.v("response",response);
							facebookHandler.post(new Runnable() {
								public void run() {
								
									Toast.makeText(BirthDayPopUpActivity.this, "Message published!", Toast.LENGTH_LONG).show();
								}
							});

						}
					}, null);
				}
				else{
					Log.v("msg", "sessionNOTValid, relogin");
				}


			} else {
				// no logged in, so relogin
				Log.v("msg", "sessionNOTValid, relogin");
				//    			ToTApplication.mFacebook.authorize( (Activity) context, permissions, new LoginDialogListener());
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
