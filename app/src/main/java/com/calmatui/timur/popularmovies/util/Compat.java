package com.calmatui.timur.popularmovies.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;

/**
 * @author Timur Calmatui
 * @since 2015-08-20.
 */
public class Compat
{
    public static Drawable getTintedDrawable(Context context, int resId, int tint)
    {
        Drawable drawable = ContextCompat.getDrawable(context, resId);
        drawable = drawable.mutate();
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, tint);
        return drawable;
    }
    
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isActivityDestroyed(Activity activity)
    {
        return activity == null || Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
                && activity.isDestroyed();
    }
}
