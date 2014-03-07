package com.smartengine.ohnomessaging.dbhelper;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class SavedMessageDatabase  extends SQLiteOpenHelper{

	
	private static String Databasename = "MESSAGE";
	private static int Databasevertion = 1;
	private String savedmessagetable="savedmessage";
	private String presetmessagetable="presetmessage";
	 public SavedMessageDatabase(Context context) {
		super(context, Databasename, null, Databasevertion);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		
		String sql= "create table "+ savedmessagetable+ "("+ "_id "+ "INTEGER PRIMARY KEY AUTOINCREMENT," +
				" msg "+" TEXT)";
		db.execSQL(sql);
		sql= "create table "+ presetmessagetable+ "("+ "_id "+ "INTEGER PRIMARY KEY AUTOINCREMENT," +
				" msg "+" TEXT)";
		db.execSQL(sql);
		
		
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
		
	}
	public void insertsavedmessage(String body)
	{
		SQLiteDatabase db=this.getWritableDatabase();
		ContentValues values=new ContentValues();
		values.put("msg",body);
		db.insert(savedmessagetable,null, values);
		db.close();
		
	}
	public ArrayList<String> getsavedMessages()
	{
		ArrayList<String> list=new ArrayList<String>();
		SQLiteDatabase db=this.getReadableDatabase();
		String[] colums={"msg"};
		Cursor c=db.query(savedmessagetable, colums, null,null,null,null,null);
		while(c.moveToNext())
			list.add(c.getString(0));
		c.close();
		db.close();
		return list;
	}

}
