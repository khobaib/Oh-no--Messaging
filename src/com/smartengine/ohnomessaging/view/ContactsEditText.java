/*
* Copyright (C) 2012 Robert Li
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.smartengine.ohnomessaging.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.widget.MultiAutoCompleteTextView;

import com.bugsense.trace.Utils;
import com.smartengine.ohnomessaging.R;
import com.smartengine.ohnomessaging.adapter.ContactsAdapter;
import com.smartengine.ohnomessaging.model.Contact;
import com.smartengine.ohnomessaging.utils.Utility;

public class ContactsEditText extends MultiAutoCompleteTextView {
    
    private ContactsAdapter mAdapter;
    private Bitmap mLoadingImage;
    private int mDropdownItemHeight;

    public ContactsEditText(Context context) {
        super(context);
        init(context);
    }

    public ContactsEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ContactsEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        // Set image height
        mDropdownItemHeight = 48;
        
        // Set default image
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(),
                R.drawable.ic_contact_picture_holo_light, options);
        options.inSampleSize = Utility.calculateInSampleSize(options, mDropdownItemHeight,
                mDropdownItemHeight);
        options.inJustDecodeBounds = false;
        mLoadingImage = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.ic_contact_picture_holo_light, options);

        // Set adapter
        mAdapter = new ContactsAdapter(context);
        setAdapter(mAdapter);

        // Separate entries by commas
        setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        // Pop up suggestions after 1 character is typed.
        setThreshold(1);
    }
    
    @Override
    protected CharSequence convertSelectionToString(Object selectedItem) {
        return ((Contact) selectedItem).getDisplayName();
    }
    
    /**
     * Holder class to return results to the parent Activity.
     */
/*    public class Contact {
        public String displayName;
        public Bitmap image;
        public long id;
        public String lookupKey;
        public String phoneNumber;
    }*/
    
//    private class ContactsAdapter extends CursorAdapter {
//
//        Context mContext;
//        LayoutInflater mInflater;
//
//        public ContactsAdapter(Context context) {
//            super(context, null, 0);
//
//            mContext = context;
//            mInflater = LayoutInflater.from(context);
//        }
//        
//        @Override
//        public Object getItem(int position) {
//            Cursor cursor = (Cursor) super.getItem(position);
//            Contact contact = new Contact();
//            
//            String imageUri = cursor.getString(ContactsQuery.PHOTO_THUMBNAIL_DATA_COLUMN);
//            Bitmap bitmap = loadContactPhotoThumbnail(imageUri, mDropdownItemHeight);
//            if (bitmap == null) {
//                bitmap = mLoadingImage;
//            }
//            
//            contact.setId(cursor.getLong(ContactsQuery.ID_COLUMN));
//            contact.setLookupKey(cursor.getString(ContactsQuery.LOOKUP_KEY_COLUMN));
//            contact.setDisplayName(cursor.getString(ContactsQuery.DISPLAY_NAME_COLUMN));
//            contact.setImage(bitmap);
//            contact.setPhoneNumber(cursor.getString(ContactsQuery.CONTACT_PHONE_NUMBER_COLUMN));
//            
//            return contact;
//        }
//
//        @Override
//        public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
//            if (constraint == null || constraint.length() == 0) {
//                return mContext.getContentResolver().query(
//                        ContactsQuery.CONTENT_URI,
//                        ContactsQuery.PROJECTION,
//                        ContactsQuery.SELECTION,
//                        null,
//                        ContactsQuery.SORT_ORDER); 
//            }
//
//            return mContext.getContentResolver().query(
//                    Uri.withAppendedPath(ContactsQuery.FILTER_URI, constraint.toString()),
//                    ContactsQuery.PROJECTION,
//                    ContactsQuery.SELECTION,
//                    null,
//                    ContactsQuery.SORT_ORDER);
//        }
//
//        @Override
//        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
//            final View dropdownView = mInflater.inflate(R.layout.contacts_dropdown_item,
//                    viewGroup, false);
//
//            ViewHolder holder = new ViewHolder();
//            holder.text = (TextView) dropdownView.findViewById(R.id.text1); 
//            holder.phoneNumber = (TextView) dropdownView.findViewById(R.id.tv_number); 
//            holder.image = (ImageView) dropdownView.findViewById(R.id.iv_user_pic);
//
//            dropdownView.setTag(holder);
//
//            return dropdownView;
//        }
//
//        @Override
//        public void bindView(View view, Context context, Cursor cursor) {
//            final ViewHolder holder = (ViewHolder) view.getTag();
//
//            final String displayName = cursor.getString(ContactsQuery.DISPLAY_NAME_COLUMN);
//            final String phNumber = cursor.getString(ContactsQuery.CONTACT_PHONE_NUMBER_COLUMN);
//            final String imageUri = cursor.getString(ContactsQuery.PHOTO_THUMBNAIL_DATA_COLUMN);
//
//            holder.text.setText(displayName);
//            holder.phoneNumber.setText(phNumber);
//
//            Bitmap bitmap = loadContactPhotoThumbnail(imageUri, mDropdownItemHeight);
//            if (bitmap == null) {
//                bitmap = mLoadingImage;
//            }
//            holder.image.setImageBitmap(bitmap);
//        }
//
//        private Bitmap loadContactPhotoThumbnail(String photoData, int imageSize) {
//            AssetFileDescriptor afd = null;
//
//            try {
//                Uri thumbUri;
//                if (Utils.hasHoneycomb() && photoData != null) {
//                    thumbUri = Uri.parse(photoData);
//                } else {
//                    final Uri contactUri = Uri.withAppendedPath(Contacts.CONTENT_URI, photoData);
//                    thumbUri = Uri.withAppendedPath(contactUri, Photo.CONTENT_DIRECTORY);
//                }
//
//                afd = mContext.getContentResolver().openAssetFileDescriptor(thumbUri, "r");
//                FileDescriptor fd = afd.getFileDescriptor();
//
//                if (fd != null) {
//                    return Utils.decodeSampledBitmapFromDescriptor(fd, imageSize, imageSize);
//                }
//            } catch (FileNotFoundException e) {
//            } finally {
//                if (afd != null) {
//                    try {
//                        afd.close();
//                    } catch (IOException e) {} 
//                }
//            }
//
//            return null;
//        }
//
//    }

    /**
     * Class to hold the dropdown item's views. Used as a tag to bind the child views to its
     * parent.
     */
//    private class ViewHolder {
//        public TextView text;
//        public TextView phoneNumber;
//        public ImageView image;
//    }

    /**
     * This interface defines constants for the Cursor and CursorLoader, based on constants defined
     * in the {@link android.provider.ContactsContract.Contacts} class.
     */
//    private static interface ContactsQuery {
//
//        // A content URI for the Contacts table
//        final static Uri CONTENT_URI = CommonDataKinds.Phone.CONTENT_URI;
//
//        // The search/filter query Uri
//        final static Uri FILTER_URI = CommonDataKinds.Phone.CONTENT_FILTER_URI;
////        final static Uri FILTER_URI = Contacts.CONTENT_LOOKUP_URI;
//
//        // The selection clause for the CursorLoader query. The search criteria defined here
//        // restrict results to contacts that have a display name, are linked to visible groups,
//        // and have a phone number.  Notice that the search on the string provided by the user
//        // is implemented by appending the search string to CONTENT_FILTER_URI.
//        @SuppressLint("InlinedApi")
//        final static String SELECTION =
//                (Utils.hasHoneycomb() ? CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY : CommonDataKinds.Phone.DISPLAY_NAME) +
//                "<>''" + " AND " + CommonDataKinds.Phone.IN_VISIBLE_GROUP + "=1 AND " +
//                CommonDataKinds.Phone.HAS_PHONE_NUMBER + "=1";
//
//        // The desired sort order for the returned Cursor. Not sure what apps like Mms use, but
//        // TIMES_CONTACTED seems to be fairly useful for this purpose.
//        final static String SORT_ORDER = CommonDataKinds.Phone.TIMES_CONTACTED + " DESC";
//
//        // The projection for the CursorLoader query. This is a list of columns that the Contacts
//        // Provider should return in the Cursor.
//        @SuppressLint("InlinedApi")
//        final static String[] PROJECTION = {
//
//            // The contact's row id
//            CommonDataKinds.Phone._ID,
//
//            // A pointer to the contact that is guaranteed to be more permanent than _ID. Given
//            // a contact's current _ID value and LOOKUP_KEY, the Contacts Provider can generate
//            // a "permanent" contact URI.
//            CommonDataKinds.Phone.LOOKUP_KEY,
//
//            // In platform version 3.0 and later, the Contacts table contains
//            // DISPLAY_NAME_PRIMARY, which either contains the contact's displayable name or
//            // some other useful identifier such as an email address. This column isn't
//            // available in earlier versions of Android, so you must use Contacts.DISPLAY_NAME
//            // instead.
//            Utils.hasHoneycomb() ? CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY : CommonDataKinds.Phone.DISPLAY_NAME,
//
//            // In Android 3.0 and later, the thumbnail image is pointed to by
//            // PHOTO_THUMBNAIL_URI. In earlier versions, there is no direct pointer; instead,
//            // you generate the pointer from the contact's ID value and constants defined in
//            // android.provider.ContactsContract.Contacts.
//            Utils.hasHoneycomb() ? CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI : CommonDataKinds.Phone._ID,
//                    
////            Contacts.Data.
//                    
////                    ContactsContract.CommonDataKinds.Phone.
//            CommonDataKinds.Phone.NUMBER
//        };
//        
//        // The query column numbers which map to each value in the projection
//        final static int ID_COLUMN = 0;
//        final static int LOOKUP_KEY_COLUMN = 1;
//        final static int DISPLAY_NAME_COLUMN = 2;
//        final static int PHOTO_THUMBNAIL_DATA_COLUMN = 3;
//        final static int CONTACT_PHONE_NUMBER_COLUMN = 4;
//    }


}
