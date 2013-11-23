package com.smartengine.ohnomessaging.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ohnomessaging.R;
import com.smartengine.ohnomessaging.model.TextMessage;
import com.smartengine.ohnomessaging.utils.Constants;
import com.smartengine.ohnomessaging.utils.Utility;

public class ThreadMessageAdapter extends BaseAdapter {
    
    private Context mContext;
    private LayoutInflater mInflater;
    private List<TextMessage> messageList;
    public Bitmap MyImageBitmap, OtherImageBitmap;
    
    public ThreadMessageAdapter(Context context, List<TextMessage> messageList, Bitmap myImage, Bitmap otherImage) {
        this.mContext = context;
        this.messageList = messageList;
        this.MyImageBitmap = myImage;
        this.OtherImageBitmap = otherImage;
        this.mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return messageList.size();
    }

    @Override
    public Object getItem(int position) {
        return messageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    
//    private interface MessageViewHolder {
//        public void populate(final Object item, int position, Bitmap bmp);
//    }
    
    private static class MessageHolder{
        
        private ImageView UserPic;        
        private TextView MessageBody;
        private TextView MessageTime;
        
//        public MessageHolder(View row) {
//            UserPic = (ImageView) row.findViewById(R.id.iv_user_pp);
//            MessageBody = (TextView) row.findViewById(R.id.tv_message_body);
//        }
//        
//        public void populateFrom(TextMessage item, int position, Bitmap bmp) {
//            UserPic.setImageBitmap(bmp);
//            MessageBody.setText(item.getMessageBody());
//        }
//
//        public void populate(Object item, int position, Bitmap bmp) {
//            if(item instanceof TextMessage){
//                this.populateFrom((TextMessage)item, position, bmp);
//            }           
//        }
        
    }
       
    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        TextMessage item = (TextMessage)getItem(position);

        int type = Constants.TYPE_INCOMING_MESSAGE;
        if (item.getMessaageType() == Constants.TYPE_SENT_MESSAGE) {
            type = Constants.TYPE_SENT_MESSAGE;
        }
        return type - 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        
        MessageHolder holder;
        if (convertView == null) {  
            int itemViewType = getItemViewType(position);
            if(itemViewType == Constants.TYPE_SENT_MESSAGE - 1)
                row = mInflater.inflate(R.layout.row_sent_message_item, null);
            else
                row = mInflater.inflate(R.layout.row_received_message_item, null);
            
            holder = new MessageHolder();
            
            holder.UserPic = (ImageView) row.findViewById(R.id.iv_user_pp);
            holder.MessageBody = (TextView) row.findViewById(R.id.tv_message_body);
            holder.MessageTime = (TextView) row.findViewById(R.id.tv_message_time);
            row.setTag(holder);
        } 
        else {
            holder = (MessageHolder) row.getTag();
        }
        
        TextMessage message = (TextMessage) getItem(position);
        
        holder.MessageBody.setText(message.getMessageBody());
        holder.MessageTime.setText(Utility.getFormattedTime(message.getTimeOfMessage()));
        
        if(message.getMessaageType() == Constants.TYPE_SENT_MESSAGE)
            holder.UserPic.setImageBitmap(MyImageBitmap);
        else
            holder.UserPic.setImageBitmap(OtherImageBitmap);
            
        return row;
    }

}
