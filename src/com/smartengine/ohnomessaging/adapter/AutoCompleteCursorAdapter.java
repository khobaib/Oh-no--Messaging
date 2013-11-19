package com.smartengine.ohnomessaging.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.provider.Contacts.People;
import android.provider.SyncStateContract.Constants;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AutoCompleteCursorAdapter extends CursorAdapter {

    private TextView mName, mNumber;
    
    public AutoCompleteCursorAdapter(Context context, Cursor c) {
        super(context, c);
        mContent = context.getContentResolver();
    }
 
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final LinearLayout ret = new LinearLayout(context);
        final LayoutInflater inflater = LayoutInflater.from(context);
        mName = (TextView) inflater.inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
        mNumber = (TextView) inflater.inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
        ret.setOrientation(LinearLayout.VERTICAL);
 
        LinearLayout horizontal = new LinearLayout(context);
        horizontal.setOrientation(LinearLayout.HORIZONTAL);
         
        // YOU CAN EVEN ADD IMAGES TO EACH ENTRY OF YOUR AUTOCOMPLETE FIELDS
        // THIS EXAMPLE DOES IT PROGRAMMATICALLY USING JAVA, BUT THE XML ANALOG IS VERY SIMILAR
        ImageView icon = new ImageView(context);
 
        int nameIdx = cursor.getColumnIndexOrThrow(People.NAME);
        int numberIdx = cursor.getColumnIndex(People.NUMBER);
 
        String name = cursor.getString(nameIdx);
        String number = cursor.getString(numberIdx);
 
        mName.setText(name);
        mNumber.setText(number);
 
        // SETTING THE TYPE SPECIFICS USING JAVA
        mNumber.setTextSize(16);
        mNumber.setTextColor(Color.GRAY);
 
        /**
         * Always add an icon, even if it is null. Keep the layout children
         * indices consistent.
         */
        Drawable image_icon  = null;
//        if (carrier != CarrierInfo.NOT_CACHED && carrier != CarrierInfo.ERROR && carrier != CarrierInfo.NETWORK_ERROR) {
//            image_icon = context.getResources().getDrawable(R.drawable.icon);
//        }
 
        icon.setImageDrawable(image_icon);
        icon.setPadding(0, -2, 2, 6);
 
        // AN EXAMPLE OF HOW YOU CAN ARRANGE YOUR LAYOUTS PROGRAMMATICALLY
        // PLACE THE NUMBER AND ICON NEXT TO EACH OTHER
        horizontal
                .addView(mNumber, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        horizontal.addView(icon, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 
        ret.addView(mName, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        ret.addView(horizontal, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        return ret;
    }
 
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int nameIdx = cursor.getColumnIndexOrThrow(People.NAME);
        int numberIdx = cursor.getColumnIndex(People.NUMBER);
 
        String name = cursor.getString(nameIdx);
        String number = cursor.getString(numberIdx);
 
        /**
         * Always add an icon, even if it is null. Keep the layout children
         * indices consistent.
         */
        Drawable image_icon  = null;
//        if (carrier != CarrierInfo.NOT_CACHED && carrier != CarrierInfo.ERROR && carrier != CarrierInfo.NETWORK_ERROR) {
//            image_icon = context.getResources().getDrawable(R.drawable.icon);
//        }
 
        // NOTICE VIEWS HAVE ALREADY BEEN INFLATED AND LAYOUT HAS ALREADY BEEN SET SO ALL YOU NEED TO DO IS SET THE DATA
        ((TextView) ((LinearLayout) view).getChildAt(0)).setText(name);
        LinearLayout horizontal = (LinearLayout) ((LinearLayout) view).getChildAt(1);
        ((TextView) horizontal.getChildAt(0)).setText(number);
        ((ImageView) horizontal.getChildAt(1)).setImageDrawable(image_icon);
    }
 
    @Override
    public String convertToString(Cursor cursor) {
        // THIS METHOD DICTATES WHAT IS SHOWN WHEN THE USER CLICKS EACH ENTRY IN YOUR AUTOCOMPLETE LIST
        // IN MY CASE I WANT THE NUMBER DATA TO BE SHOWN
        int numCol = cursor.getColumnIndexOrThrow(People.NUMBER);
        String number = cursor.getString(numCol);
        return number;
    }
 
    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        // THIS IS HOW YOU QUERY FOR SUGGESTIONS
        // NOTICE IT IS JUST A STRINGBUILDER BUILDING THE WHERE CLAUSE OF A CURSOR WHICH IS THE USED TO QUERY FOR RESULTS
        if (getFilterQueryProvider() != null) { return getFilterQueryProvider().runQuery(constraint); }
 
        StringBuilder buffer = null;
        String[] args = null;
        if (constraint != null) {
            buffer = new StringBuilder();
            buffer.append(People.NAME + " IS NOT NULL AND " + People.NUMBER_KEY + " IS NOT NULL AND ");
            buffer.append("UPPER(");
            buffer.append(People.NAME);
            buffer.append(") GLOB ?");
            args = new String[] { constraint.toString().toUpperCase() + "*" };
        }
 
//        return mContent.query(People.CONTENT_URI, Constants.PEOPLE_PROJECTION, buffer == null ? null : buffer
//                .toString(), args, People.DEFAULT_SORT_ORDER);
        return null;
    }
 
    private ContentResolver mContent;

}
