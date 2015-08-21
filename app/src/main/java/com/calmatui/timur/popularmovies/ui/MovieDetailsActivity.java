package com.calmatui.timur.popularmovies.ui;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.calmatui.timur.popularmovies.R;
import com.calmatui.timur.popularmovies.api.Api;
import com.calmatui.timur.popularmovies.model.Movie;
import com.calmatui.timur.popularmovies.util.Compat;
import com.calmatui.timur.popularmovies.util.Themes;

/**
 * @author Timur Calmatui
 * @since 2015-08-20.
 */
public class MovieDetailsActivity extends BaseActivity
{
    public static final String ARG_MOVIE = "ARG_MOVIE";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        Movie movie = null;
        Bundle args = getIntent().getExtras();
        if (args != null)
        {
            movie = args.getParcelable(ARG_MOVIE);
        }

        if (movie == null)
        {
            return;
        }

        TextView tv = (TextView) findViewById(R.id.title);
        tv.setText(movie.getOriginalTitle());

        tv = (TextView) findViewById(R.id.release_date);
        if (movie.getReleaseDate() != null)
        {
            tv.setText(DateFormat.getMediumDateFormat(this).format(movie.getReleaseDate()));
        }
        else
        {
            tv.setVisibility(View.GONE);
        }

        tv = (TextView) findViewById(R.id.rating);
        String rating = movie.getRating() == 10.0 ? "10" : String.valueOf(movie.getRating());
        tv.setText(rating);

        tv = (TextView) findViewById(R.id.synopsis);
        tv.setText(movie.getOverview());

        final ImageView poster = (ImageView) findViewById(R.id.poster);
        Drawable noImageDrawable = Compat.getTintedDrawable(this, R.drawable.no_image_placeholder,
                Themes.getThemeAttrColor(this, android.R.attr.textColorSecondary));

        if (movie.getPosterPath() == null)
        {
            poster.setImageDrawable(noImageDrawable);
            poster.setBackgroundColor(getResources().getColor(R.color.no_image_bg_color));
            return;
        }

        Glide.with(this)
             .load(Api.getPosterUrl(movie.getPosterPath(), Api.POSTER_SIZE_342))
             .error(noImageDrawable)
             .crossFade()
             .listener(new RequestListener<String, GlideDrawable>()
             {
                 @Override
                 public boolean onException(Exception e, String model,
                                            Target<GlideDrawable> target,
                                            boolean isFirstResource)
                 {
                     poster.setBackgroundColor(getResources().getColor(R.color.no_image_bg_color));
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
}
