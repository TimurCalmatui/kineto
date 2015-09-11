package com.calmatui.timur.popularmovies.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

/**
 * Provides access to movies data.
 * <p/>
 * Based on Sunshine Version 2.
 *
 * @author Timur Calmatui
 * @since 2015-09-10.
 */
public class MoviesProvider extends ContentProvider
{
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MoviesDbHelper mOpenHelper;
    
    private static final int MOVIES = 100;
    private static final int MOVIE = 101;
    private static final int MOVIE_VIDEOS = 102;
    private static final int MOVIE_REVIEWS = 103;
    private static final int VIDEOS = 200;
    private static final int REVIEWS = 300;
    
    public static final String MOVIE_BY_ID_SELECTION = MoviesContract.MovieEntry.TABLE_NAME
            + "." + MoviesContract.MovieEntry._ID + " = ? ";
    
    public static final String VIDEOS_BY_MOVIE_SELECTION = MoviesContract.VideoEntry.TABLE_NAME
            + "." + MoviesContract.VideoEntry.COLUMN_MOVIE_ID + " = ? ";
    
    public static final String REVIEWS_BY_MOVIE_SELECTION = MoviesContract.ReviewEntry.TABLE_NAME
            + "." + MoviesContract.ReviewEntry.COLUMN_MOVIE_ID + " = ? ";
    
    static UriMatcher buildUriMatcher()
    {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MoviesContract.CONTENT_AUTHORITY;
        
        matcher.addURI(authority, MoviesContract.PATH_MOVIES, MOVIES);
        matcher.addURI(authority, MoviesContract.PATH_MOVIES + "/#", MOVIE);
        matcher.addURI(authority, MoviesContract.PATH_MOVIES + "/#/" + MoviesContract.PATH_VIDEOS,
                MOVIE_VIDEOS);
        matcher.addURI(authority, MoviesContract.PATH_MOVIES + "/#/" + MoviesContract.PATH_REVIEWS,
                MOVIE_REVIEWS);
        matcher.addURI(authority, MoviesContract.PATH_VIDEOS, VIDEOS);
        matcher.addURI(authority, MoviesContract.PATH_REVIEWS, REVIEWS);
        
        return matcher;
    }
    
    @Override
    public boolean onCreate()
    {
        mOpenHelper = new MoviesDbHelper(getContext());
        return true;
    }
    
    @Override
    public String getType(Uri uri)
    {
        switch (sUriMatcher.match(uri))
        {
            case MOVIES:
                return MoviesContract.MovieEntry.CONTENT_TYPE;
            
            case MOVIE:
                return MoviesContract.MovieEntry.CONTENT_ITEM_TYPE;
            
            case MOVIE_VIDEOS:
            case VIDEOS:
                return MoviesContract.VideoEntry.CONTENT_TYPE;
            
            case MOVIE_REVIEWS:
            case REVIEWS:
                return MoviesContract.ReviewEntry.CONTENT_TYPE;
            
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
    }
    
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder)
    {
        Cursor retCursor;
        switch (sUriMatcher.match(uri))
        {
            // "movies"
            case MOVIES:
            {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            
            // "movies/#"
            case MOVIE:
            {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.MovieEntry.TABLE_NAME,
                        projection,
                        MOVIE_BY_ID_SELECTION,
                        new String[]{Long.toString(MoviesContract.parseId(uri))},
                        null,
                        null,
                        null
                );
                break;
            }
            
            // "movies/#/videos"
            case MOVIE_VIDEOS:
            {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.VideoEntry.TABLE_NAME,
                        projection,
                        VIDEOS_BY_MOVIE_SELECTION,
                        new String[]{Long.toString(MoviesContract.parseId(uri))},
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            
            // "movies/#/reviews"
            case MOVIE_REVIEWS:
            {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.ReviewEntry.TABLE_NAME,
                        projection,
                        REVIEWS_BY_MOVIE_SELECTION,
                        new String[]{Long.toString(MoviesContract.parseId(uri))},
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
        
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        
        return retCursor;
    }
    
    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;
        
        switch (match)
        {
            case MOVIES:
            {
                long _id = db.insert(MoviesContract.MovieEntry.TABLE_NAME, null, values);
                if (_id > 0)
                {
                    returnUri = MoviesContract.MovieEntry.buildMovieUri(_id);
                }
                else
                {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                
                break;
            }
            
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
        
        getContext().getContentResolver().notifyChange(uri, null);
        
        return returnUri;
    }
    
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        
        // this makes delete all rows return the number of rows deleted
        if (null == selection)
        {
            selection = "1";
        }
        
        switch (match)
        {
            // "movies"
            case MOVIES:
                rowsDeleted = db.delete(
                        MoviesContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            
            // "movies/#"
            case MOVIE:
            {
                rowsDeleted = db.delete(
                        MoviesContract.MovieEntry.TABLE_NAME,
                        MOVIE_BY_ID_SELECTION,
                        new String[]{Long.toString(MoviesContract.parseId(uri))});
                break;
            }
            
            // "movies/#/videos"
            case MOVIE_VIDEOS:
            {
                rowsDeleted = db.delete(
                        MoviesContract.VideoEntry.TABLE_NAME,
                        VIDEOS_BY_MOVIE_SELECTION,
                        new String[]{Long.toString(MoviesContract.parseId(uri))}
                );
                break;
            }
            
            // "movies/#/reviews"
            case MOVIE_REVIEWS:
            {
                rowsDeleted = db.delete(
                        MoviesContract.ReviewEntry.TABLE_NAME,
                        REVIEWS_BY_MOVIE_SELECTION,
                        new String[]{Long.toString(MoviesContract.parseId(uri))}
                );
                break;
            }

            // "videos"
            case VIDEOS:
                rowsDeleted =
                        db.delete(MoviesContract.VideoEntry.TABLE_NAME, selection, selectionArgs);
                break;
    
            // "reviews"
            case REVIEWS:
                rowsDeleted =
                        db.delete(MoviesContract.ReviewEntry.TABLE_NAME, selection, selectionArgs);
                break;
            
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
        
        // Because a null deletes all rows
        if (rowsDeleted != 0)
        {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        
        return rowsDeleted;
    }
    
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;
        
        switch (match)
        {
            case MOVIES:
                rowsUpdated = db.update(MoviesContract.MovieEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
        
        if (rowsUpdated != 0)
        {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        
        return rowsUpdated;
    }
    
    @Override
    public int bulkInsert(Uri uri, @NonNull ContentValues[] values)
    {
        String tableName;
        switch (sUriMatcher.match(uri))
        {
            case MOVIES:
                tableName = MoviesContract.MovieEntry.TABLE_NAME;
                break;
            
            case VIDEOS:
                tableName = MoviesContract.VideoEntry.TABLE_NAME;
                break;
            
            case REVIEWS:
                tableName = MoviesContract.ReviewEntry.TABLE_NAME;
                break;
            
            default:
                return super.bulkInsert(uri, values);
        }
        
        int returnCount = 0;
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try
        {
            for (ContentValues value : values)
            {
                long _id = db.insert(tableName, null, value);
                if (_id != -1)
                {
                    returnCount++;
                }
            }
            db.setTransactionSuccessful();
        }
        finally
        {
            db.endTransaction();
        }
        
        getContext().getContentResolver().notifyChange(uri, null);
        
        return returnCount;
    }
    
    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown()
    {
        mOpenHelper.close();
        super.shutdown();
    }
}
