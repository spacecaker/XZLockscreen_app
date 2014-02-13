package com.spacecaker.xzlockscreen;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;

public class XZLockscreen extends Activity implements
		KeyguardScreenCallback {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(new XZLockscreenPropane(this, this));
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return keyCode == KeyEvent.KEYCODE_HOME
				|| keyCode == KeyEvent.KEYCODE_BACK;
	}

	@Override
	public void onAttachedToWindow() {

		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
			getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
		} else {
		}

		super.onAttachedToWindow();
	}

	@Override
	public void goToUnlockScreen() {
		finish();
	}
}
