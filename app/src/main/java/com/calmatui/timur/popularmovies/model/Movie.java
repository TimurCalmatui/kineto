package com.calmatui.timur.popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * @author Timur Calmatui
 * @since 2015-08-18.
 */
public class Movie implements Parcelable
{
    private long id;
    private String originalTitle;
    private String overview;
    private Date releaseDate;
    private String posterPath;
    @SerializedName("vote_average")
    private float rating;

    public long getId()
    {
        return id;
    }

    public String getOriginalTitle()
    {
        return originalTitle;
    }

    public String getOverview()
    {
        return overview;
    }

    public Date getReleaseDate()
    {
        return releaseDate;
    }

    public String getPosterPath()
    {
        return posterPath;
    }

    public float getRating()
    {
        return rating;
    }

    @Override
    public String toString()
    {
        return "Movie{" +
                "id=" + id +
                ", originalTitle='" + originalTitle + '\'' +
                ", releaseDate='" + releaseDate + '\'' +
                ", posterPath='" + posterPath + '\'' +
                ", rating=" + rating +
                ", overview='" + overview + '\'' +
                '}';
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeLong(id);
        dest.writeString(originalTitle);
        dest.writeString(overview);
        dest.writeLong(releaseDate != null ? releaseDate.getTime() : 0);
        dest.writeString(posterPath);
        dest.writeFloat(rating);
    }

    protected Movie(Parcel in)
    {
        id = in.readLong();
        originalTitle = in.readString();
        overview = in.readString();
        long time = in.readLong();
        releaseDate = time != 0 ? new Date(time) : null;
        posterPath = in.readString();
        rating = in.readFloat();
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>()
    {
        @Override
        public Movie createFromParcel(Parcel in)
        {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size)
        {
            return new Movie[size];
        }
    };
}
