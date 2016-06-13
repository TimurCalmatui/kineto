package com.timurcalmatui.kineto.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.timurcalmatui.kineto.R;
import com.timurcalmatui.kineto.api.Api;
import com.timurcalmatui.kineto.data.MoviesContract;
import com.timurcalmatui.kineto.model.ApiError;
import com.timurcalmatui.kineto.model.Movie;
import com.timurcalmatui.kineto.model.MoviesResponse;
import com.timurcalmatui.kineto.util.Compat;
import com.timurcalmatui.kineto.util.Device;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * @author Timur Calmatui
 * @since 2015-08-18.
 */
public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{
    public static final String ARG_TWO_PANE_MODE = "ARG_TWO_PANE_MODE";
    
    private static final String TAG = MainFragment.class.getSimpleName();
    private static final boolean DEBUG = false;
    
    private static final String STATE_SORT_ORDER = "STATE_SORT_ORDER";
    private static final String STATE_MOVIES = "STATE_MOVIES";
    private static final String STATE_SELECTED_POSITION = "STATE_SELECTED_POSITION";
    
    private static final String PREFERENCE_KEY =
            "com.timurcalmatui.kineto.MOVIES_LIST_PREFERENCES";
    
    private static final String SORT_FAVORITES = "SORT_FAVORITES";
    
    private static final int FAVORITES_LOADER = 1;
    private static final int FAVORITES_LOADER_RESTORED = 2;
    
    private RecyclerView mRecyclerView;
    private String mSortOrder = Api.SORT_POPULARITY_DESC;
    private String mNewSortOrder = mSortOrder;
    
    private TextView mNoFavoritesText;
    private TextView mErrorText;
    private Button mRetryButton;
    private ContentLoadingProgressBar mProgressBar;
    
    private boolean mInProgress;
    private boolean mTwoPaneMode;
    
    private int mSelectedPosition = 0;
    private MoviesCursorAdapter mMoviesCursorAdapter;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setHasOptionsMenu(true);
        
        Bundle args = getArguments();
        if (args != null)
        {
            mTwoPaneMode = args.getBoolean(ARG_TWO_PANE_MODE);
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        
        mNoFavoritesText = (TextView) v.findViewById(R.id.empty);
        mErrorText = (TextView) v.findViewById(R.id.error);
        mRetryButton = (Button) v.findViewById(R.id.retry);
        mProgressBar = (ContentLoadingProgressBar) v.findViewById(R.id.progress);
        
        mRecyclerView = (RecyclerView) v.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(),
                getColumnNumber(), GridLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        
        mRetryButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                refresh();
            }
        });
        
        mNewSortOrder = mSortOrder =
                getActivity().getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE)
                             .getString(STATE_SORT_ORDER, mSortOrder);
        
        ArrayList<Movie> movies = null;
        boolean restoredState = false;
        if (savedInstanceState != null)
        {
            movies = savedInstanceState.getParcelableArrayList(STATE_MOVIES);
            mSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            restoredState = true;
        }
        
        if (movies != null)
        {
            mRecyclerView.setAdapter(new MoviesAdapter(getActivity(), movies,
                    (Listener) getActivity(), mTwoPaneMode, mSelectedPosition, false));
        }
        else
        {
            final boolean finalRestoredState = restoredState;
            mProgressBar.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener()
                    {
                        @Override
                        public void onGlobalLayout()
                        {
                            refresh(finalRestoredState);
                            mProgressBar.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    });
        }
        
        return v;
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        if (mRecyclerView.getAdapter() != null)
        {
            if (mRecyclerView.getAdapter() instanceof MoviesAdapter)
            {
                MoviesAdapter adapter = (MoviesAdapter) mRecyclerView.getAdapter();
                outState.putParcelableArrayList(STATE_MOVIES, adapter.getItems());
                outState.putInt(STATE_SELECTED_POSITION, adapter.getSelectedPosition());
            }
            else if (mRecyclerView.getAdapter() instanceof MoviesCursorAdapter)
            {
                // don't save items (the loader will reload data)
                MoviesCursorAdapter adapter = (MoviesCursorAdapter) mRecyclerView.getAdapter();
                outState.putInt(STATE_SELECTED_POSITION, adapter.getSelectedPosition());
            }
        }
        
        super.onSaveInstanceState(outState);
    }
    
    @Override
    public void onPause()
    {
        super.onPause();
        
        getActivity().getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE)
                     .edit()
                     .putString(STATE_SORT_ORDER, mSortOrder)
                     .apply();
    }
    
    private int getColumnNumber()
    {
        float thumbSize = mTwoPaneMode ? 300.0f : 200.0f;
        return Math.max(2, Math.round(Device.getScreenWidthDp(getActivity()) / thumbSize));
    }
    
    private void refresh()
    {
        refresh(false);
    }
    
    private void refresh(boolean restoredState)
    {
        refresh(mSortOrder, restoredState);
    }
    
    private void refresh(String sortOrder, boolean restoredState)
    {
        if (mInProgress)
        {
            return;
        }
        
        setUiInProgress(true);
        
        if (sortOrder.equals(SORT_FAVORITES))
        {
            getLoaderManager().restartLoader(
                    restoredState ? FAVORITES_LOADER_RESTORED : FAVORITES_LOADER, null, this);
        }
        else
        {
            Api.getInstance().listMovies(sortOrder, new MoviesResponseCallback(this));
        }
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.main, menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.sort)
        {
            int checkedItem = 0;
            if (mSortOrder.equals(Api.SORT_RATING_DESC))
            {
                checkedItem = 1;
            }
            else if (mSortOrder.equals(SORT_FAVORITES))
            {
                checkedItem = 2;
            }
            
            // TODO: find out why accentColor doesn't tint radio buttons (AppCompat should have this)
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.sort_by)
                    .setSingleChoiceItems(R.array.movie_sort_options,
                            checkedItem,
                            new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    mNewSortOrder = Api.SORT_POPULARITY_DESC;
                                    
                                    if (which == 1)
                                    {
                                        mNewSortOrder = Api.SORT_RATING_DESC;
                                    }
                                    else if (which == 2)
                                    {
                                        mNewSortOrder = SORT_FAVORITES;
                                    }
                                    
                                    if (!mNewSortOrder.equals(mSortOrder))
                                    {
                                        // force fresh state
                                        mSelectedPosition = 0;
                                        mMoviesCursorAdapter = null;
                                        refresh(mNewSortOrder, false);
                                    }
                                    
                                    dialog.dismiss();
                                }
                            })
                    .show();
            
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        return new CursorLoader(getActivity(),
                MoviesContract.MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null); // TODO: sort by some meaningful criteria
    }
    
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
    {
        if (!mNewSortOrder.equals(SORT_FAVORITES))
        {
            return;
        }
        
        if (DEBUG)
        {
            Log.d(TAG, "onLoadFinished");
        }
        
        setUiInProgress(false);
        mSortOrder = mNewSortOrder;
        
        if (mMoviesCursorAdapter == null)
        {
            mMoviesCursorAdapter = new MoviesCursorAdapter(getActivity(),
                    data, (Listener) getActivity(), mTwoPaneMode, mSelectedPosition,
                    loader.getId() == FAVORITES_LOADER);
        }
        else
        {
            mMoviesCursorAdapter.swapCursor(data);
        }
        
        if (mRecyclerView.getAdapter() != mMoviesCursorAdapter)
        {
            mRecyclerView.setAdapter(mMoviesCursorAdapter);
        }
        
        mNoFavoritesText.setVisibility(data == null || data.getCount() == 0
                ? View.VISIBLE : View.GONE);
    }
    
    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        onLoadFinished(loader, null);
    }
    
    public interface Listener
    {
        void onItemSelected(Movie movie);
    }
    
    private static class MoviesResponseCallback implements Callback<MoviesResponse>
    {
        private final WeakReference<MainFragment> mWeakFragment;
        
        public MoviesResponseCallback(MainFragment fragment)
        {
            mWeakFragment = new WeakReference<>(fragment);
        }
        
        @Override
        public void success(MoviesResponse moviesResponse, Response response)
        {
            MainFragment fragment = mWeakFragment.get();
            if (fragment == null || Compat.isActivityDestroyed(fragment.getActivity()))
            {
                return;
            }
            
            fragment.setUiInProgress(false);
            fragment.mSortOrder = fragment.mNewSortOrder;
            fragment.mNoFavoritesText.setVisibility(View.GONE);
            
            MoviesAdapter adapter = new MoviesAdapter(fragment.getActivity(),
                    moviesResponse.getMovies(), (Listener) fragment.getActivity(),
                    fragment.mTwoPaneMode, fragment.mSelectedPosition, true);
            fragment.mRecyclerView.setAdapter(adapter);
        }
        
        @Override
        public void failure(RetrofitError error)
        {
            MainFragment fragment = mWeakFragment.get();
            if (fragment == null || Compat.isActivityDestroyed(fragment.getActivity()))
            {
                return;
            }
            
            Activity activity = fragment.getActivity();
            
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
                errorString = fragment.getString(R.string.error_no_internet);
            }
            
            if (fragment.mRecyclerView.getAdapter() != null)
            {
                // already have content
                Toast.makeText(activity.getApplicationContext(), errorString, Toast.LENGTH_SHORT)
                     .show();
            }
            else
            {
                // no content yet
                fragment.mErrorText.setText(errorString);
                fragment.mErrorText.setVisibility(View.VISIBLE);
                fragment.mRetryButton.setVisibility(View.VISIBLE);
            }
            
            fragment.setUiInProgress(false);
        }
    }
}
