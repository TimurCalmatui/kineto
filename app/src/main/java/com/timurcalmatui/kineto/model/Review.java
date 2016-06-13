package com.timurcalmatui.kineto.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * @author Timur Calmatui
 * @since 2015-09-08.
 */
public class Review implements Parcelable
{
    private String author;
    private String content;
    
    public Review()
    {
    }
    
    public String getAuthor()
    {
        return author;
    }
    
    public String getContent()
    {
        return content;
    }
    
    public void setAuthor(String author)
    {
        this.author = author;
    }
    
    public void setContent(String content)
    {
        this.content = content;
    }
    
    public boolean isValid()
    {
        return !TextUtils.isEmpty(author) && !TextUtils.isEmpty(content);
    }
    
    @Override
    public String toString()
    {
        return "Review{" +
                "author='" + author + '\'' +
                ", content='" + content + '\'' +
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
        dest.writeString(this.author);
        dest.writeString(this.content);
    }
    
    protected Review(Parcel in)
    {
        this.author = in.readString();
        this.content = in.readString();
    }
    
    public static final Parcelable.Creator<Review> CREATOR = new Parcelable.Creator<Review>()
    {
        public Review createFromParcel(Parcel source)
        {
            return new Review(source);
        }
        
        public Review[] newArray(int size)
        {
            return new Review[size];
        }
    };
}
