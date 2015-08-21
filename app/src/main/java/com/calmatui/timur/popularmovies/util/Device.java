package com.calmatui.timur.popularmovies.util;

import android.content.Context;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.view.Display;
import android.view.WindowManager;

/**
 * @author Timur Calmatui
 * @since 2015-08-19.
 */
public class Device
{
    public static int getScreenWidthDp(Context context)
    {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        return pxToDp(context, size.x);
    }

    public static int pxToDp(Context context, int px)
    {
        return Math.round(px / context.getResources().getDisplayMetrics().density);
    }

    public static boolean isConnectedToInternet(Context context)
    {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
}
