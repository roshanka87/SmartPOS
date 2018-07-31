package com.example.admin.smartpos;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class activity_login extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		View decorView = getWindow().getDecorView();
		int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
		decorView.setSystemUiVisibility(uiOptions);
		ActionBar actionBar = getSupportActionBar();
		actionBar.hide();

		Button button = (Button) findViewById(R.id.button_LogIn);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				EditText txtUserName = (EditText) findViewById(R.id.editText_UserName);
				EditText txtPassword = (EditText) findViewById(R.id.editText_Password);

				Intent Main = new Intent(activity_login.this, activity_main.class);
				startActivity(Main);

				finish();
			}
		});


		TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

		if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
			String IMEI_Number_Holder = telephonyManager.getDeviceId();
			Log.i("IMEI Reader", "IMEI Number: "+IMEI_Number_Holder);
		}


	}




	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			System.exit(0);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
