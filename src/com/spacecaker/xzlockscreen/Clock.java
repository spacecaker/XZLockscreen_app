package com.spacecaker.xzlockscreen;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.TextView;

public class Clock extends TextView {
	
 private TextView clock;
 
	 public Clock(final Context context, AttributeSet attrs) {
		  super(context, attrs);

			clock = (TextView) findViewById(R.id.clock);
	 	    SharedPreferences sharedPreferences = context.getSharedPreferences("SpacePrefsFile",Context.MODE_PRIVATE); 
	 	    String clockColor = sharedPreferences.getString("clockColor","#ffffffff");
	   	    clock.setTextColor(Color.parseColor(clockColor));
	        Boolean ClockVisibility = sharedPreferences.getBoolean("clockVisibility", false);
	        if (ClockVisibility == false){
	        clock.setVisibility(0);
	        } else {
		        clock.setVisibility(GONE);
	        }  
	   		        
              final Handler h = new Handler();
              h.post(new Runnable() {
                  @Override
                  public void run() {
                      updateTime();
                      h.postDelayed(this, 1000);
                  }
              }); 
              BroadcastReceiver mclockcolorReceiver = new BroadcastReceiver() {
                  @Override
                  public void onReceive(Context c, Intent i) {
                	  String clockColor = i.getStringExtra("clockcolor");
                	  clock.setTextColor(Color.parseColor(clockColor));
                      SharedPreferences sharedPreferences = context.getSharedPreferences("SpacePrefsFile",Context.MODE_PRIVATE);
                      SharedPreferences.Editor editor = sharedPreferences.edit(); //opens the editor
                      editor.putString("clockColor", clockColor); //true or false
                      editor.commit();	 
                  }
                  
              };
              BroadcastReceiver mclockHideReceiver = new BroadcastReceiver() {
                  @Override
                  public void onReceive(Context c, Intent i) {
                	  clock.setVisibility(GONE);
                      SharedPreferences sharedPreferences = context.getSharedPreferences("SpacePrefsFile",Context.MODE_PRIVATE);
                      SharedPreferences.Editor editor = sharedPreferences.edit(); //opens the editor
                      editor.putBoolean("clockVisibility", true); //true or false
                      editor.commit();	 
                  }
                  
              };
              BroadcastReceiver mclockUnhideReceiver = new BroadcastReceiver() {
                  @Override
                  public void onReceive(Context c, Intent i) {
                	  clock.setVisibility(VISIBLE);
                      SharedPreferences sharedPreferences = context.getSharedPreferences("SpacePrefsFile",Context.MODE_PRIVATE);
                      SharedPreferences.Editor editor = sharedPreferences.edit(); //opens the editor
                      editor.putBoolean("clockVisibility", false); //true or false
                      editor.commit();	 
                  }
                  
              };
              
              context.registerReceiver(mclockcolorReceiver, new IntentFilter("com.spacecaker.xzlockscreen.CHANGE_CLOCK_COLOR"));
              context.registerReceiver(mclockHideReceiver, new IntentFilter("com.spacecaker.xzlockscreen.HIDE_CLOCK"));
              context.registerReceiver(mclockUnhideReceiver, new IntentFilter("com.spacecaker.xzlockscreen.UNHIDE_CLOCK"));
	 }
	 
	void updateTime() {
		  Calendar cal = Calendar.getInstance();
          int hour = cal.get(Calendar.HOUR);
          SimpleDateFormat ampm = new SimpleDateFormat("aa");
          String am_pm = ampm.format(cal.getTime());
          int min = cal.get(Calendar.MINUTE);
          if (min < 10) {
        	  
        	  if (hour < 10){
        		  if (hour == 0){
                      clock.setText("12"+":"+"0"+min);
                	  }
        		  else {
        			  clock.setText("0"+hour+":"+"0"+min);	  
        		  }
        	  }
        	  else {
                  clock.setText(hour+":"+"0"+min);  
        	  }
          }
          else {
        	  if (hour < 10){
        		  if (hour == 0){
                      clock.setText("12"+":"+min);
                	  }
        		  else {
        			  clock.setText("0"+hour+":"+min);	  
        		  }
        	  }
        	  else {
                  clock.setText(hour+":"+min);  
        	  }
          }	
		
	}
 
}
