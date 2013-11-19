package com.smartengine.ohnomessaging;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.bugsense.trace.BugSenseHandler;
import com.smartengine.ohnomessaging.adapter.MessageListAdapter;
import com.smartengine.ohnomessaging.model.TextMessage;
import com.smartengine.ohnomessaging.utils.Constants;

public class InboxActivity extends Activity {

    //    private static final int TYPE_INCOMING_MESSAGE = 1;
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
            public void onItemClick(AdapterView<?> parent, View b, int position, long id) {
                TextMessage selectedMessage = (TextMessage) parent.getItemAtPosition(position);
                            
                Intent i = new Intent(InboxActivity.this, ContactSelectedNewMessageActivity.class);
                i.putExtra(Constants.THREAD_ID, selectedMessage.getThreadId());
                i.putExtra(Constants.DISPLAY_NAME, selectedMessage.getContactName());
                i.putExtra(Constants.CONTACT_ID, selectedMessage.getContactId());
                i.putExtra(Constants.CONTACT_NUMBER, selectedMessage.getPhoneNumber());
                startActivity(i);

            }
        });


        populateMessageList();
    }

    public void populateMessageList() {
        listInboxMessages = null;
        fetchInboxMessages();

        messageListAdapter = new MessageListAdapter(InboxActivity.this, R.layout.row_inbox_item, recordsStored);
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

        Cursor c = this.getContentResolver()
                .query(uriSms,
                        new String[] { "_id", "thread_id", "address", "date", "body",
                        "type", "read" }, "type=" + Constants.TYPE_INCOMING_MESSAGE 
                        + " OR type=" + Constants.TYPE_SENT_MESSAGE, null,
                        "date" + " COLLATE LOCALIZED DESC");
        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            while (!c.isAfterLast()){
                int threadId = c.getInt(c.getColumnIndexOrThrow("thread_id"));
                if(!isThreadIdFound(threadId)){

                    String contactNumber = c.getString(c.getColumnIndexOrThrow("address"));
                    String messageBody = c.getString(c.getColumnIndexOrThrow("body"));                
                    int msgType = c.getInt(c.getColumnIndexOrThrow("type"));
                    String date = c.getString(c.getColumnIndexOrThrow("date"));

                    //                Log.e("?????????", "date =" + date);

                    // initially contact-name = number to cover those contact who don't have name
                    // initially contactId = -1;
                    TextMessage message = new TextMessage(contactNumber, null, -1, threadId, msgType, messageBody, date);
                    message.setMessageCount(1);
                    smsInbox.add(message);

                    //                Uri lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(contactNumber));
                    //                Cursor nameCursor = getContentResolver().query(lookupUri, new String[]{PhoneLookup.DISPLAY_NAME, PhoneLookup._ID},null,null,null);
                    //                try {
                    //                    nameCursor.moveToFirst();
                    //                    String displayName = nameCursor.getString(nameCursor.getColumnIndexOrThrow(PhoneLookup.DISPLAY_NAME));
                    //                    int contactId = nameCursor.getInt(nameCursor.getColumnIndexOrThrow(PhoneLookup._ID));
                    //                    Log.e(">>>>>", "contact id for " + contactNumber + " is = " + contactId);
                    //                    message.setContactName(displayName);
                    //                    message.setContactId(contactId);
                    //
                    //                } catch (Exception e) {
                    //                }finally{
                    //                    nameCursor.close();
                    //                }
                }
                c.moveToNext();
            }
        }
        c.close();

        return smsInbox;
    }

    private Boolean isThreadIdFound(int threadId){
        for(TextMessage tMsg : smsInbox){
            if(tMsg.getThreadId() == threadId){
                tMsg.setMessageCount(tMsg.getMessageCount() + 1);
                return true;
            }
        }
//        threadIdList.add(threadId);
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

    public void onClickNewMessage(View v){
        Intent i = new Intent(InboxActivity.this, NewMessageActivity.class);
        startActivity(i);
    }

}
