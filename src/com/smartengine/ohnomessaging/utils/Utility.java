package com.smartengine.ohnomessaging.utils;

import java.io.FileDescriptor;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

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
    
    
    public static int calculateInSampleSize(BitmapFactory.Options options,
            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
            // with both dimensions larger than or equal to the requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down further
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }
    
    
    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }
    
    public static Bitmap decodeSampledBitmapFromDescriptor(
            FileDescriptor fileDescriptor, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
    }
    
    
    public static String trimLastComma(String str) {
        if (str == null || str.equals("")) {
            return "";
        } else {
            try{
                str = str.substring(0, str.length()-2);
                int index = str.lastIndexOf(",");
                if(index != -1){
                    str = str.substring(0, index) + " and " + str.substring(index+1);
                }
            }catch (Exception e) {
                str = "";
            }
            return str;
        }
    }

}
