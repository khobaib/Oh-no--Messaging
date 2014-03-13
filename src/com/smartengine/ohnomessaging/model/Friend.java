package com.smartengine.ohnomessaging.model;

import android.os.Parcelable;

public class Friend{
	private String name;
	private String uid;
	private String birthDay;
	public Friend(String name, String uid, String brthday) {
		this.name=name;
		this.uid=uid;
		this.birthDay=brthday;
		
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

}
