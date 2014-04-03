package com.smartengine.ohnomessaging;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.R.bool;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.bugsense.trace.BugSenseHandler;

import com.ohnomessaging.R;
import com.smartengine.ohnomessaging.NewMessageActivity.GetContacts;
import com.smartengine.ohnomessaging.adapter.MessageListAdapter;
import com.smartengine.ohnomessaging.adapter.NothingSelectedSpinnerAdapter;
import com.smartengine.ohnomessaging.dbhelper.SavedMessageDatabase;
import com.smartengine.ohnomessaging.model.TextMessage;
import com.smartengine.ohnomessaging.receiver.AlarmReceiver;
import com.smartengine.ohnomessaging.utils.Constants;

public class InboxActivity extends Activity {

	private static final int BUTTON_POSITIVE = -1;
	private static final int BUTTON_NEGATIVE = -2;
	private static int init = 0;

	// private static final int TYPE_INCOMING_MESSAGE = 1;
	private ListView messageList;
	private MessageListAdapter messageListAdapter;
	private List<TextMessage> recordsStored;
	private List<TextMessage> listInboxMessages;
	private ProgressDialog progressDialogInbox;
	private CustomHandler customHandler;

	List<Integer> threadIdList;
	List<TextMessage> smsInbox;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		BugSenseHandler.initAndStartSession(this, Constants.BUGSENSE_API_KEY);

		setContentView(R.layout.activity_inbox);
		//setAlarm();
		initViews();

	}

	@Override
	protected void onStart() {
		super.onStart();
		BugSenseHandler.startSession(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		BugSenseHandler.closeSession(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		populateMessageList();
	}

	private void initViews() {
		customHandler = new CustomHandler(this);
		progressDialogInbox = new ProgressDialog(this);

		recordsStored = new ArrayList<TextMessage>();

		messageList = (ListView) findViewById(R.id.messageList);
		messageList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {

				TextMessage selectedMessage = (TextMessage) parent
						.getItemAtPosition(position);
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(InboxActivity.this);
				String isLocked = prefs.getString(
						"thread_" + selectedMessage.getThreadId(), "-1");
				// toast(""+selectedMessage.getThreadId()+isLocked);
				if (!isLocked.equals("locked")) {

					Intent i = new Intent(InboxActivity.this,
							ContactSelectedNewMessageActivity.class);
					i.putExtra(Constants.THREAD_ID,
							selectedMessage.getThreadId());
					i.putExtra(Constants.DISPLAY_NAME,
							selectedMessage.getContactName());
					i.putExtra(Constants.CONTACT_ID,
							selectedMessage.getContactId());
					i.putExtra(Constants.CONTACT_NUMBER,
							selectedMessage.getPhoneNumber());
					startActivity(i);
				} else {
					toast("This thread is Locked");
				}

			}
		});

		messageList.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View v,
					int position, long id) {

				final ArrayList<String> list = new ArrayList<String>();
				final TextMessage selectedMessage = (TextMessage) parent
						.getItemAtPosition(position);
				final int threadId = selectedMessage.getThreadId();
				Spinner spinner = (Spinner) parent
						.findViewById(R.id.spinnerlongoptn);

				//list.add("Options");
				String option = setLockUnLockOption(threadId);
				list.add(option);
				list.add("Add To BlockList");
				list.add("DELETE");
														
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(
						getApplicationContext(), R.layout.spinner_row_view,
						list);
				spinner.setAdapter(new NothingSelectedSpinnerAdapter(adapter,R.layout.spinner_row_view,InboxActivity.this));
				//spinner.setAdapter(adapter);
				spinner.performClick();
				spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent, View v,
							int position, long id) {

						if (position == 1) {
							if (list.get(position-1).equals("LOCK")) {
								setLockToThread(threadId);

							} else if (list.get(position-1).equals("UNLOCK")) {

								showUnlockDialog(threadId);
							}
						} 
						else if (position == 2)
						{
							SavedMessageDatabase smDataBase=new SavedMessageDatabase(InboxActivity.this);
							smDataBase.insertItemToBlockList(selectedMessage.getContactName(),selectedMessage.getPhoneNumber());
							toast("Added To Blocklist");
						}
						else if (position == 3)
							showDeleteDialog(threadId);
						
						

					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {

					}
				});

				return false;
			}
		});

		populateMessageList();
	}

	public String setLockUnLockOption(int thrdid) {
		if (isThreadLocked(thrdid))
			return "UNLOCK";
		else
			return "LOCK";

	}

	public boolean isPasswordSet() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(InboxActivity.this);
		String password = prefs.getString("password", "");
		Log.v("pass",password);
		if (password.equals(""))
			return false;
		else
			return true;

	}

	public boolean isThreadLocked(int thrdid) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(InboxActivity.this);
		String isLocked = prefs.getString("thread_" + thrdid, "-1");
		if (isLocked.equals("locked"))
			return true;
		else
			return false;

	}

	public void setLockToThread(int thrdid) {
		if (isPasswordSet()) {
			SharedPreferences.Editor editor = PreferenceManager
					.getDefaultSharedPreferences(InboxActivity.this).edit();
			editor.putString("thread_" + thrdid, "locked");
			editor.commit();
			toast("Thread is Locked");
		} else
			toast("No password is set.Please set a password.");
	}

	private void showLongPressOptionsDialog(int threadId) {
		final Dialog lngpressoptionsdialog = new Dialog(InboxActivity.this);
		final ArrayList<String> list = new ArrayList<String>();
		list.add("Options");
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(InboxActivity.this);
		String isLocked = prefs.getString("thread_" + threadId, "-1");
		if (isLocked.equals("locked")) {
			list.add("UNLOCK");
		} else
			list.add("LOCK");
		//list.add("Add To Block List");
		list.add("DELETE");
		final int tid = threadId;
		lngpressoptionsdialog
				.setContentView(R.layout.message_thread_long_press);
		lngpressoptionsdialog.setTitle("Options");
		Spinner spnrop = (Spinner) lngpressoptionsdialog
				.findViewById(R.id.spinnerlongpress);
		// ArrayAdapter<>
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				getApplicationContext(), R.layout.spinner_row_view, list);
		spnrop.setAdapter(adapter);
		spnrop.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View v,
					int position, long id) {
				if (position == 1) {
					if (list.get(position).equals("LOCK")) {
						SharedPreferences.Editor editor = PreferenceManager
								.getDefaultSharedPreferences(InboxActivity.this)
								.edit();
						editor.putString("thread_" + tid, "locked");
						editor.commit();
						toast("Thread is Locked");
					} else if (list.get(position).equals("UNLOCK")) {
						/*
						 * SharedPreferences.Editor editor = PreferenceManager
						 * .getDefaultSharedPreferences(InboxActivity.this)
						 * .edit(); editor.putString("thread_" + tid,
						 * "unlocked"); editor.commit();
						 */
						showUnlockDialog(tid);
					}
					lngpressoptionsdialog.dismiss();
				}
				
				else if (position == 2) {
					showDeleteDialog(tid);
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});
		lngpressoptionsdialog.show();
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		// lngpressoptionsdialog.getWindow().setLayout(metrics.heightPixels,
		// metrics.widthPixels);
	}

	private void showUnlockDialog(int threadId) {
		final Dialog dialog = new Dialog(InboxActivity.this);
		dialog.setContentView(R.layout.set_password);
		dialog.setTitle("Set Password");
		final EditText edtxtpasswrd = (EditText) dialog
				.findViewById(R.id.editTextpasswrd);
		final EditText edttxtconfirmpassrd = (EditText) dialog
				.findViewById(R.id.editTextconfirmpasswrd);

		Button btnsave = (Button) dialog.findViewById(R.id.buttonsavepasswrd);
		edttxtconfirmpassrd.setVisibility(View.GONE);
		btnsave.setText("Unlock");
		final int tId = threadId;
		btnsave.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(InboxActivity.this);
				String password = prefs.getString("password", "-1");
				if (password.equals(edtxtpasswrd.getText().toString())) {
					SharedPreferences.Editor editor = PreferenceManager
							.getDefaultSharedPreferences(InboxActivity.this)
							.edit();
					editor.putString("thread_" + tId, "unlocked");
					editor.commit();
					dialog.dismiss();
					toast("Thread Unlocked");
				} else {
					toast("Incorrect Password");
				}

			}
		});
		dialog.show();
		// DisplayMetrics metrics = new DisplayMetrics();
		// getWindowManager().getDefaultDisplay().getMetrics(metrics);

		// dialog.getWindow().setLayout(metrics.heightPixels,
		// metrics.widthPixels);

	}

	private void toast(String str) {
		Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG).show();
	}

	private void showDeleteDialog(final int threadId) {

		AlertDialog Alert = new AlertDialog.Builder(InboxActivity.this)
				.create();
		Alert.setTitle("Delete");
		Alert.setMessage("Want to delete this text thread?");

		Alert.setButton(BUTTON_POSITIVE, "Delete",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.cancel();

						Uri deleteUri = Uri.parse("content://sms");
						getContentResolver().delete(deleteUri,
								"thread_id=" + threadId, null);
						populateMessageList();
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

	public void populateMessageList() {
		listInboxMessages = null;
		fetchInboxMessages();

		messageListAdapter = new MessageListAdapter(InboxActivity.this,
				R.layout.row_inbox_item, recordsStored);
		messageList.setAdapter(messageListAdapter);
	}

	private void showProgressDialog(String message) {
		progressDialogInbox.setMessage(message);
		progressDialogInbox.setIndeterminate(true);
		progressDialogInbox.setCancelable(true);
		progressDialogInbox.show();
	}

	private void fetchInboxMessages() {
		if (listInboxMessages == null) {
			threadIdList = new ArrayList<Integer>();
			showProgressDialog("Fetching Inbox Messages...");
			startThread();
		} else {
			// messageType = TYPE_INCOMING_MESSAGE;
			recordsStored = listInboxMessages;
			messageListAdapter.setArrayList(recordsStored);
		}
	}

	public class FetchMessageThread extends Thread {

		public int tag = -1;

		public FetchMessageThread(int tag) {
			this.tag = tag;
		}

		@Override
		public void run() {

			recordsStored = fetchInboxSms();
			listInboxMessages = recordsStored;
			customHandler.sendEmptyMessage(0);

		}

	}

	public List<TextMessage> fetchInboxSms() {
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
		c.close();

		return smsInbox;
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

	private FetchMessageThread fetchMessageThread;

	private int currentCount = 0;

	public synchronized void startThread() {

		if (fetchMessageThread == null) {
			fetchMessageThread = new FetchMessageThread(currentCount);
			fetchMessageThread.start();
		}
	}

	public synchronized void stopThread() {
		if (fetchMessageThread != null) {
			Log.i("Cancel thread", "stop thread");
			FetchMessageThread moribund = fetchMessageThread;
			currentCount = fetchMessageThread.tag == 0 ? 1 : 0;
			fetchMessageThread = null;
			moribund.interrupt();
		}
	}

	static class CustomHandler extends Handler {
		private final WeakReference<InboxActivity> activityHolder;

		CustomHandler(InboxActivity inboxListActivity) {
			activityHolder = new WeakReference<InboxActivity>(inboxListActivity);
		}

		@Override
		public void handleMessage(android.os.Message msg) {

			InboxActivity inboxListActivity = activityHolder.get();
			if (inboxListActivity.fetchMessageThread != null
					&& inboxListActivity.currentCount == inboxListActivity.fetchMessageThread.tag) {
				Log.i("received result", "received result");
				inboxListActivity.fetchMessageThread = null;

				inboxListActivity.messageListAdapter
						.setArrayList(inboxListActivity.recordsStored);
				inboxListActivity.progressDialogInbox.dismiss();
			}
		}
	}

	private OnCancelListener dialogCancelListener = new OnCancelListener() {

		@Override
		public void onCancel(DialogInterface dialog) {
			stopThread();
		}

	};

	public void onClickNewMessage(View v) {
		Intent i = new Intent(InboxActivity.this, NewMessageActivity.class);
		startActivity(i);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.inbox, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == R.id.new_message) {
			Intent i = new Intent(InboxActivity.this, NewMessageActivity.class);
			startActivity(i);
			
		}

		return super.onOptionsItemSelected(item);
	}

	
}
