package com.smartengine.ohnomessaging.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ohnomessaging.R;
import com.smartengine.ohnomessaging.model.TextMessage;
import com.smartengine.ohnomessaging.utils.Utility;

public class MessageListAdapter extends ArrayAdapter<TextMessage> {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<TextMessage> messageListArray;
    private Bitmap defaultUserPic;

    public MessageListAdapter(Context context, int textViewResourceId, List<TextMessage> messageListArray) {
        super(context, textViewResourceId);
        this.messageListArray = messageListArray;
        this.mContext = context;
        this.mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        defaultUserPic = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture);
    }

    private static class ViewHolder {
        ImageView UserPic;
        TextView ContactName;
        TextView messageContent;
        TextView MessageCount;
        TextView Time;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {           
            convertView = mInflater.inflate(R.layout.row_inbox_item, null);

            holder = new ViewHolder();

            holder.UserPic = (ImageView) convertView.findViewById(R.id.iv_user_pp);
            holder.ContactName = (TextView) convertView.findViewById(R.id.tv_contact_name);
            holder.messageContent = (TextView) convertView.findViewById(R.id.tv_message_body);
            holder.MessageCount = (TextView) convertView.findViewById(R.id.tv_message_count);
            holder.Time = (TextView) convertView.findViewById(R.id.tv_time);
            convertView.setTag(holder);
        } 
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        TextMessage message = getItem(position);

        if(message.getContactName() == null)
            getContactInfo(position);

        holder.ContactName.setText((message.getContactName() == null)? message.getPhoneNumber() : message.getContactName());
        holder.messageContent.setText(message.getMessageBody());
        holder.MessageCount.setText("" + message.getMessageCount());
        holder.Time.setText(Utility.getFormattedTime(message.getTimeOfMessage()));
        holder.UserPic.setImageBitmap(getContactPhoto(message.getContactId()));

        return convertView;
    }

    @Override
    public int getCount() {
        return messageListArray.size();
    }

    @Override
    public TextMessage getItem(int position) {
        return messageListArray.get(position);
    }

    public void setArrayList(List<TextMessage> messageList) {
        this.messageListArray = messageList;
        notifyDataSetChanged();
    }


    public void getContactInfo(int position){
        TextMessage message = messageListArray.get(position);
        Uri lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(message.getPhoneNumber()));        
        try {
            Cursor nameCursor = mContext.getContentResolver().query(lookupUri, new String[]{PhoneLookup.DISPLAY_NAME, PhoneLookup._ID},null,null,null);
            if(nameCursor != null && nameCursor.getCount() > 0){
                nameCursor.moveToFirst();
                String displayName = nameCursor.getString(nameCursor.getColumnIndexOrThrow(PhoneLookup.DISPLAY_NAME));
                int contactId = nameCursor.getInt(nameCursor.getColumnIndexOrThrow(PhoneLookup._ID));
                //            Log.e(">>>>>", "contact id for " + contactNumber + " is = " + contactId);
                message.setContactName(displayName);
                message.setContactId(contactId);
                nameCursor.close();
            }

        } catch (Exception e) {
        }
    }


    public Bitmap getContactPhoto(long contactId) {
        //        return defaultUserPic;
        if(contactId == -1)
            return defaultUserPic;
        Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
        Uri photoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.CONTENT_DIRECTORY);
        Cursor cursor = mContext.getContentResolver().query(photoUri, new String[] {Contacts.Photo.PHOTO}, null, null, null);
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


}
