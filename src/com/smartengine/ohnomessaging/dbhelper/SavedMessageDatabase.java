package com.smartengine.ohnomessaging.dbhelper;

import java.util.ArrayList;

import com.smartengine.ohnomessaging.model.Contact;
import com.smartengine.ohnomessaging.model.TextMessage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class SavedMessageDatabase extends SQLiteOpenHelper {

	private static String Databasename = "MESSAGE";
	private static int Databasevertion = 1;
	private String savedmessagetable = "savedmessage";
	private String presetmessagetable = "presetmessage";
	private String blocklistable = "blocklist";

	public SavedMessageDatabase(Context context) {
		super(context, Databasename, null, Databasevertion);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		String sql = "create table " + savedmessagetable + "(" + "_id "
				+ "INTEGER PRIMARY KEY AUTOINCREMENT," + " msg " + " TEXT)";
		db.execSQL(sql);
		sql = "create table " + presetmessagetable + "(" + "_id "
				+ "INTEGER PRIMARY KEY AUTOINCREMENT," + " msg " + " TEXT)";
		db.execSQL(sql);
		sql = "create table " + blocklistable + "(" + "_id "
				+ "INTEGER PRIMARY KEY AUTOINCREMENT," + " name " + " TEXT ,"
				+ " phone " + " TEXT " + ")";
		db.execSQL(sql);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	public void insertsavedmessage(String body) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("msg", body);
		db.insert(savedmessagetable, null, values);
		db.close();

	}

	public ArrayList<String> getsavedMessages() {
		ArrayList<String> list = new ArrayList<String>();
		SQLiteDatabase db = this.getReadableDatabase();
		String[] colums = { "msg" };
		Cursor c = db.query(savedmessagetable, colums, null, null, null, null,
				null);
		while (c.moveToNext())
			list.add(c.getString(0));
		c.close();
		db.close();
		return list;
	}

	public ArrayList<String> getPresetMessages() {
		ArrayList<String> list = new ArrayList<String>();
		SQLiteDatabase db = this.getReadableDatabase();
		String[] colums = { "msg" };
		Cursor c = db.query(presetmessagetable, colums, null, null, null, null,
				null);
		while (c.moveToNext())
			list.add(c.getString(0));
		c.close();
		db.close();
		return list;
	}

	public void insertPresetmessage(String body) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("msg", body);
		db.insert(presetmessagetable, null, values);
		db.close();

	}

	public void insertItemToBlockList(String name, String number) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("name", name);
		values.put("phone", number);
		db.insert(blocklistable, null, values);
		db.close();
	}

	public ArrayList<Contact> getBlockedList() {
		ArrayList<Contact> list = new ArrayList<Contact>();
		SQLiteDatabase db = this.getReadableDatabase();
		String[] colums = { "name", "phone" };
		Cursor c = db
				.query(blocklistable, colums, null, null, null, null, null);
		while (c.moveToNext()) {
			Contact contact = new Contact();
			contact.setDisplayName(c.getString(0));
			contact.setPhoneNumber(c.getString(1));
			list.add(contact);
		}
		c.close();
		db.close();
		return list;

	}
	public void  deleteEntryFromBlockList(String number){
		
		SQLiteDatabase db=this.getWritableDatabase();
		//String sql="DELETE from "+ blocklistable+" WHERE  phone= "+number;
		//db.execSQL(sql);
		db.delete(blocklistable,"_id = "+getId(number),null);
		db.close();
		
	}
	public int  getId(String number)
	{
		ArrayList<Contact> list = new ArrayList<Contact>();
		SQLiteDatabase db = this.getReadableDatabase();
		String[] colums = {"_id","phone" };
		Cursor c = db
				.query(blocklistable, colums, null, null, null, null, null);
		while (c.moveToNext()) {
			if(c.getString(1).equals(number))
				return c.getInt(0);
		}
		c.close();
		db.close();
		return -1;
		
	}
	
}
