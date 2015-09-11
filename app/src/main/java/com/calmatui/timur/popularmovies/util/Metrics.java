package com.calmatui.timur.popularmovies.util;

import android.content.Context;

/**
 * @author Timur Calmatui
 * @since 2015-09-08.
 */
public class Metrics
{
    public static int dpToPx(Context context, float dp)
    {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
