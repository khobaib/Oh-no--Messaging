package com.smartengine.ohnomessaging;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.telephony.SmsManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;
import com.smartengine.ohnomessaging.adapter.MessageListAdapter;
import com.smartengine.ohnomessaging.adapter.ThreadMessageAdapter;
import com.smartengine.ohnomessaging.model.TextMessage;
import com.smartengine.ohnomessaging.utils.Constants;

public class ContactSelectedNewMessageActivity extends Activity {

    private static int MAX_SMS_MESSAGE_LENGTH = 160;

    ListView ThreadMessageList;

    ThreadMessageAdapter threadMessageAdapter;
    List<TextMessage> messageList;

    int threadId;
    String contactName;
    int contactId;

    Bitmap myUserPic, otherUserPic, defaultUserPic;

    RelativeLayout rlSendTo;
    TextView tvSendTo;
    ImageView ivSendTo;

    EditText MessageBody;
    List<String> phoneNumbers;
    
    BroadcastReceiver sentMessageReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BugSenseHandler.initAndStartSession(this, Constants.BUGSENSE_API_KEY);
        
        setContentView(R.layout.activity_contact_selected_new_message);

        ThreadMessageList = (ListView) findViewById(R.id.lv_thread_messages);

        contactName = null;
        contactId = -1;
        myUserPic = BitmapFactory.decodeResource(getResources(), R.drawable.ic_contact_picture);
        defaultUserPic = BitmapFactory.decodeResource(getResources(), R.drawable.ic_contact_picture);

        phoneNumbers = new ArrayList<String>();
        MessageBody = (EditText) findViewById(R.id.et_msg_body);

        tvSendTo = (TextView) findViewById(R.id.tv_send_to);         
        ivSendTo = (ImageView) findViewById(R.id.iv_send_to);  

        rlSendTo = (RelativeLayout) findViewById(R.id.rl_send_to);
        rlSendTo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {   
                //                String sendTo = contactNos.getText().toString().trim();
                String msgBody = MessageBody.getText().toString().trim();

                if(msgBody == null || msgBody.equals(""))
                    Toast.makeText(ContactSelectedNewMessageActivity.this, "Message is empty.", Toast.LENGTH_SHORT).show();
                else{
                    for(String sendTo : phoneNumbers)
                        sendSMS(sendTo, msgBody);
                    finish();
                }
            }
        });
        
        threadId = getIntent().getExtras().getInt(Constants.THREAD_ID); 
        
        String contactName = getIntent().getExtras().getString(Constants.DISPLAY_NAME); 
        tvSendTo.setText("Send to " + contactName);
        
        contactId = getIntent().getExtras().getInt(Constants.CONTACT_ID);
        otherUserPic = getContactPhoto(contactId);
        
        String contactNumber = getIntent().getExtras().getString(Constants.CONTACT_NUMBER);
        phoneNumbers.add(contactNumber);
        
        myUserPic = getProfilePhoto();

//        String[] mProjection = new String[]{
//                Profile._ID,
//                Profile.DISPLAY_NAME_PRIMARY,
//                Profile.LOOKUP_KEY,
//                Profile.PHOTO_THUMBNAIL_URI
//        };
//
//        Cursor mProfileCursor = getContentResolver().query(Profile.CONTENT_URI, mProjection , null, null, null);
//        if (mProfileCursor != null && mProfileCursor.getCount() > 0) {
//            mProfileCursor.moveToFirst();
//            byte[] data = mProfileCursor.getBlob(3);
//            if (data != null) {
//                myUserPic = BitmapFactory.decodeByteArray(data, 0, data.length);
//            }
//        }
//        mProfileCursor.close();

        
        ivSendTo.setImageBitmap(otherUserPic);
        
        messageList = fetchInboxSms();

        threadMessageAdapter = new ThreadMessageAdapter(ContactSelectedNewMessageActivity.this,
                messageList, myUserPic, otherUserPic);
        ThreadMessageList.setAdapter(threadMessageAdapter);
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


    public List<TextMessage> fetchInboxSms() {
        List<TextMessage> smsInbox = new ArrayList<TextMessage>();

        Uri uriSms = Uri.parse("content://sms");

        Cursor c = this.getContentResolver()
                .query(uriSms,
                        new String[] { "_id", "thread_id", "address", "date", "body",
                        "type", "read" }, "thread_id=" + threadId + " AND + (type=" + Constants.TYPE_INCOMING_MESSAGE 
                        + " OR type=" + Constants.TYPE_SENT_MESSAGE + ")", null,
                        "date" + " COLLATE LOCALIZED ASC");
        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            while (!c.isAfterLast()){
                //                int threadId = c.getInt(c.getColumnIndexOrThrow("thread_id"));
                String contactNumber = c.getString(c.getColumnIndexOrThrow("address"));
                String messageBody = c.getString(c.getColumnIndexOrThrow("body"));                
                int msgType = c.getInt(c.getColumnIndexOrThrow("type"));
                String date = c.getString(c.getColumnIndexOrThrow("date"));

//                if(contactId == -1)
//                    getContactInfo(contactNumber);

                // initially contact-name = number to cover those contact who don't have name
                // initially contactId = -1;
                TextMessage message = new TextMessage(contactNumber, contactNumber, contactId, threadId, msgType, messageBody, date);
                smsInbox.add(message);
                c.moveToNext();
            }
        }
        c.close();

        return smsInbox;
    }


//    public void getContactInfo(String contactNumber){
//        Uri lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(contactNumber));
//        Cursor nameCursor = getContentResolver().query(lookupUri, new String[]{PhoneLookup.DISPLAY_NAME, PhoneLookup._ID},null,null,null);
//        try {
//            nameCursor.moveToFirst();
//            contactName = nameCursor.getString(nameCursor.getColumnIndexOrThrow(PhoneLookup.DISPLAY_NAME));
//            contactId = nameCursor.getInt(nameCursor.getColumnIndexOrThrow(PhoneLookup._ID));
//            //            Log.e(">>>>>", "contact id for " + contactNumber + " is = " + contactId);
//
//        } catch (Exception e) {
//        }finally{
//            nameCursor.close();
//        }
//    }


    public Bitmap getContactPhoto(long contactId) {
        if(contactId == -1)
            return defaultUserPic;
        Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
        Uri photoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.CONTENT_DIRECTORY);
        Cursor cursor = getContentResolver().query(photoUri, new String[] {Contacts.Photo.PHOTO}, null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            Bitmap thumbnail = defaultUserPic;
            if (cursor.moveToFirst()) {
                byte[] data = cursor.getBlob(0);
                if (data != null) {
                    thumbnail = BitmapFactory.decodeByteArray(data, 0, data.length);
                }
            }
            return thumbnail;
        } finally {
            cursor.close();
        }
    }
    
    public Bitmap getProfilePhoto() {
        Bitmap profilePhoto = null;
        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(), ContactsContract.Profile.CONTENT_URI);
        if (input != null) {
            profilePhoto = BitmapFactory.decodeStream(input);
        }

        return profilePhoto;
    }


    private void sendSMS(final String phoneNumber, final String message){        
        String SENT = "SMS_SENT";
//        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);

//        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

        sentMessageReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS sent", Toast.LENGTH_SHORT).show();
                        
                        ContentValues values = new ContentValues();
                        values.put("address", phoneNumber);
                        values.put("body", message); 
                        getApplicationContext().getContentResolver().insert(Uri.parse("content://sms/sent"), values);
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off", Toast.LENGTH_SHORT).show();
                        break;
                }
                unregisterReceiver(sentMessageReceiver);
                ContactSelectedNewMessageActivity.this.finish();
            }
        };
 
        //---when the SMS has been sent---
        registerReceiver(sentMessageReceiver, new IntentFilter(SENT));

        //---when the SMS has been delivered---
//        registerReceiver(new BroadcastReceiver(){
//            @Override
//            public void onReceive(Context arg0, Intent arg1) {
//                switch (getResultCode())
//                {
//                    case Activity.RESULT_OK:
//                        Toast.makeText(getBaseContext(), "SMS delivered to " + phoneNumber, Toast.LENGTH_SHORT).show();
//                        break;
//                    case Activity.RESULT_CANCELED:
//                        Toast.makeText(getBaseContext(), "SMS not delivered to " + phoneNumber, Toast.LENGTH_SHORT).show();
//                        break;                        
//                }
//            }
//        }, new IntentFilter(DELIVERED));        

        SmsManager sms = SmsManager.getDefault();

        int length = message.length();          
        if(length > MAX_SMS_MESSAGE_LENGTH) {
            ArrayList<String> messagelist = sms.divideMessage(message);          
            sms.sendMultipartTextMessage(phoneNumber, null, messagelist, null, null);
        }
        else       
            sms.sendTextMessage(phoneNumber, null, message, sentPI, null);        
    }


}
