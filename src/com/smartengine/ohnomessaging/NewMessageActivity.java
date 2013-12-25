package com.smartengine.ohnomessaging;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;
import com.ohnomessaging.R;
import com.smartengine.ohnomessaging.adapter.ContactsAdapter;
import com.smartengine.ohnomessaging.model.Contact;
import com.smartengine.ohnomessaging.utils.Constants;
import com.smartengine.ohnomessaging.utils.Utility;
import com.smartengine.ohnomessaging.view.ContactsCompletionView;
import com.smartengine.ohnomessaging.view.TokenCompleteTextView;

public class NewMessageActivity extends Activity implements TokenCompleteTextView.TokenListener{

    private static int MAX_SMS_MESSAGE_LENGTH = 160;

    RelativeLayout rlSendTo;
    TextView tvSendTo;
    ImageView ivSendTo;

    //    ContactsEditText contactNos;

    ContactsCompletionView completionView;
    ContactsAdapter cAdapter;
    EditText MessageBody;

    //    String strContacts;
    //    List<String> phoneNumbers;

    Bitmap defaultUserPic;

    int fromActivity;

    BroadcastReceiver sentMessageReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BugSenseHandler.initAndStartSession(this, Constants.BUGSENSE_API_KEY);

        setContentView(R.layout.activity_new_message);

        //        strContacts = null;
        //        phoneNumbers = new ArrayList<String>();
        defaultUserPic = BitmapFactory.decodeResource(getResources(), R.drawable.ic_contact_picture);

        cAdapter = new ContactsAdapter(this);

        completionView = (ContactsCompletionView)findViewById(R.id.searchView);
        completionView.setAdapter(cAdapter);
        completionView.setTokenListener(this);

        if (savedInstanceState == null) {
            completionView.setPrefix("");
            //            completionView.addObject(people[0]);
            //            completionView.addObject(people[1]);
        }

        //        contactNos = (ContactsEditText) findViewById(R.id.act_to);
        MessageBody = (EditText) findViewById(R.id.et_msg_body);

        tvSendTo = (TextView) findViewById(R.id.tv_send_to);         
        ivSendTo = (ImageView) findViewById(R.id.iv_send_to);  

        rlSendTo = (RelativeLayout) findViewById(R.id.rl_send_to);
        rlSendTo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {   
                //                String sendTo = contactNos.getText().toString().trim();
                String msgBody = MessageBody.getText().toString().trim();

                if(completionView.getObjects().size() == 0)
                    Toast.makeText(NewMessageActivity.this, "No contact is selected.", Toast.LENGTH_SHORT).show();
                else if(msgBody == null || msgBody.equals(""))
                    Toast.makeText(NewMessageActivity.this, "Message is empty.", Toast.LENGTH_SHORT).show();
                else{
                    for (Object obj: completionView.getObjects()) {
                        Contact contact = (Contact)obj;
                        String sendTo = contact.getPhoneNumber();
                        
                        ContentValues values = new ContentValues();
                        values.put("address", sendTo);
                        values.put("body", msgBody); 
                        getApplicationContext().getContentResolver().insert(Uri.parse("content://sms/sent"), values);
                        
                        sendSMS(sendTo, msgBody);
                    }
                }
            }
        }); 
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

//                        ContentValues values = new ContentValues();
//                        values.put("address", phoneNumber);
//                        values.put("body", message); 
//                        getApplicationContext().getContentResolver().insert(Uri.parse("content://sms/sent"), values);

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

                try{
                    unregisterReceiver(sentMessageReceiver);
                    NewMessageActivity.this.finish();
                }catch (Exception e){
                    Log.e("", "exception in unregistering broadcastreceiver");
                }
            }
        };

        //---when the SMS has been sent---
        registerReceiver(sentMessageReceiver, new IntentFilter(SENT));      

        SmsManager sms = SmsManager.getDefault();

        int length = message.length();          
        if(length > MAX_SMS_MESSAGE_LENGTH) {
            ArrayList<String> messagelist = sms.divideMessage(message);         
            
            ArrayList<PendingIntent> sendPIs = new ArrayList<PendingIntent>();
            sendPIs.add(sentPI);
            int msgCount = messagelist.size();
            for(int i = 1; i < msgCount; i++){
                sendPIs.add(null);
            }
            
            sms.sendMultipartTextMessage(phoneNumber, null, messagelist, sendPIs, null);
        }
        else       
            sms.sendTextMessage(phoneNumber, null, message, sentPI, null);        
    }

    public void onClickInbox(View v){
        Intent i = new Intent(NewMessageActivity.this, InboxActivity.class);
        startActivity(i);
        finish();
    }




    private void updateUI(){
        String strContacts = null;
        if(completionView.getObjects().size() == 0){
            strContacts = "Send";  
            ivSendTo.setImageBitmap(defaultUserPic);
            tvSendTo.setVisibility(View.INVISIBLE);
        }
        else if(completionView.getObjects().size() == 1){
            strContacts = "" + ((Contact) completionView.getObjects().get(0)).getDisplayName();
            ivSendTo.setVisibility(View.VISIBLE);
            ivSendTo.setImageBitmap(((Contact) completionView.getObjects().get(0)).getImage());
            tvSendTo.setVisibility(View.VISIBLE);
        }
        else if(completionView.getObjects().size() > 1){
            ivSendTo.setVisibility(View.GONE);
            strContacts = ""; 
            for (Object obj: completionView.getObjects()) {
                Contact contact = (Contact)obj;
                strContacts = strContacts + contact.getDisplayName() + ", ";
            }
            strContacts = Utility.trimLastComma(strContacts);
            tvSendTo.setVisibility(View.VISIBLE);
        }


        tvSendTo.setText(strContacts);
    }




    private void updateTokenConfirmation() {
        updateUI();
    }


    @Override
    public void onTokenAdded(Object token) {
        //        ((TextView)findViewById(R.id.lastEvent)).setText("Added: " + token);
        Log.e("onTokenAdded", "Token Added");
        updateTokenConfirmation();
    }

    @Override
    public void onTokenRemoved(Object token) {
        Log.e("onTokenAdded", "Token removed");
        //        ((TextView)findViewById(R.id.lastEvent)).setText("Removed: " + token);
        updateTokenConfirmation();
    }

}
