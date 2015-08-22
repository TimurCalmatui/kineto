package com.calmatui.timur.popularmovies.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.calmatui.timur.popularmovies.R;
import com.calmatui.timur.popularmovies.api.Api;
import com.calmatui.timur.popularmovies.model.ApiError;
import com.calmatui.timur.popularmovies.model.Movie;
import com.calmatui.timur.popularmovies.model.MoviesResponse;
import com.calmatui.timur.popularmovies.util.Device;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * @author Timur Calmatui
 * @since 2015-08-18.
 */
public class MainActivity extends BaseActivity
{
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final boolean DEBUG = false;

    private static final String STATE_SORT_ORDER = "STATE_SORT_ORDER";
    private static final String STATE_MOVIES = "STATE_MOVIES";

    private static final String PREFERENCE_KEY =
            "com.calmatui.timur.popularmovies.MOVIES_LIST_PREFERENCES";

    private RecyclerView mRecyclerView;
    private String mSortOrder = Api.SORT_POPULARITY_DESC;
    private String mNewSortOrder = mSortOrder;

    private TextView mErrorText;
    private Button mRetryButton;
    private ContentLoadingProgressBar mProgressBar;
    private boolean mInProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mErrorText = (TextView) findViewById(R.id.error);
        mRetryButton = (Button) findViewById(R.id.retry);
        mProgressBar = (ContentLoadingProgressBar) findViewById(R.id.progress);

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        GridLayoutManager layoutManager =
                new GridLayoutManager(this, getColumnNumber(), GridLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);

        mRetryButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                refresh();
            }
        });

        mNewSortOrder = mSortOrder = getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE)
                .getString(STATE_SORT_ORDER, mSortOrder);

        ArrayList<Movie> movies = null;
        if (savedInstanceState != null)
        {
            movies = savedInstanceState.getParcelableArrayList(STATE_MOVIES);
        }

        if (movies != null)
        {
            mRecyclerView.setAdapter(new MoviesAdapter(this, movies));
        }
        else
        {
            mProgressBar.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener()
                    {
                        @Override
                        public void onGlobalLayout()
                        {
                            refresh();
                            mProgressBar.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        if (mRecyclerView.getAdapter() != null)
        {
            outState.putParcelableArrayList(STATE_MOVIES,
                    ((MoviesAdapter) mRecyclerView.getAdapter()).getItems());
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE)
                .edit()
                .putString(STATE_SORT_ORDER, mSortOrder)
                .apply();
    }

    private int getColumnNumber()
    {
        return Math.max(2, Math.round(Device.getScreenWidthDp(this) / 200.0f));
    }

    private void refresh()
    {
        refresh(mSortOrder);
    }

    private void refresh(String sortOrder)
    {
        if (mInProgress)
        {
            return;
        }

        setUiInProgress(true);
        Api.getInstance().listMovies(sortOrder, new MoviesResponseCallback(this));
    }

    private void setUiInProgress(boolean inProgress)
    {
        mInProgress = inProgress;

        if (inProgress)
        {
            mErrorText.setVisibility(View.GONE);
            mRetryButton.setVisibility(View.GONE);
            mProgressBar.show();
        }
        else
        {
            mProgressBar.hide();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.sort)
        {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.sort_by)
                    .setSingleChoiceItems(R.array.movie_sort_options,
                            mSortOrder.equals(Api.SORT_POPULARITY_DESC) ? 0 : 1,
                            new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    mNewSortOrder = which == 0
                                            ? Api.SORT_POPULARITY_DESC
                                            : Api.SORT_RATING_DESC;

                                    if (!mNewSortOrder.equals(mSortOrder))
                                    {
                                        refresh(mNewSortOrder);
                                    }

                                    dialog.dismiss();
                                }
                            })
                    .show();

            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private static class MoviesResponseCallback implements Callback<MoviesResponse>
    {
        private WeakReference<MainActivity> mWeakActivity;

        public MoviesResponseCallback(MainActivity mainActivity)
        {
            mWeakActivity = new WeakReference<>(mainActivity);
        }

        @Override
        public void success(MoviesResponse moviesResponse, Response response)
        {
            MainActivity activity = mWeakActivity.get();
            if (activity == null)
            {
                return;
            }

            activity.setUiInProgress(false);
            activity.mSortOrder = activity.mNewSortOrder;

            MoviesAdapter adapter = new MoviesAdapter(activity, moviesResponse.getMovies());
            activity.mRecyclerView.setAdapter(adapter);
        }

        @Override
        public void failure(RetrofitError error)
        {
            MainActivity activity = mWeakActivity.get();
            if (activity == null)
            {
                return;
            }

            String errorString;
            if (Device.isConnectedToInternet(activity))
            {
                try
                {
                    ApiError apiError = (ApiError) error.getBodyAs(ApiError.class);
                    errorString = apiError.getStatusMessage();
                }
                catch (Throwable t)
                {
                    errorString = error.getMessage();
                }

                if (DEBUG)
                {
                    Log.e(TAG, "Error fetching movies: " + error);
                }
            }
            else
            {
                errorString = activity.getString(R.string.error_no_internet);
            }

            if (activity.mRecyclerView.getAdapter() != null)
            {
                // already have content
                Toast.makeText(activity, errorString, Toast.LENGTH_LONG).show();
            }
            else
            {
                // no content yet
                activity.mErrorText.setText(errorString);
                activity.mErrorText.setVisibility(View.VISIBLE);
                activity.mRetryButton.setVisibility(View.VISIBLE);
            }

            activity.setUiInProgress(false);
        }
    }
}
