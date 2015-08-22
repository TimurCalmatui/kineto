package com.calmatui.timur.popularmovies.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.calmatui.timur.popularmovies.R;
import com.calmatui.timur.popularmovies.api.Api;
import com.calmatui.timur.popularmovies.model.Movie;
import com.calmatui.timur.popularmovies.util.Compat;
import com.calmatui.timur.popularmovies.util.Themes;

import java.util.ArrayList;

/**
 * @author Timur Calmatui
 * @since 2015-08-19.
 */
public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.ViewHolder>
{
    private final Drawable mNoImageDrawable;
    private ArrayList<Movie> mMovies;
    private LayoutInflater mInflater;
    private Drawable mPlaceholder;

    public MoviesAdapter(Context context, ArrayList<Movie> movies)
    {
        mMovies = movies;
        mInflater = LayoutInflater.from(context);
        mPlaceholder = ContextCompat.getDrawable(context, R.drawable.poster_placeholder);
        mNoImageDrawable = Compat.getTintedDrawable(context, R.drawable.no_image_placeholder,
                Themes.getThemeAttrColor(context, android.R.attr.textColorSecondary));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = mInflater.inflate(R.layout.view_grid_item_movie, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        String posterPath = mMovies.get(position).getPosterPath();
        if (posterPath == null)
        {
            holder.mPoster.setImageDrawable(mNoImageDrawable);
            return;
        }

        // TODO: find out why there are visual glitches when no placeholder
        // TODO: optimize/configure caching
        Glide.with(holder.mPoster.getContext())
             .load(Api.getPosterUrl(mMovies.get(position).getPosterPath()))
             .crossFade()
             .placeholder(mPlaceholder)
             .error(mNoImageDrawable)
             .into(holder.mPoster);
    }

    @Override
    public int getItemCount()
    {
        return mMovies.size();
    }

    public ArrayList<Movie> getItems()
    {
        return mMovies;
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public ImageView mPoster;

        public ViewHolder(View v)
        {
            super(v);
            mPoster = (ImageView) v.findViewById(R.id.poster);

            v.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent intent = new Intent(v.getContext(), MovieDetailsActivity.class);
                    intent.putExtra(MovieDetailsActivity.ARG_MOVIE,
                            mMovies.get(getAdapterPosition()));
                    v.getContext().startActivity(intent);
                }
            });
        }
    }
}
