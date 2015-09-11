package com.calmatui.timur.popularmovies.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.calmatui.timur.popularmovies.R;

/**
 * @author Timur Calmatui
 * @since 2015-09-08.
 */
public class Intents
{
    public static void startYouTubeVideo(Context context, String videoId)
    {
        try
        {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId));
            context.startActivity(intent);
        }
        catch (ActivityNotFoundException ex)
        {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://www.youtube.com/watch?v=" + videoId));
            context.startActivity(intent);
        }
    }
    
    public static void shareYoutubeVideo(Context context, String videoId)
    {
        try
        {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "http://www.youtube.com/watch?v=" + videoId);
            context.startActivity(shareIntent);
        }
        catch (ActivityNotFoundException ex)
        {
            Toast.makeText(context, R.string.error_share_youtube, Toast.LENGTH_SHORT).show();
        }
    }
}
