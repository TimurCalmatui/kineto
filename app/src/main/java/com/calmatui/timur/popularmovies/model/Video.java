package com.calmatui.timur.popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Timur Calmatui
 * @since 2015-09-08.
 */
public class Video implements Parcelable
{
    public static final String SITE_YOUTUBE = "YouTube";
    
    private String site;
    private String key;
    
    public String getKey()
    {
        return key;
    }
    
    public boolean isValid()
    {
        return SITE_YOUTUBE.equals(site) && key != null && key.length() > 0;
    }
    
    public void setSite(String site)
    {
        this.site = site;
    }
    
    public void setKey(String key)
    {
        this.key = key;
    }
    
    @Override
    public String toString()
    {
        return "Video{" +
                "site='" + site + '\'' +
                ", key='" + key + '\'' +
                '}';
    }
    
    @Override
    public int describeContents()
    {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(this.site);
        dest.writeString(this.key);
    }
    
    public Video()
    {
    }
    
    protected Video(Parcel in)
    {
        this.site = in.readString();
        this.key = in.readString();
    }
    
    public static final Parcelable.Creator<Video> CREATOR = new Parcelable.Creator<Video>()
    {
        public Video createFromParcel(Parcel source)
        {
            return new Video(source);
        }
        
        public Video[] newArray(int size)
        {
            return new Video[size];
        }
    };
}
