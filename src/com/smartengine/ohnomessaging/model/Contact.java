package com.smartengine.ohnomessaging.model;

import android.graphics.Bitmap;

public class Contact{

    /**
     * 
     */
//    private static final long serialVersionUID = 1L;
    private String displayName;
    private Bitmap image;
    private long id;
    private String lookupKey;
    private String phoneNumber;

    public Contact() {
        // TODO Auto-generated constructor stub
    }

    public Contact(String displayName, Bitmap image, long id, String lookupKey, String phoneNumber) {
        this.displayName = displayName;
        this.image = image;
        this.id = id;
        this.lookupKey = lookupKey;
        this.phoneNumber = phoneNumber;
    }
    
//    public Contact(Parcel in) {
//        String[] data = new String[1];
//        in.readStringArray(data);
//        this.displayName = data[0];
//    }
    
    
    public String getDisplayName() {
        return displayName;
    }
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    public Bitmap getImage() {
        return image;
    }
    public void setImage(Bitmap image) {
        this.image = image;
    }
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getLookupKey() {
        return lookupKey;
    }
    public void setLookupKey(String lookupKey) {
        this.lookupKey = lookupKey;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String toString() { return displayName; }

//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeStringArray(new String[] { this.displayName });
//    }
//
//    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
//        public Contact createFromParcel(Parcel in) {
//            return new Contact(in);
//        }
//
//        public Contact[] newArray(int size) {
//            return new Contact[size];
//        }
//    };
    
    
    

}
