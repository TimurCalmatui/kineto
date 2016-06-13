package com.timurcalmatui.kineto.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines table and column names for the movie database. Also helps with content URIs.
 * <p/>
 * Based on Sunshine Version 2.
 *
 * @author Timur Calmatui
 * @since 2015-09-10.
 */
public class MoviesContract
{
    public static final String CONTENT_AUTHORITY = "com.timurcalmatui.kineto.app";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_MOVIES = "movies";
    public static final String PATH_VIDEOS = "videos";
    public static final String PATH_REVIEWS = "reviews";
    
    /**
     * Converts the second path segment to a long.
     * <p/>
     * This supports a common convention for RESTful URIs where an ID is
     * stored in the second path segment after resource name.
     *
     * @return the long conversion of the second segment or -1 if the path is
     * empty
     * @throws UnsupportedOperationException if this isn't a hierarchical URI
     * @throws NumberFormatException         if the second segment isn't a number
     */
    public static long parseId(Uri contentUri)
    {
        String second = contentUri.getPathSegments().get(1);
        return second == null ? -1 : Long.parseLong(second);
    }
    
    public static final class MovieEntry implements BaseColumns
    {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();
        
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/movie";
        
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/movie";
        
        public static final String TABLE_NAME = "movies";
        public static final String COLUMN_ORIGINAL_TITLE = "original_title";
        public static final String COLUMN_OVERVIEW = "overview";
        /**
         * Date, stored as long in milliseconds since the epoch
         */
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        
        public static Uri buildMovieUri(long id)
        {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
        
        public static Uri buildMovieVideosUri(long id)
        {
            return CONTENT_URI.buildUpon()
                              .appendEncodedPath(String.valueOf(id))
                              .appendPath(PATH_VIDEOS)
                              .build();
        }
        
        public static Uri buildMovieReviewsUri(long id)
        {
            return CONTENT_URI.buildUpon()
                              .appendEncodedPath(String.valueOf(id))
                              .appendPath(PATH_REVIEWS)
                              .build();
        }
    }
    
    public static final class VideoEntry implements BaseColumns
    {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_VIDEOS).build();
        
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/video";
        
        public static final String TABLE_NAME = "videos";
        public static final String COLUMN_MOVIE_ID = "movie_id"; // FK
        public static final String COLUMN_KEY = "key";
    }
    
    public static final class ReviewEntry implements BaseColumns
    {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEWS).build();
        
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/review";
        
        public static final String TABLE_NAME = "reviews";
        public static final String COLUMN_MOVIE_ID = "movie_id"; // FK
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_CONTENT = "content";
    }
}
