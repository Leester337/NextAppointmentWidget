package com.example.next_appt;
import java.io.Console;
import java.util.Calendar;
import java.util.Date;

import com.example.next_appt.R;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Instances;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.RemoteViews;

public class ApptWidgetProvider extends AppWidgetProvider {
	
	private PendingIntent service = null;

	
	@Override
	public void onDisabled(Context context)
	{
		final AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);  
		  
        m.cancel(service); 
       
	}

	@Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		final AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);  
		  
        final Calendar TIME = Calendar.getInstance();  
        TIME.set(Calendar.MINUTE, 0);  
        TIME.set(Calendar.SECOND, 0);  
        TIME.set(Calendar.MILLISECOND, 0);  
  
        final Intent i = new Intent(context, MyService.class);  
  
        if (service == null)  
        {  
            service = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);  
        }  
  
        m.setRepeating(AlarmManager.RTC, TIME.getTime().getTime(), 1000 * 60, service);  
    }
	
	
    
    
}