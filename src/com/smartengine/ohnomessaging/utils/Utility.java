package com.smartengine.ohnomessaging.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Utility {
    
    public static String[] month_name = {
      "Jan", "Feb", "Mar", "Apr", "May", "Jun",
      "Jul", "Aug", "Sep", "Act", "Nov", "Dec"
    };

    private static Calendar clearTimes(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY,0);
        c.set(Calendar.MINUTE,0);
        c.set(Calendar.SECOND,0);
        c.set(Calendar.MILLISECOND,0);
        return c;
    }
    
    
    public static String getFormattedTime(String millisInStr){
        if(millisInStr == null)
            return null;
        long millis = Long.parseLong(millisInStr);
        if(isToday(millis)){
            return formatTOdayTime(millis);
        }
        else{
            return formatTime(millis);
        }
    }
    
    
    public static String formatTOdayTime(long millis){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm a");
        dateFormat.setCalendar(cal);
        return dateFormat.format(cal.getTime());
    }

    public static Boolean isToday(long millis) {
        Calendar today = Calendar.getInstance();
        today = clearTimes(today);

        if(millis > today.getTimeInMillis())
            return true;
        return false;
    }

    public static String formatTime(long millis){
        
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        
        String timeOfMsg = month_name[month] + " " + day;
        
        long currentMillis = System.currentTimeMillis();
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(currentMillis);
        
        int currentYear = c.get(Calendar.YEAR);
        if(year != currentYear)
            timeOfMsg = timeOfMsg + ", " + year;
        
        return timeOfMsg;
    }

}
