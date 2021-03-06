package com.example.give2gether;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.LoggingBehavior;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.model.GraphUser;

public class FacebookLoginActivity extends Activity {
	
	public static String TAG = "naddola";
	List<String> permissions = new ArrayList<String>();
	
	private Session.StatusCallback statusCallback = new SessionStatusCallback();
	private Button buttonLoginLogout;
	TextView facebookstatus;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_facebook_login);

		init();
		dataInit();
		facebookInit(savedInstanceState);

	}

	private void init() {
		buttonLoginLogout = (Button) findViewById(R.id.buttonLoginLogout);
		facebookstatus = (TextView) findViewById(R.id.facebookstatus);
	}

	@SuppressLint("NewApi")
	private void dataInit() {
		// ActionBar Init
		getActionBar().setDisplayShowHomeEnabled(false);
		// getActionBar().setTitle(R.string.board_detail_activity_title);
	}

	private void facebookInit(Bundle savedInstanceState) {
		
		permissions.add("email");
		
		Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

		Session session = Session.getActiveSession();
		if (session == null) {
			if (savedInstanceState != null) {
				session = Session.restoreSession(this, null, statusCallback,
						savedInstanceState);
			}
			if (session == null) {
				session = new Session(this);
			}
			Session.setActiveSession(session);
			if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
				session.openForRead(new Session.OpenRequest(this)
						.setCallback(statusCallback).setPermissions(permissions));
			}
		}

		updateView();
	}

	@Override
	public void onStart() {
		super.onStart();
		Session.getActiveSession().addCallback(statusCallback);
	}

	@Override
	public void onStop() {
		super.onStop();
		Session.getActiveSession().removeCallback(statusCallback);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode,
				resultCode, data);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Session session = Session.getActiveSession();
		Session.saveSession(session, outState);
	}

	private void updateView() {

		Session session = Session.getActiveSession();
		if (session.isOpened()) {
			Request.newMeRequest(session, new Request.GraphUserCallback() {
				@Override
				public void onCompleted(GraphUser user, Response response) {
					// TODO Auto-generated method stub
					if (user != null) {

						TelephonyManager telManager = (TelephonyManager) FacebookLoginActivity.this
								.getSystemService(TELEPHONY_SERVICE);

						Intent intent = new Intent(FacebookLoginActivity.this,
								SignupProcActivity.class);
						String email = user.getProperty("email").toString();
						String name = user.getName();
						String phone = telManager.getLine1Number();
						String birth = user.getBirthday();
						intent.putExtra("email", email);
						intent.putExtra("name", name);
						intent.putExtra("phone", phone);
						intent.putExtra("birth", birth);
						startActivity(intent);
						finish();

						facebookstatus.setText(user.getName());

					} else
						facebookstatus.setText("None");

				}
			}).executeAsync();

			buttonLoginLogout.setText("Logout");
			buttonLoginLogout.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					onClickLogout();
				}
			});

		} else if (!session.isOpened() && !session.isClosed()) {
			session.openForRead(new Session.OpenRequest(this).setPermissions(permissions)
					.setCallback(statusCallback));
		}

	}

	private void onClickLogin() {
		Session session = Session.getActiveSession();
		if (!session.isOpened() && !session.isClosed()) {
			// session.openActiveSession(this, true, statusCallback);
			session.openForRead(new Session.OpenRequest(this)
					.setCallback(statusCallback));
		} else {
			Session.openActiveSession(this, true, statusCallback);

		}
	}

	private void onClickLogout() {
		Session session = Session.getActiveSession();
		if (!session.isClosed()) {
			session.closeAndClearTokenInformation();
		}
	}

	private class SessionStatusCallback implements Session.StatusCallback {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			updateView();
		}
	}

}
