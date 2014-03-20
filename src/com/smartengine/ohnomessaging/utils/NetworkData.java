package com.smartengine.ohnomessaging.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkData {

	private static final String LOG_TAG = null;

	public boolean hasActiveInternetConnection(Context context) {
		if (isNetworkAvailable(context)) {
			try {
				HttpURLConnection urlc = (HttpURLConnection) (new URL(
						"http://www.google.com").openConnection());
				urlc.setRequestProperty("User-Agent", "Test");
				urlc.setRequestProperty("Connection", "close");
				urlc.setConnectTimeout(15000);

				urlc.connect();
				return (urlc.getResponseCode() == 200);
			} catch (IOException e) {
				Log.e(LOG_TAG, "Error checking internet connection", e);
			}
		} else {
			Log.d(LOG_TAG, "No network available!");
		}
		return false;
	}

	private static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null;
	}

}
