package com.smartengine.ohnomessaging.dbhelper;

import java.util.ArrayList;
import java.util.HashMap;

import com.smartengine.ohnomessaging.model.Contact;
import com.smartengine.ohnomessaging.model.Friend;
import com.smartengine.ohnomessaging.model.TextMessage;

import android.R.array;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class SavedMessageDatabase extends SQLiteOpenHelper {

	private static String Databasename = "MESSAGE";
	private static int Databasevertion = 1;
	private String savedmessagetable = "savedmessage";
	private String presetmessagetable = "presetmessage";
	private String blocklistable = "blocklist";
	private String friendbirthdays = "frinedbirthdays";

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
		sql = "create table " + friendbirthdays + "(" + "_id "
				+ "INTEGER PRIMARY KEY AUTOINCREMENT," + " name " + " TEXT ,"
				+ " phone " + " TEXT, " + " uid " + "TEXT, " + "birthday "
				+ " TEXT," + " profilepic " + " TEXT" + ")";

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

	public void deleteEntryFromBlockList(String number) {

		SQLiteDatabase db = this.getWritableDatabase();
		// String sql="DELETE from "+ blocklistable+" WHERE  phone= "+number;
		// db.execSQL(sql);
		db.delete(blocklistable, "_id = " + getId(number), null);
		db.close();

	}

	public int getId(String number) {
		ArrayList<Contact> list = new ArrayList<Contact>();
		SQLiteDatabase db = this.getReadableDatabase();
		String[] colums = { "_id", "phone" };
		Cursor c = db
				.query(blocklistable, colums, null, null, null, null, null);
		while (c.moveToNext()) {
			if (c.getString(1).equals(number))
				return c.getInt(0);
		}
		c.close();
		db.close();
		return -1;

	}

	public void deleteSavedMessage(String body) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(savedmessagetable, "_id= " + getSavedMessageID(body), null);
		db.close();
	}

	public int getSavedMessageID(String body) {
		SQLiteDatabase db = this.getWritableDatabase();
		String[] colums = { "_id", "msg" };
		Cursor c = db.query(savedmessagetable, colums, null, null, null, null,
				null);
		while (c.moveToNext()) {
			if (c.getString(1).equals(body))
				return c.getInt(0);
		}
		c.close();
		db.close();
		return -1;
	}

	public void deletePresetMessage(String body) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(presetmessagetable, "_id= " + getPresetMessageID(body), null);
		db.close();
	}

	public int getPresetMessageID(String body) {
		SQLiteDatabase db = this.getWritableDatabase();
		String[] colums = { "_id", "msg" };
		Cursor c = db.query(presetmessagetable, colums, null, null, null, null,
				null);
		while (c.moveToNext()) {
			if (c.getString(1).equals(body))
				return c.getInt(0);
		}
		// c.close();
		// db.close();
		return -1;
	}

	public void insertBirthDays(ArrayList<Friend> list) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		int size = list.size();
		for (int i = 0; i < size; i++) {
			values.put("name", list.get(i).getName());
			values.put("uid", list.get(i).getUid());
			values.put("birthday", list.get(i).getBirthDay());
			values.put("profilepic", list.get(i).getPicUrl());
			db.insert(friendbirthdays, null, values);
		}
		db.close();
	}

	public ArrayList<Friend> getFriendBirthDays() {
		ArrayList<Friend> list = new ArrayList<Friend>();
		SQLiteDatabase db = this.getReadableDatabase();
		String[] colums = { "name", "uid", "birthday", "profilepic" };
		Cursor cursor = db.query(friendbirthdays, colums, null, null, null,
				null, null, null);
		while (cursor.moveToNext()) {
			list.add(new Friend(cursor.getString(0), cursor.getString(1),
					cursor.getString(2), cursor.getString(3)));
		}
		cursor.close();
		db.close();
		return list;
	}

	public ArrayList<Contact> getFriendBirthDays2() {
		ArrayList<Contact> list = new ArrayList<Contact>();
		SQLiteDatabase db = this.getReadableDatabase();
		String[] colums = { "name", "uid", "birthday" };
		Cursor cursor = db.query(friendbirthdays, colums, null, null, null,
				null, null, null);
		while (cursor.moveToNext()) {
			Contact contact = new Contact();
			contact.setDisplayName(cursor.getString(0));
			contact.setPhoneNumber(cursor.getString(2));
			list.add(contact);
		}
		cursor.close();
		db.close();
		return list;
	}

	public void deleteAllRecordsFromBirthdays() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(friendbirthdays, null, null);
		db.close();
	}

	public ArrayList<String> getBirthDay(String mon, String day) {
		SQLiteDatabase db = this.getReadableDatabase();
		ArrayList<String> list = new ArrayList<String>();
		String[] colums = { "name", "uid", "birthday" };
		Cursor cursor = db.query(friendbirthdays, colums, null, null, null,
				null, null, null);
		while (cursor.moveToNext()) {
			String birthday = cursor.getString(2);
			String[] array;
			if (birthday.contains(",")) {
				array = birthday.split(",");
				birthday = array[0];
			}
			String date = "";
			if (birthday.contains(" ")) {
				array = birthday.split(" ");
				date = array[1];
			}

			if (birthday.contains(mon) && date.equals(day)) {
				Log.v("msg", date);
				list.add(cursor.getString(0) + "," + cursor.getString(2));
			}

		}
		cursor.close();
		db.close();
		return list;

	}

	public String getMonthAndDate(String birth) {
		String[] array = birth.split(".");
		array = array[0].split(" ");
		return array[0];
	}

	public HashMap<String, String> getFriendList() {
		HashMap<String, String> map = new HashMap<String, String>();
		SQLiteDatabase db = this.getReadableDatabase();
		String[] colums = { "name", "uid", };
		Cursor cursor = db.query(friendbirthdays, colums, null, null, null,
				null, null, null);
		while (cursor.moveToNext())
			map.put(cursor.getString(1), cursor.getString(0));

		cursor.close();
		db.close();
		return map;
	}

}
