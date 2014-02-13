package com.spacecaker.xzlockscreen.settings;

import com.spacecaker.xzlockscreen.KeyguardService;
import com.spacecaker.xzlockscreen.R;

import net.margaritov.preference.colorpicker.ColorPickerPreference;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class Settings extends PreferenceActivity implements
		OnPreferenceClickListener {

	public static class Keys {
		public static final String SPACEXZ_LOCKER_ENABLED = "spacexz_locker_enabled";
	}

	private CheckBoxPreference mEnabled;
	protected String clock;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		addPreferencesFromResource(R.xml.settings);

		mEnabled = (CheckBoxPreference) findPreference(Keys.SPACEXZ_LOCKER_ENABLED);
		mEnabled.setOnPreferenceClickListener(this);
		
        ((ColorPickerPreference)findPreference("clockColor")).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

 			@Override
 			public boolean onPreferenceChange(Preference preference, Object newValue) {
 				clock = (ColorPickerPreference.convertToARGB(Integer.valueOf(String.valueOf(newValue))));;
 				preference.setSummary(clock);
 	            SharedPreferences sharedPreferences = getSharedPreferences("SpacePrefsFile",MODE_PRIVATE);
 	            SharedPreferences.Editor editor = sharedPreferences.edit(); //opens the editor
 	            editor.putString("clockColor", clock); //true or false
 	            editor.commit();
 				Intent intent = new Intent();
 				intent.setAction("com.spacecaker.xzlockscreen.CHANGE_CLOCK_COLOR");
 				intent.putExtra("clockcolor",clock.toString());
 				sendBroadcast(intent);				
 				return false;
 			}

         });
         String clockcolor = getSharedPreferences("SpacePrefsFile",MODE_PRIVATE).getString("clockColor","#ffffffff");
 	    
 	    ((ColorPickerPreference)findPreference("clockColor")).setDefaultValue(clockcolor);
 	    ((ColorPickerPreference)findPreference("clockColor")).setSummary(clockcolor);			
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (preference.equals(mEnabled)) {
			Context context = getBaseContext();
			Intent intent = new Intent(context, KeyguardService.class);
			if (mEnabled.isChecked()) {
				context.startService(intent);
			} else {
				context.stopService(intent);
			}
		}
		return false;
	}
}