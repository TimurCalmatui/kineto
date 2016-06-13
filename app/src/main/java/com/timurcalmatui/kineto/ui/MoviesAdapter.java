package com.timurcalmatui.kineto.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.timurcalmatui.kineto.R;
import com.timurcalmatui.kineto.api.Api;
import com.timurcalmatui.kineto.model.Movie;
import com.timurcalmatui.kineto.util.Compat;
import com.timurcalmatui.kineto.util.Themes;

import java.util.ArrayList;

/**
 * @author Timur Calmatui
 * @since 2015-08-19.
 */
public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.ViewHolder>
{
    private RecyclerView mRecyclerView;
    private final Drawable mNoImageDrawable;
    private ArrayList<Movie> mMovies;
    private LayoutInflater mInflater;
    private final MainFragment.Listener mListener;
    private Drawable mPlaceholder;
    private boolean mSingleChoiceMode = false;
    private int mSelectedPosition = -1;
    private Drawable mSelectedBackground;
    
    public MoviesAdapter(Context context, ArrayList<Movie> movies, MainFragment.Listener listener,
                         boolean singleChoiceMode, int selectedPosition,
                         boolean notifyInitialSelectedPosition)
    {
        mMovies = movies;
        mInflater = LayoutInflater.from(context);
        mListener = listener;
        mSingleChoiceMode = singleChoiceMode;
        mSelectedPosition = selectedPosition;
        
        mPlaceholder = ContextCompat.getDrawable(context, R.drawable.poster_placeholder);
        mNoImageDrawable = Compat.getTintedDrawable(context, R.drawable.no_image_placeholder,
                Themes.getThemeAttrColor(context, android.R.attr.textColorSecondary));
        
        if (mSingleChoiceMode)
        {
            mSelectedBackground =
                    ContextCompat.getDrawable(context, R.drawable.selected_background);
            
            if (notifyInitialSelectedPosition && mSelectedPosition >= 0)
            {
                listener.onItemSelected(mMovies.get(mSelectedPosition));
            }
        }
        
        setHasStableIds(true);
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
        if (mSingleChoiceMode)
        {
            holder.mContainer.setBackground(
                    position == mSelectedPosition ? mSelectedBackground : null);
        }
        
        String posterPath = mMovies.get(position).getPosterPath();
        if (posterPath == null)
        {
            holder.mPoster.setImageDrawable(mNoImageDrawable);
            return;
        }
        
        // TODO: optimize/configure caching
        Glide.with(holder.mPoster.getContext())
             .load(Api.getPosterUrl(mMovies.get(position).getPosterPath()))
             .crossFade()
             .placeholder(mPlaceholder)
             .error(mNoImageDrawable)
             .into(holder.mPoster);
    }
    
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }
    
    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView)
    {
        super.onDetachedFromRecyclerView(recyclerView);
        mRecyclerView = null;
    }
    
    @Override
    public int getItemCount()
    {
        return mMovies.size();
    }
    
    @Override
    public long getItemId(int position)
    {
        Movie movie = mMovies.get(position);
        if (movie != null)
        {
            return movie.getId();
        }
        else
        {
            return super.getItemId(position);
        }
    }
    
    public ArrayList<Movie> getItems()
    {
        return mMovies;
    }
    
    public int getSelectedPosition()
    {
        return mSelectedPosition;
    }
    
    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public ImageView mPoster;
        public View mContainer;
        
        public ViewHolder(View v)
        {
            super(v);
            mPoster = (ImageView) v.findViewById(R.id.poster);
            mContainer = v.findViewById(R.id.poster_container);
            
            v.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    int newSelectedPosition = getAdapterPosition();
                    if (mSingleChoiceMode && mSelectedPosition != newSelectedPosition)
                    {
                        if (mSelectedPosition != -1 && mRecyclerView != null)
                        {
                            // notifyItemChanged() causes weird image reloads so we
                            // just find appropriate view holder and change it's background
                            // manually
                            ViewHolder vh = (ViewHolder) mRecyclerView
                                    .findViewHolderForAdapterPosition(mSelectedPosition);
                            
                            if (vh != null)
                            {
                                vh.mContainer.setBackground(null);
                            }
                        }
                        
                        mSelectedPosition = newSelectedPosition;
                        mContainer.setBackground(mSelectedBackground);
                    }
                    else if (mSingleChoiceMode)
                    {
                        // mSelectedPosition == newSelectedPosition => no need to notify selected
                        return;
                    }
                    
                    mListener.onItemSelected(mMovies.get(newSelectedPosition));
                }
            });
        }
    }
}
