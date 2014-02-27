package com.smartengine.ohnomessaging;

import java.util.ArrayList;

import com.ohnomessaging.R;
import com.smartengine.ohnomessaging.adapter.ContactListadapter;
import com.smartengine.ohnomessaging.model.Contact;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class SettingsActivity extends Activity implements OnClickListener {
	private Button btnsavepaswrd;
	private Button btnrchangepassword;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_activity);
		btnsavepaswrd = (Button) findViewById(R.id.buttonsetpasswrd);
		btnrchangepassword = (Button) findViewById(R.id.buttonchangepasswrd);
		btnsavepaswrd.setOnClickListener(this);
		btnrchangepassword.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.buttonsetpasswrd) {

			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(SettingsActivity.this);
			String password = prefs.getString("password", "");
			if (password.equals(""))
				showPasswrdSetDialog();
			else
				toast("You have already set  a password");

		}
		else if(v.getId()==R.id.buttonchangepasswrd)
			showPasswordChangeDialog();

	}

	public void showPasswrdSetDialog() {
		final Dialog dialog = new Dialog(SettingsActivity.this);
		dialog.setContentView(R.layout.set_password);
		dialog.setTitle("Set Password");
		final EditText edtxtpasswrd = (EditText) dialog
				.findViewById(R.id.editTextpasswrd);
		final EditText edttxtconfirmpassrd = (EditText) dialog
				.findViewById(R.id.editTextconfirmpasswrd);
		Button btnsave = (Button) dialog.findViewById(R.id.buttonsavepasswrd);
		btnsave.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (edttxtconfirmpassrd.getText().toString()
						.equals(edtxtpasswrd.getText().toString())) {
					SharedPreferences.Editor editor = PreferenceManager
							.getDefaultSharedPreferences(SettingsActivity.this)
							.edit();
					editor.putString("password", edtxtpasswrd.getText()
							.toString());
					editor.commit();
					dialog.dismiss();
					toast("Password Saved");
				} else {
					toast("Passwprd Mismatch");
				}

			}
		});
		dialog.show();
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		dialog.getWindow().setLayout(metrics.heightPixels, metrics.widthPixels);
	}

	private void showPasswordChangeDialog() {
		final Dialog dialog = new Dialog(SettingsActivity.this);
		dialog.setContentView(R.layout.set_password);
		dialog.setTitle("Change Password");
		final EditText editTextPasswardNow = (EditText) dialog
				.findViewById(R.id.editTextinputpass);

		final EditText edtTxtPassword = (EditText) dialog
				.findViewById(R.id.editTextpasswrd);
		final EditText edtTxtPasswrdConfirmd = (EditText) dialog
				.findViewById(R.id.editTextconfirmpasswrd);
		Button btnsave = (Button) dialog.findViewById(R.id.buttonsavepasswrd);

		editTextPasswardNow.setVisibility(View.VISIBLE);
		editTextPasswardNow.setHint("Enter Current Password");
		edtTxtPassword.setHint("Enter New Password");
		edtTxtPasswrdConfirmd.setHint("Confirm New Password");
		btnsave.setText("Change Password");

		btnsave.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SharedPreferences pref = PreferenceManager
						.getDefaultSharedPreferences(SettingsActivity.this);
				String password = pref.getString("password", "-1");
				if (password.equals(editTextPasswardNow.getText().toString())) {

					if (edtTxtPasswrdConfirmd.getText().toString()
							.equals(edtTxtPassword.getText().toString())) {
						SharedPreferences.Editor editor = PreferenceManager
								.getDefaultSharedPreferences(
										SettingsActivity.this).edit();
						editor.putString("password", edtTxtPassword.getText()
								.toString());
						editor.commit();
						dialog.dismiss();
						toast("New Password Saved");
					} else {
						toast("Passwprd Mismatch");
					}
				}
				else
					toast("Incorrect Password");

			}
		});
		dialog.show();
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		dialog.getWindow().setLayout(metrics.heightPixels, metrics.widthPixels);
	}

	private void toast(String str) {
		Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG).show();
	}
}
