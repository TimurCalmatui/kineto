package com.calmatui.timur.popularmovies.ui;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.calmatui.timur.popularmovies.R;
import com.calmatui.timur.popularmovies.api.Api;
import com.calmatui.timur.popularmovies.data.MoviesContract;
import com.calmatui.timur.popularmovies.data.MoviesContract.MovieEntry;
import com.calmatui.timur.popularmovies.data.MoviesDbHelper;
import com.calmatui.timur.popularmovies.data.MoviesProvider;
import com.calmatui.timur.popularmovies.model.ApiError;
import com.calmatui.timur.popularmovies.model.Movie;
import com.calmatui.timur.popularmovies.model.Review;
import com.calmatui.timur.popularmovies.model.ReviewsResponse;
import com.calmatui.timur.popularmovies.model.Video;
import com.calmatui.timur.popularmovies.model.VideosResponse;
import com.calmatui.timur.popularmovies.util.Compat;
import com.calmatui.timur.popularmovies.util.Device;
import com.calmatui.timur.popularmovies.util.Intents;
import com.calmatui.timur.popularmovies.util.Metrics;
import com.calmatui.timur.popularmovies.util.Themes;
import com.calmatui.timur.popularmovies.util.WeakAsyncTask;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * @author Timur Calmatui
 * @since 2015-08-20.
 */
public class MovieDetailsFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>, WeakAsyncTask.WeakAsyncTaskCallbacks
{
    public static final String ARG_MOVIE = "ARG_MOVIE";
    
    private static final boolean DEBUG = false;
    private static final String TAG = "MovieDetails";
    
    private static final String STATE_TRAILERS = "STATE_TRAILERS";
    private static final String STATE_REVIEWS = "STATE_REVIEWS";
    private static final String STATE_MOVIE_FAVORITE = "STATE_MOVIE_FAVORITE";
    private static final int TRAILER_THUMBNAIL_SPACING = 8;
    
    private static final int FAVOURITE_TAG = 0;
    private static final int VIDEOS_TAG = 1;
    private static final int REVIEWS_TAG = 2;
    
    private Movie mMovie;
    private Boolean mMovieIsFavorite;
    
    private ImageView mFavoriteImageView;
    
    private ArrayList<Video> mTrailers = new ArrayList<>(0);
    private LinearLayout mTrailersContainer;
    private View mTrailersSection;
    private boolean mTrailersInitilized = false;
    
    private ArrayList<Review> mReviews = new ArrayList<>(0);
    private LinearLayout mReviewsContainer;
    private View mReviewsSection;
    private boolean mReviewsInitialized = false;
    
    private MenuItem mShareMenuItem;
    private Drawable mNoImageDrawable;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        
        Bundle args = getArguments();
        if (args != null)
        {
            mMovie = args.getParcelable(ARG_MOVIE);
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_movie_details, container, false);
        
        if (DEBUG)
        {
            Log.d(TAG, "onCreateView");
        }
        
        if (mMovie == null)
        {
            v.findViewById(R.id.main_section).setVisibility(View.GONE);
            return v;
        }
        
        TextView tv = (TextView) v.findViewById(R.id.title);
        tv.setText(mMovie.getOriginalTitle());
        
        tv = (TextView) v.findViewById(R.id.release_date);
        if (mMovie.getReleaseDate() != null)
        {
            tv.setText(DateFormat.getMediumDateFormat(getActivity())
                                 .format(mMovie.getReleaseDate()));
        }
        else
        {
            tv.setVisibility(View.GONE);
        }
        
        tv = (TextView) v.findViewById(R.id.rating);
        String rating = mMovie.getRating() == 10.0 ? "10" : String.valueOf(mMovie.getRating());
        tv.setText(rating);
        
        tv = (TextView) v.findViewById(R.id.synopsis);
        tv.setText(mMovie.getOverview());
        
        final ImageView poster = (ImageView) v.findViewById(R.id.poster);
        mNoImageDrawable = Compat.getTintedDrawable(getActivity(),
                R.drawable.no_image_placeholder,
                Themes.getThemeAttrColor(getActivity(), android.R.attr.textColorSecondary));
        
        if (mMovie.getPosterPath() == null)
        {
            poster.setImageDrawable(mNoImageDrawable);
            poster.setBackgroundColor(getResources().getColor(R.color.no_image_bg_color));
        }
        else
        {
            Glide.with(this)
                 .load(Api.getPosterUrl(mMovie.getPosterPath(), Api.POSTER_SIZE_342))
                 .error(mNoImageDrawable)
                 .crossFade()
                 .listener(new RequestListener<String, GlideDrawable>()
                 {
                     @Override
                     public boolean onException(Exception e, String model,
                                                Target<GlideDrawable> target,
                                                boolean isFirstResource)
                     {
                         poster.setBackgroundColor(
                                 getResources().getColor(R.color.no_image_bg_color));
                         return false;
                     }
                
                     @Override
                     public boolean onResourceReady(GlideDrawable resource, String model,
                                                    Target<GlideDrawable> target,
                                                    boolean isFromMemoryCache,
                                                    boolean isFirstResource)
                     {
                         return false;
                     }
                 })
                 .into(poster);
        }
        
        mFavoriteImageView = (ImageView) v.findViewById(R.id.favorite);
        mFavoriteImageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // haven't completed all requests
                if (mMovieIsFavorite == null
                        || !mTrailersInitilized
                        || !mReviewsInitialized)
                {
                    return;
                }
                
                saveFavoriteStatus(!mMovieIsFavorite);
            }
        });
        
        mTrailersContainer = (LinearLayout) v.findViewById(R.id.trailers_container);
        mTrailersSection = v.findViewById(R.id.trailers_section);
        
        mReviewsContainer = (LinearLayout) v.findViewById(R.id.reviews_container);
        mReviewsSection = v.findViewById(R.id.reviews_section);
        
        if (savedInstanceState != null)
        {
            mTrailers = savedInstanceState.getParcelableArrayList(STATE_TRAILERS);
            showTrailersIfAny();
            
            mReviews = savedInstanceState.getParcelableArrayList(STATE_REVIEWS);
            showReviewsIfAny();
            
            mMovieIsFavorite = savedInstanceState.getBoolean(STATE_MOVIE_FAVORITE);
            setFavoriteIcon(mMovieIsFavorite);
            
            if (DEBUG)
            {
                Log.d(TAG, "restored state");
            }
        }
        else
        {
            if (DEBUG)
            {
                Log.d(TAG, "checking favorite state");
            }
            
            getLoaderManager().initLoader(FAVOURITE_TAG, null, this);
        }
        
        return v;
    }
    
    private void setFavoriteIcon(boolean favorite)
    {
        int drawable = favorite
                ? R.drawable.ic_favorite_alpha_24dp
                : R.drawable.ic_favorite_border_alpha_24dp;
        
        mFavoriteImageView.setImageDrawable(
                Compat.getTintedDrawable(getActivity(), drawable,
                        Themes.getThemeAttrColor(getActivity(), R.attr.colorAccent)));
        
        mFavoriteImageView.setVisibility(View.VISIBLE);
    }
    
    private void saveFavoriteStatus(boolean favorite)
    {
        mMovieIsFavorite = favorite;
        setFavoriteIcon(mMovieIsFavorite);
        
        // TODO: show toast
        new WeakAsyncTask(FAVOURITE_TAG, this).execute();
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        outState.putParcelableArrayList(STATE_TRAILERS, mTrailers);
        outState.putParcelableArrayList(STATE_REVIEWS, mReviews);
        outState.putBoolean(STATE_MOVIE_FAVORITE, mMovieIsFavorite != null && mMovieIsFavorite);
        // TODO: remember scroll position
        super.onSaveInstanceState(outState);
    }
    
    private void showTrailersIfAny()
    {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        for (int i = 0; i < mTrailers.size(); i++)
        {
            final Video video = mTrailers.get(i);
            
            final View thumbnail = inflater.inflate(R.layout.view_trailer_thumbnail,
                    mTrailersContainer, false);
            ImageView iv = (ImageView) thumbnail.findViewById(R.id.image);
            
            int leftPadding = thumbnail.getPaddingLeft();
            if (i > 0)
            {
                leftPadding += Metrics.dpToPx(getActivity(), TRAILER_THUMBNAIL_SPACING);
            }
            
            thumbnail.setPadding(leftPadding, thumbnail.getPaddingTop(),
                    thumbnail.getPaddingRight(), thumbnail.getPaddingBottom());
            
            Glide.with(this)
                 .load(Api.getTrailerThumbnailUrl(video))
                 .crossFade()
                 .error(mNoImageDrawable)
                 .into(iv);
            
            mTrailersContainer.addView(thumbnail);
            
            thumbnail.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intents.startYouTubeVideo(getActivity(), video.getKey());
                }
            });
        }
        
        if (mTrailers.size() > 0)
        {
            mTrailersSection.setVisibility(View.VISIBLE);
        }
        
        mTrailersInitilized = true;
        getActivity().supportInvalidateOptionsMenu();
        
        // TODO: center trailers horizontally if mTrailersContainer width is less than screen width
    }
    
    private void showReviewsIfAny()
    {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        for (Review review : mReviews)
        {
            View view = inflater.inflate(R.layout.view_review, mReviewsContainer, false);
            TextView author = (TextView) view.findViewById(R.id.author);
            TextView content = (TextView) view.findViewById(R.id.content);
            
            author.setText(review.getAuthor());
            content.setText(review.getContent());
            
            mReviewsContainer.addView(view);
        }
        
        if (mReviews.size() > 0)
        {
            mReviewsSection.setVisibility(View.VISIBLE);
        }
        
        mReviewsInitialized = true;
    }
    
    private void processApiError(RetrofitError error)
    {
        String errorString;
        if (Device.isConnectedToInternet(getActivity()))
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
                Log.e(TAG, "API Error: " + error);
            }
        }
        else
        {
            errorString = getString(R.string.error_no_internet);
        }
        
        if (!mMovieIsFavorite)
        {
            Toast.makeText(getActivity().getApplicationContext(), errorString, Toast.LENGTH_SHORT)
                 .show();
        }
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.movie_details, menu);
        mShareMenuItem = menu.findItem(R.id.action_share);
    }
    
    @Override
    public void onPrepareOptionsMenu(Menu menu)
    {
        super.onPrepareOptionsMenu(menu);
        mShareMenuItem.setVisible(!mTrailersInitilized || mTrailersSection == null
                || mTrailersSection.getVisibility() == View.VISIBLE);
        
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.action_share)
        {
            Intents.shareYoutubeVideo(getActivity(), mTrailers.get(0).getKey());
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        if (id == FAVOURITE_TAG)
        {
            if (DEBUG)
            {
                Log.d(TAG, "onCreateLoader favorite");
            }
            
            return new CursorLoader(
                    getActivity(),
                    MovieEntry.buildMovieUri(mMovie.getId()),
                    new String[]{MovieEntry._ID},
                    null,
                    null,
                    null
            );
        }
        else if (id == VIDEOS_TAG)
        {
            if (DEBUG)
            {
                Log.d(TAG, "onCreateLoader videos");
            }
            
            return new CursorLoader(
                    getActivity(),
                    MovieEntry.buildMovieVideosUri(mMovie.getId()),
                    null,
                    null,
                    null,
                    null
            );
        }
        else if (id == REVIEWS_TAG)
        {
            if (DEBUG)
            {
                Log.d(TAG, "onCreateLoader reviews");
            }
            
            return new CursorLoader(
                    getActivity(),
                    MovieEntry.buildMovieReviewsUri(mMovie.getId()),
                    null,
                    null,
                    null,
                    null
            );
        }
        
        return null;
    }
    
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
    {
        int id = loader.getId();
        if (id == FAVOURITE_TAG)
        {
            if (DEBUG)
            {
                Log.d(TAG, "onLoadFinished favorite");
            }
            
            mMovieIsFavorite = data.moveToFirst();
            setFavoriteIcon(mMovieIsFavorite);
            
            if (DEBUG)
            {
                Log.d(TAG, "fetching videos and reviews from server");
            }
            Api.getInstance().listVideos(mMovie.getId(), new VideosResponseCallback(this));
            Api.getInstance().listReviews(mMovie.getId(), new ReviewsResponseCallback(this));
        }
        else if (id == VIDEOS_TAG)
        {
            if (DEBUG)
            {
                Log.d(TAG, "onLoadFinished videos");
            }
            
            mTrailers = new ArrayList<>(data.getCount());
            while (data.moveToNext())
            {
                mTrailers.add(MoviesDbHelper.toVideo(data));
            }
            
            showTrailersIfAny();
        }
        else if (id == REVIEWS_TAG)
        {
            if (DEBUG)
            {
                Log.d(TAG, "onLoadFinished reviews");
            }
            
            mReviews = new ArrayList<>(data.getCount());
            while (data.moveToNext())
            {
                mReviews.add(MoviesDbHelper.toReview(data));
            }
            
            showReviewsIfAny();
        }
        
        data.close(); // TODO: investigate if we need to close the cursor here
    }
    
    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
    }
    
    private void insertReviews(ContentResolver resolver)
    {
        if (mReviews.size() > 0)
        {
            ContentValues[] values = new ContentValues[mReviews.size()];
            for (int i = 0; i < values.length; i++)
            {
                values[i] = MoviesDbHelper.toContentValues(mReviews.get(i), mMovie.getId());
            }
            
            resolver.bulkInsert(MoviesContract.ReviewEntry.CONTENT_URI, values);
        }
    }
    
    private void insertVideos(ContentResolver resolver)
    {
        if (mTrailers.size() > 0)
        {
            ContentValues[] values = new ContentValues[mTrailers.size()];
            for (int i = 0; i < values.length; i++)
            {
                values[i] = MoviesDbHelper.toContentValues(mTrailers.get(i), mMovie.getId());
            }
            
            resolver.bulkInsert(MoviesContract.VideoEntry.CONTENT_URI, values);
        }
    }
    
    @Override
    public void onAsyncTaskDoInBackground(int id)
    {
        // TODO: also refresh movie details
        // TODO: investigate whether this could be done via a Loader instead of an AsyncTask
        
        Activity activity = getActivity();
        if (Compat.isActivityDestroyed(activity))
        {
            return;
        }
        
        ContentResolver resolver = activity.getContentResolver();
        if (id == FAVOURITE_TAG)
        {
            if (DEBUG)
            {
                Log.d(TAG, "onAsyncTaskDoInBackground saving favorite state");
            }
            
            if (mMovieIsFavorite)
            {
                resolver.insert(MovieEntry.CONTENT_URI, MoviesDbHelper.toContentValues(mMovie));
                insertVideos(resolver);
                insertReviews(resolver);
            }
            else
            {
                resolver.delete(MovieEntry.buildMovieUri(mMovie.getId()), null, null);
            }
        }
        else if (id == VIDEOS_TAG)
        {
            if (DEBUG)
            {
                Log.d(TAG, "onAsyncTaskDoInBackground refreshing videos");
            }
            
            resolver.delete(MoviesContract.VideoEntry.CONTENT_URI,
                    MoviesProvider.VIDEOS_BY_MOVIE_SELECTION,
                    new String[]{Long.toString(mMovie.getId())});
            
            insertVideos(resolver);
        }
        else if (id == REVIEWS_TAG)
        {
            if (DEBUG)
            {
                Log.d(TAG, "onAsyncTaskDoInBackground refreshing reviews");
            }
            
            resolver.delete(MoviesContract.ReviewEntry.CONTENT_URI,
                    MoviesProvider.REVIEWS_BY_MOVIE_SELECTION,
                    new String[]{Long.toString(mMovie.getId())});
            
            insertReviews(resolver);
        }
    }
    
    private static class VideosResponseCallback implements Callback<VideosResponse>
    {
        private WeakReference<MovieDetailsFragment> mWeakFragment;
        
        public VideosResponseCallback(MovieDetailsFragment fragment)
        {
            mWeakFragment = new WeakReference<>(fragment);
        }
        
        private MovieDetailsFragment getWeakFragment()
        {
            MovieDetailsFragment fragment = mWeakFragment.get();
            if (fragment != null && !Compat.isActivityDestroyed(fragment.getActivity()))
            {
                return fragment;
            }
            
            return null;
        }
        
        @Override
        public void success(VideosResponse videosResponse, Response response)
        {
            MovieDetailsFragment fragment = getWeakFragment();
            if (fragment != null)
            {
                if (DEBUG)
                {
                    Log.d(TAG, "videos fetched successfully");
                }
                
                fragment.mTrailers = new ArrayList<>(videosResponse.getVideos().size());
                for (Video video : videosResponse.getVideos())
                {
                    if (video.isValid())
                    {
                        fragment.mTrailers.add(video);
                    }
                }
                
                fragment.showTrailersIfAny();
                
                if (fragment.mMovieIsFavorite)
                {
                    // persist to db
                    new WeakAsyncTask(VIDEOS_TAG, fragment).execute();
                }
            }
        }
        
        @Override
        public void failure(RetrofitError error)
        {
            MovieDetailsFragment fragment = getWeakFragment();
            if (fragment != null)
            {
                if (DEBUG)
                {
                    Log.d(TAG, "videos fetching failed");
                }
                
                fragment.mTrailersInitilized = true;
                fragment.processApiError(error);
                
                if (fragment.mMovieIsFavorite)
                {
                    // load from db
                    fragment.getLoaderManager().restartLoader(VIDEOS_TAG, null, fragment);
                }
            }
        }
    }
    
    private static class ReviewsResponseCallback implements Callback<ReviewsResponse>
    {
        private WeakReference<MovieDetailsFragment> mWeakFragment;
        
        public ReviewsResponseCallback(MovieDetailsFragment fragment)
        {
            mWeakFragment = new WeakReference<>(fragment);
        }
        
        private MovieDetailsFragment getWeakFragment()
        {
            MovieDetailsFragment fragment = mWeakFragment.get();
            if (fragment != null && !Compat.isActivityDestroyed(fragment.getActivity()))
            {
                return fragment;
            }
            
            return null;
        }
        
        @Override
        public void success(ReviewsResponse reviewsResponse, Response response)
        {
            MovieDetailsFragment fragment = getWeakFragment();
            if (fragment != null)
            {
                if (DEBUG)
                {
                    Log.d(TAG, "reviews fetched successfully");
                }
                
                fragment.mReviews = new ArrayList<>(reviewsResponse.getReviews().size());
                for (Review review : reviewsResponse.getReviews())
                {
                    if (review.isValid())
                    {
                        fragment.mReviews.add(review);
                    }
                }
                
                fragment.showReviewsIfAny();
                
                if (fragment.mMovieIsFavorite)
                {
                    // persist to db
                    new WeakAsyncTask(REVIEWS_TAG, fragment).execute();
                }
            }
        }
        
        @Override
        public void failure(RetrofitError error)
        {
            MovieDetailsFragment fragment = getWeakFragment();
            if (fragment != null)
            {
                if (DEBUG)
                {
                    Log.d(TAG, "reviews fetching failed");
                }
                
                fragment.mReviewsInitialized = true;
                fragment.processApiError(error);
                
                if (fragment.mMovieIsFavorite)
                {
                    // load from db
                    fragment.getLoaderManager().restartLoader(REVIEWS_TAG, null, fragment);
                }
            }
        }
    }
}
