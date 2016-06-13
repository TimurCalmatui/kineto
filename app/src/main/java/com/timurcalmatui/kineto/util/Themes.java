package com.timurcalmatui.kineto.util;

import android.content.Context;
import android.content.res.TypedArray;

/**
 * @author Timur Calmatui
 * @since 2015-08-20.
 */
public class Themes
{
    private static final int[] TEMP_ARRAY = new int[1];

    /**
     * @see android.support.v7.internal.widget.ThemeUtils#getThemeAttrColor(Context, int)
     */
    public static int getThemeAttrColor(Context context, int attr)
    {
        TEMP_ARRAY[0] = attr;
        @SuppressWarnings("ConstantConditions")
        TypedArray a = context.obtainStyledAttributes(null, TEMP_ARRAY);
        try
        {
            return a.getColor(0, 0);
        }
        finally
        {
            a.recycle();
        }
    }
}
