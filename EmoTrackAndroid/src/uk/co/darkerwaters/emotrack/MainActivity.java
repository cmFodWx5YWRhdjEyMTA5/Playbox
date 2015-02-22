package uk.co.darkerwaters.emotrack;

import java.net.CookieManager;
import java.util.Locale;

import com.gdevelop.gwt.syncrpc.LoginUtils;
import com.gdevelop.gwt.syncrpc.android.CookieManagerAvailableListener;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {

	public static int MY_REQUEST_ID = 2222;
	public static final String BASE_URL = "http://1-dot-coral-velocity-820.appspot.com/";
	// Use this for Hosted Emulator Loopback Interface
	//public static final String BASE_URL = "http://10.0.2.2:8888";
	CookieManager cm;

	CookieManagerAvailableListener listener;

	boolean waitForCM = false;
	private Account account;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Button activate = (Button) this.findViewById(R.id.activate);
		activate.setEnabled(false);
		activate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				RpcTrackPointAction ab = new RpcTrackPointAction(MainActivity.this.cm);
				ab.execute(MainActivity.this);
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == MY_REQUEST_ID) {
			if (resultCode == RESULT_OK) {
				Account account = (Account) data.getExtras().get(LoginUtils.ACCOUNT_KEY);
				try {
					LoginUtils.loginAppEngine(this, this.listener, account);
					this.waitForCM = true;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		AccountManager accountManager = AccountManager.get(getApplicationContext());
        Account[] accounts = accountManager.getAccountsByType("com.google");
        if (null != accounts && accounts.length == 1) {
        	this.account = accounts[0];
        }
        else if (null != accounts && accounts.length > 0) {
        	// TODO handle multiple accounts
        	this.account = accounts[0];
        }
        else {
        	// TODO handle no account
        	this.account = null;
        }
        
		this.listener = new CookieManagerAvailableListener() {
			@Override
			public void onAuthFailure() {
				throw new RuntimeException("Authentication Failed");
			}

			@Override
			public void onCMAvailable(CookieManager cm) {
				MainActivity.this.cm = cm;
				Button activate = (Button) MainActivity.this.findViewById(R.id.activate);
				activate.setEnabled(true);
			}
		};
		if (this.cm == null && !this.waitForCM) {
			try {
				LoginUtils.useAccountSelector(false);
				// Test mode
				LoginUtils.setLoginUrl(BASE_URL, true);
				LoginUtils.loginAppEngine(this, this.listener, this.account, MY_REQUEST_ID);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
}
