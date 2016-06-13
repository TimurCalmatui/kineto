package com.timurcalmatui.kineto.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.timurcalmatui.kineto.data.MoviesContract.MovieEntry;
import com.timurcalmatui.kineto.data.MoviesContract.ReviewEntry;
import com.timurcalmatui.kineto.data.MoviesContract.VideoEntry;
import com.timurcalmatui.kineto.model.Movie;
import com.timurcalmatui.kineto.model.Review;
import com.timurcalmatui.kineto.model.Video;

import java.util.Date;

/**
 * Manages a local database for movie data.
 * <p/>
 * This is based on Sunshine Version 2.
 *
 * @author Timur Calmatui
 * @since 2015-09-10.
 */
public class MoviesDbHelper extends SQLiteOpenHelper
{
    private static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "movies.db";
    
    public MoviesDbHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onConfigure(SQLiteDatabase db)
    {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }
    
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                MovieEntry._ID + " INTEGER PRIMARY KEY, " +
                MovieEntry.COLUMN_ORIGINAL_TITLE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_OVERVIEW + " TEXT, " +
                MovieEntry.COLUMN_RELEASE_DATE + " INTEGER," +
                MovieEntry.COLUMN_POSTER_PATH + " TEXT, " +
                MovieEntry.COLUMN_VOTE_AVERAGE + " REAL NOT NULL);";
        
        final String SQL_CREATE_VIDEOS_TABLE = "CREATE TABLE " + VideoEntry.TABLE_NAME + " (" +
                VideoEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                // the ID of the movie entry associated with this video data
                VideoEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                VideoEntry.COLUMN_KEY + " TEXT NOT NULL, " +
                
                // Set up the movie id column as a foreign key to movies table.
                " FOREIGN KEY (" + VideoEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                MovieEntry.TABLE_NAME + " (" + MovieEntry._ID + ") ON DELETE CASCADE);";
        
        final String SQL_CREATE_REVIEWS_TABLE = "CREATE TABLE " + ReviewEntry.TABLE_NAME + " (" +
                ReviewEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                // the ID of the movie entry associated with this video data
                ReviewEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                ReviewEntry.COLUMN_AUTHOR + " TEXT NOT NULL, " +
                ReviewEntry.COLUMN_CONTENT + " TEXT NOT NULL, " +
                
                // Set up the movie id column as a foreign key to movies table.
                " FOREIGN KEY (" + ReviewEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                MovieEntry.TABLE_NAME + " (" + MovieEntry._ID + ") ON DELETE CASCADE);";
        
        sqLiteDatabase.execSQL(SQL_CREATE_MOVIES_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_VIDEOS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_REVIEWS_TABLE);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion)
    {
        // do nothing
    }
    
    public static ContentValues toContentValues(Movie movie)
    {
        ContentValues values = new ContentValues();
        
        values.put(MovieEntry._ID, movie.getId());
        values.put(MovieEntry.COLUMN_ORIGINAL_TITLE, movie.getOriginalTitle());
        values.put(MovieEntry.COLUMN_OVERVIEW, movie.getOverview());
        values.put(MovieEntry.COLUMN_RELEASE_DATE,
                movie.getReleaseDate() != null ? movie.getReleaseDate().getTime() : null);
        values.put(MovieEntry.COLUMN_POSTER_PATH, movie.getPosterPath());
        values.put(MovieEntry.COLUMN_VOTE_AVERAGE, movie.getRating());
        
        return values;
    }
    
    public static ContentValues toContentValues(Video video, long movieId)
    {
        ContentValues values = new ContentValues();
        
        values.put(VideoEntry.COLUMN_MOVIE_ID, movieId);
        values.put(VideoEntry.COLUMN_KEY, video.getKey());
        
        return values;
    }
    
    public static ContentValues toContentValues(Review review, long movieId)
    {
        ContentValues values = new ContentValues();
        
        values.put(ReviewEntry.COLUMN_MOVIE_ID, movieId);
        values.put(ReviewEntry.COLUMN_AUTHOR, review.getAuthor());
        values.put(ReviewEntry.COLUMN_CONTENT, review.getContent());
        
        return values;
    }
    
    public static Movie toMovie(Cursor cursor)
    {
        Movie movie = new Movie();
        
        movie.setId(cursor.getLong(cursor.getColumnIndex(MovieEntry._ID)));
        
        movie.setOriginalTitle(
                cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_ORIGINAL_TITLE)));
        
        movie.setOverview(cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_OVERVIEW)));
        
        if (!cursor.isNull(cursor.getColumnIndex(MovieEntry.COLUMN_RELEASE_DATE)))
        {
            movie.setReleaseDate(new Date(cursor.getLong(
                    cursor.getColumnIndex(MovieEntry.COLUMN_RELEASE_DATE))));
        }
        
        movie.setRating(cursor.getFloat(cursor.getColumnIndex(MovieEntry.COLUMN_VOTE_AVERAGE)));
        
        movie.setPosterPath(cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_POSTER_PATH)));
        
        return movie;
    }
    
    public static Video toVideo(Cursor cursor)
    {
        Video video = new Video();
        
        video.setSite(Video.SITE_YOUTUBE);
        video.setKey(cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_KEY)));
        
        return video;
    }
    
    public static Review toReview(Cursor cursor)
    {
        Review review = new Review();
        
        review.setAuthor(cursor.getString(cursor.getColumnIndex(ReviewEntry.COLUMN_AUTHOR)));
        review.setContent(cursor.getString(cursor.getColumnIndex(ReviewEntry.COLUMN_CONTENT)));
        
        return review;
    }
}
