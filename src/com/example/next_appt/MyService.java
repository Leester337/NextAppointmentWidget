package com.example.next_appt;

import java.util.Date;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Instances;
import android.widget.RemoteViews;

public class MyService extends Service {
	
    long timeOfNextEvent = 0;
    String titleOfNextEvent = "";
    
	@Override
	public void onCreate()
	{
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Date date = new Date();
		System.out.println("Time of Next Event - Date: " + Long.toString(timeOfNextEvent - date.getTime()));
		if ((timeOfNextEvent - date.getTime()) < 0)
		{
			timeOfNextEvent = 0;
		}
		
		buildUpdate();
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	private void buildUpdate()
	{
		final int PROJECTION_ID_INDEX = 0;
        Cursor cal = getCalendars();
	    
        while (cal.moveToNext()) 
        {
		    long calID = 0;
		      
		    // Get the field values
		    calID = cal.getLong(PROJECTION_ID_INDEX);
		    
		    getEventsFromCalendar(calID);
		    getEventsFromInstances(calID);

        }
        
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.main);
		System.out.println("Event: " + titleOfNextEvent);
		System.out.println("Event: " + timeOfNextEvent);


		Date date = new Date();
		long timeLeft = timeOfNextEvent - date.getTime();
		
		long daysLeft = timeLeft/86400000;
		timeLeft = (timeLeft%86400000);
		long hoursLeft = timeLeft/3600000;
		timeLeft = timeLeft%3600000;
		long minutesLeft = timeLeft/60000;
		timeLeft = timeLeft%60000;
		long secondsLeft = timeLeft/1000;
		
		String timeString = "";
		
		if (daysLeft != 0)
		{
			timeString += Long.toString(daysLeft) + " days ";
		}
		
		if (hoursLeft != 0)
		{
			timeString += Long.toString(hoursLeft) + " hours ";
		}
		
		if (minutesLeft != 0)
		{
			timeString += Long.toString(minutesLeft) + " min ";
		}
		
		if (secondsLeft > 30)
			minutesLeft++;
		
		views.setTextViewText(R.id.widget_titleText, titleOfNextEvent);
		views.setTextViewText(R.id.widget_timeText, timeString + "left.");
        // Push update for this widget to the home screen  
        ComponentName thisWidget = new ComponentName(this, ApptWidgetProvider.class);  
        AppWidgetManager manager = AppWidgetManager.getInstance(this);  
        manager.updateAppWidget(thisWidget, views); 
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void checkNextEventFromCal(Cursor eventCursor)
	{
		if (eventCursor.getCount() == 0)
		{
			return;
		}
		System.out.println("Non Zero Cursor!");
    	final int EVENT_TITLE_INDEX = 0;
    	final int EVENT_TIME_INDEX = 1;
		String eventTitle = null;
		long eventTime = 0;
		try
		{
			eventTitle = eventCursor.getString(EVENT_TITLE_INDEX);

		}
		catch (Exception e)
		{
			System.out.println(e);
		}
		try
		{
			eventTime = eventCursor.getLong(EVENT_TIME_INDEX);
		}
		catch (Exception e)
		{
			System.out.println(e);
		}

		if (eventTime != 0)
		{
			if (timeOfNextEvent == 0)
			{
				timeOfNextEvent = eventTime;
				titleOfNextEvent = eventTitle;
			}
			else if (eventTime < timeOfNextEvent)
			{
				timeOfNextEvent = eventTime;
				titleOfNextEvent = eventTitle;
			}
		}
	}
	
	public void getEventsFromInstances(long calID)
	{
        String[] INSTANCE_PROJECTION = new String[] {
        	Instances.TITLE,
            Instances.BEGIN
          };
          

        // Specify the date range you want to search for recurring
        // event instances
        Date date = new Date();
        long startMillis = date.getTime();
        long endMillis = timeOfNextEvent;
        
        if (endMillis == 0)
        {
        	endMillis = startMillis + 999999999999L;
        }
          
        Cursor cur = null;
        ContentResolver cr = getContentResolver();

		String selection = "(Calendar_id=?) AND (allDay = 0) AND (begin>?) AND (end<?) AND (deleted!=1)";
        String[] selectionArgs = new String[] {Long.toString(calID), Long.toString(startMillis), Long.toString(endMillis)};

        // Construct the query with the desired date range.
        Uri.Builder builder = Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, startMillis);
        ContentUris.appendId(builder, endMillis);

        // Submit the query
        cur =  cr.query(
        	builder.build(), 
            INSTANCE_PROJECTION, 
            selection, 
            selectionArgs, 
            "begin ASC limit 1");
        
        System.out.println("Finished Instance Query");
           
        if (cur.getCount() != 0)
        {
        	cur.moveToFirst();
        }
        
        checkNextEventFromCal(cur);
          
	}
	
	
	public void getEventsFromCalendar(long calID)
	{
		String[] EVENT_PROJECTION = new String[] {
	    	    Events.TITLE,
	    	    Events.DTSTART
	    	};

        // Specify the date range you want to search for recurring
        // event instances
        Date date = new Date();
        long startMillis = date.getTime();
        long endMillis = timeOfNextEvent;
        
        if (endMillis == 0)
        {
        	endMillis = startMillis + 999999999999L;
        }
		
		String selection = "(Calendar_id=?) AND (allDay = 0) AND (dtstart>?) AND (dtend<?) AND (deleted!=1)";
        String[] selectionArgs = new String[] {Long.toString(calID), Long.toString(startMillis), Long.toString(endMillis)};
	    
	    Cursor eventCursor = getContentResolver().query(
	    		Events.CONTENT_URI,
                EVENT_PROJECTION,
                selection,
                selectionArgs,
                "dtstart ASC limit 1");
	    
	    System.out.println("Finished Event Query");
            
         if (eventCursor.getCount() != 0)
         {
        	 eventCursor.moveToFirst();
         }
         
         checkNextEventFromCal(eventCursor);
	}
    

	public Cursor getCalendars()
    {
		System.out.println("inCalendars!");
		
		
    	// Projection array. Creating indices for this array instead of doing
    	// dynamic lookups improves performance.
    	final String[] EVENT_PROJECTION = new String[] {
    	    Calendars._ID,                           // 0
    	};
    	
    	
    	//run query
		Cursor cur = null;
		ContentResolver cr = getContentResolver();
		Uri uri = Calendars.CONTENT_URI;   
		String selection = "((" + Calendars.VISIBLE + " = 1) )"; 
		// Submit the query and get a Cursor object back. 
		cur = cr.query(uri, EVENT_PROJECTION, selection, null, Calendars._ID + " ASC");
		System.out.println("finished query!");
		// Use the cursor to step through the returned records
		
		if (cur.getCount() != 0)
        {
			cur.moveToFirst();
        }
		
		return cur;	
    }

}
