package com.spacecaker.xzlockscreen;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class XZLockscreenPropane extends RelativeLayout {

	private KeyguardScreenCallback mCallback;

	private LinearLayout mContentLayout;

	private Calendar mCalendar;
	private ContentObserver mFormatChangeObserver;
	private boolean mAttached;

	private TextView mDateView;

	private final Handler mHandler = new Handler();
	private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
				mCalendar = Calendar.getInstance();
			}
			mHandler.post(new Runnable() {
				public void run() {
					updateContent();
				}
			});
		}
	};

	public XZLockscreenPropane(Context context,
			KeyguardScreenCallback callback) {
		super(context);
		mCallback = callback;

		final LayoutInflater inflater = LayoutInflater.from(context);
		mContentLayout = (LinearLayout) inflater.inflate(
				R.layout.keyguard_xzlockscreen, null, true);
		addView(mContentLayout, new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		mDateView = (TextView) mContentLayout.findViewById(R.id.date);

		// Clock and date
		mCalendar = Calendar.getInstance();
		
		XZLockscreenFCKU xZLockscreenFCKU = new XZLockscreenFCKU(context,
				new XZLockscreenFCKU.OnActionListener() {

					@Override
					public void onAction() {
						if (mCallback != null) {
							mCallback.goToUnlockScreen();
						}
					}

					@Override
					public void onTouchDown(float x, float y) {
					}

					@Override
					public void onTouchUp() {
					}

				});
		addView(xZLockscreenFCKU);

		setFocusable(true);
		setFocusableInTouchMode(true);
		setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);

	}

	@SuppressLint({ "SimpleDateFormat", "DefaultLocale" })
	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();

		if (mAttached)
			return;
		mAttached = true;

		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_TIME_TICK);
		filter.addAction(Intent.ACTION_TIME_CHANGED);
		filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		getContext().registerReceiver(mIntentReceiver, filter);

		mFormatChangeObserver = new FormatChangeObserver();
		getContext().getContentResolver().registerContentObserver(
				Settings.System.CONTENT_URI, true, mFormatChangeObserver);

		updateContent();
	}

	private class FormatChangeObserver extends ContentObserver {
		public FormatChangeObserver() {
			super(new Handler());
		}

		@Override
		public void onChange(boolean selfChange) {
			updateContent();
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();

		if (!mAttached)
			return;
		mAttached = false;

		getContext().unregisterReceiver(mIntentReceiver);
		getContext().getContentResolver().unregisterContentObserver(
				mFormatChangeObserver);
	}

	private void updateContent() {
		mCalendar.setTimeInMillis(System.currentTimeMillis());

		mDateView.setText(new SimpleDateFormat("EEE, MMMM d").format(
				mCalendar.getTime()).toUpperCase());
	}
}
