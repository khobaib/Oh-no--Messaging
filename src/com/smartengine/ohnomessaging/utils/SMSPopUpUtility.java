package com.smartengine.ohnomessaging.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSPopUpUtility {
	public static final Uri MMS_SMS_CONTENT_URI = Uri
			.parse("content://mms-sms/");
	public static final int MESSAGE_TYPE_SMS = 0;
	public static final Uri SMS_CONTENT_URI = Uri.parse("content://sms");
	private static final String UNREAD_CONDITION = "read=0";
	public static final Uri THREAD_ID_CONTENT_URI = Uri.withAppendedPath(
			MMS_SMS_CONTENT_URI, "threadID");
	public static final Uri CONVERSATION_CONTENT_URI = Uri.withAppendedPath(
			MMS_SMS_CONTENT_URI, "conversations");
	private Context context;
	private long timestamp;
	private String messageBody;
	String contactName;
	String contactId;
	String contactLookupKey;
	public static final int READ_THREAD = 1;

	public static void deleteMessage(Context context, long messageId,
			long threadId, int messageType) {
		Uri deleteUri = null;
		Log.v("msg", "ind elete" + threadId + " " + messageId);
		if (messageId > 0) {

			// We need to mark this message read first to ensure the entire
			// thread is marked as read
			// setMessageRead(context, messageId, messageType);

			// Construct delete message uri

			if (SMSPopUpUtility.MESSAGE_TYPE_SMS == messageType)
				deleteUri = Uri.withAppendedPath(SMS_CONTENT_URI,
						String.valueOf(messageId));
			else
				return;
		}

		int count = 0;
		try {
			count = context.getContentResolver().delete(deleteUri, null, null);
		} catch (Exception e) {

		}
		if (count == 1) {
			// TODO: should only set the thread read if there are no more
			// unread messages
			// setThreadRead(context, threadId);
		}
		Log.v("msg", "in delete");
	}

	public static class ContactIdentification {
		public String contactId = null;
		public String contactLookup = null;
		public String contactName = null;

		public ContactIdentification(String _contactId, String _contactLookup,
				String _contactName) {
			contactId = _contactId;
			contactLookup = _contactLookup;
			contactName = _contactName;
		}
	}

	public void SmsMmsMessage(Context _context, SmsMessage[] messages,
			long _timestamp) {
		SmsMessage sms = messages[0];

		context = _context;
		timestamp = _timestamp;
		// messageType = MESSAGE_TYPE_SMS;

		/*
		 * Fetch data from raw SMS
		 */
		String fromAddress = sms.getDisplayOriginatingAddress();
		boolean fromEmailGateway = sms.isEmail();
		// messageClass = sms.getMessageClass();

		String body = "";

		try {
			if (messages.length == 1 || sms.isReplace()) {
				body = sms.getDisplayMessageBody();
			} else {
				StringBuilder bodyText = new StringBuilder();
				for (int i = 0; i < messages.length; i++) {
					bodyText.append(messages[i].getMessageBody());
				}
				body = bodyText.toString();
			}
		} catch (Exception e) {

		}
		messageBody = body;

		/*
		 * Lookup the rest of the info from the system db
		 */

		ContactIdentification contactIdentify = null;

		// If this SMS is from an email gateway then lookup contactId by email
		// address
		if (fromEmailGateway) {
			Log.v("msg", "from email");
			// contactIdentify = SmsPopupUtils.getPersonIdFromEmail(context,
			// fromAddress);
			// contactName = fromAddress;
		} else { // Else lookup contactId by phone number
			contactIdentify = this.getPersonIdFromPhoneNumber(context,
					fromAddress);
			contactName = PhoneNumberUtils.formatNumber(fromAddress);
		}

		if (contactIdentify != null) {
			contactId = contactIdentify.contactId;
			contactLookupKey = contactIdentify.contactLookup;
			contactName = contactIdentify.contactName;
			Log.v("msg", contactId + contactLookupKey + contactName);
		}

		// unreadCount = SmsPopupUtils.getUnreadMessagesCount(context,
		// timestamp, messageBody);
	}

	public String getBody(Context contxt, SmsMessage[] messages) {
		SmsMessage sms = messages[0];

		// messageType = MESSAGE_TYPE_SMS;

		/*
		 * Fetch data from raw SMS
		 */
		String fromAddress = sms.getDisplayOriginatingAddress();
		boolean fromEmailGateway = sms.isEmail();
		// messageClass = sms.getMessageClass();

		String body = "";

		try {
			if (messages.length == 1 || sms.isReplace()) {
				body = sms.getDisplayMessageBody();
			} else {
				StringBuilder bodyText = new StringBuilder();
				for (int i = 0; i < messages.length; i++) {
					bodyText.append(messages[i].getMessageBody());
				}
				body = bodyText.toString();
			}
		} catch (Exception e) {

		}
		messageBody = body;
		Log.v("body", body);
		return body;

	}

	public String getAddress(Context _context, SmsMessage[] messages) {
		SmsMessage sms = messages[0];

		context = _context;
		// timestamp = _timestamp;
		// messageType = MESSAGE_TYPE_SMS;

		/*
		 * Fetch data from raw SMS
		 */
		String fromAddress = sms.getDisplayOriginatingAddress();
		return fromAddress;
	}

	public static ContactIdentification getPersonIdFromPhoneNumber(
			Context context, String address) {

		if (address == null) {
			return null;
		}

		Cursor cursor = null;
		try {
			cursor = context.getContentResolver().query(
					Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
							Uri.encode(address)),
					new String[] { PhoneLookup._ID, PhoneLookup.DISPLAY_NAME,
							PhoneLookup.LOOKUP_KEY }, null, null, null);
		} catch (IllegalArgumentException e) {
			// Log.e("getPersonIdFromPhoneNumber(): " + e.toString());
			return null;
		} catch (Exception e) {
			// Log.e("getPersonIdFromPhoneNumber(): " + e.toString());
			return null;
		}

		if (cursor != null) {
			try {
				if (cursor.getCount() > 0) {
					cursor.moveToFirst();
					String contactId = String.valueOf(cursor.getLong(0));
					String contactName = cursor.getString(1);
					String contactLookup = cursor.getString(2);

					return new ContactIdentification(contactId, contactLookup,
							contactName);
				}
			} finally {
				cursor.close();
			}
		}

		return null;
	}

	public long findThreadIdFromAddress(Context context, String address) {
		if (address == null)
			return 0;

		String THREAD_RECIPIENT_QUERY = "recipient";

		Uri.Builder uriBuilder = THREAD_ID_CONTENT_URI.buildUpon();
		uriBuilder.appendQueryParameter(THREAD_RECIPIENT_QUERY, address);

		long threadId = 0;

		Cursor cursor = null;
		try {

			cursor = context.getContentResolver().query(uriBuilder.build(),
					new String[] { Contacts._ID }, null, null, null);

			if (cursor != null && cursor.moveToFirst()) {
				threadId = cursor.getLong(0);
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return threadId;
	}

	synchronized public static long findMessageId(Context context,
			long threadId, long timestamp, String body, int messageType) {

		Log.v("msg", "body" + body);
		long id = 0;
		String selection = "body = "
				+ DatabaseUtils.sqlEscapeString(body != null ? body : "");
		selection += " and " + UNREAD_CONDITION;
		final String sortOrder = "date DESC";
		final String[] projection = new String[] { "_id", "date", "thread_id",
				"body" };

		if (threadId > 0) {
			Cursor cursor = context.getContentResolver().query(
					ContentUris.withAppendedId(CONVERSATION_CONTENT_URI,
							threadId), projection, selection, null, sortOrder);

			try {
				if (cursor != null && cursor.moveToFirst()) {
					id = cursor.getLong(0);
				} else
					Log.v("msg", "null");

			} finally {
				cursor.close();
			}
		}
		Log.v("msg", "thread id " + threadId + "  " + id);

		return id;
	}

	public static void setMessageRead(Context context, long messageId, int messageType) {

		SharedPreferences myPrefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		/*
		 * boolean markRead = myPrefs.getBoolean(
		 * context.getString(R.string.pref_markread_key),
		 * Defaults.PREFS_MARK_READ); if (!markRead) { return; }
		 */

		if (messageId > 0) {
			ContentValues values = new ContentValues(1);
			values.put("read", READ_THREAD);

			Uri messageUri;

			if (SMSPopUpUtility.MESSAGE_TYPE_SMS == messageType) {
				messageUri = Uri.withAppendedPath(SMS_CONTENT_URI,
						String.valueOf(messageId));
			} else {
				return;
			}

			// Log.v("messageUri for marking message read: " +
			// messageUri.toString());

			ContentResolver cr = context.getContentResolver();
			int result;
			try {
				result = cr.update(messageUri, values, null, null);
			} catch (Exception e) {
				result = 0;
			}

		}
	}

}
