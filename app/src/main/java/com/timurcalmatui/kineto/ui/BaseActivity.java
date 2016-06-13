package com.timurcalmatui.kineto.ui;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;

/**
 * @author Timur Calmatui
 * @since 2015-08-18.
 */
public abstract class BaseActivity extends AppCompatActivity
{
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent()
    {
        Intent parentIntent = super.getParentActivityIntent();
        if (parentIntent != null)
        {
            parentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        
        return parentIntent;
    }
}
