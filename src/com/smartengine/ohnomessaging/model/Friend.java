package com.smartengine.ohnomessaging.model;

import android.os.Parcelable;

public class Friend{
	private String name;
	private String uid;
	private String birthDay;
	private String picurl;
	public Friend(String name, String uid, String brthday,String purl) {
		this.name=name;
		this.uid=uid;
		this.birthDay=brthday;
		this.picurl=purl;
		
	}
	public void setName(String name)
	{
		this.name=name;
	}
	public void setUid(String uid)
	{
		this.uid=uid;
	}
	public void setBirthDay(String birthDay)
	{
		this.birthDay=birthDay;
	}
	public void setPicUrl(String url)
	{
		this.setPicUrl(url);
	}
	public String getName()
	{
		return this.name;
	}
	public String getUid()
	{
		return this.uid;
	}
	public String getBirthDay()
	{
		return this.birthDay;
	}
	public String getPicUrl()
	{
		return this.picurl;
	}
	 @Override
	    public String toString() { return name; }

}
