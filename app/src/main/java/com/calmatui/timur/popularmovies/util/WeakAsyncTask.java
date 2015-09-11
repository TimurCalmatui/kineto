package com.calmatui.timur.popularmovies.util;

import android.os.AsyncTask;

import java.lang.ref.WeakReference;

/**
 * @author Timur Calmatui
 * @since 2015-09-11.
 */
public class WeakAsyncTask extends AsyncTask<Void, Void, Void>
{
    public interface WeakAsyncTaskCallbacks
    {
        void onAsyncTaskDoInBackground(int id);
    }
    
    private int mId;
    private WeakReference<WeakAsyncTaskCallbacks> mWeakCallbacks;
    
    public WeakAsyncTask(int id, WeakAsyncTaskCallbacks callbacks)
    {
        mId = id;
        mWeakCallbacks = new WeakReference<>(callbacks);
    }
    
    @Override
    protected Void doInBackground(Void... params)
    {
        WeakAsyncTaskCallbacks callbacks = mWeakCallbacks.get();
        if (callbacks != null)
        {
            callbacks.onAsyncTaskDoInBackground(mId);
        }
        
        return null;
    }
}
