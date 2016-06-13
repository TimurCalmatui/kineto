package com.timurcalmatui.kineto.ui;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.provider.BaseColumns;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.timurcalmatui.kineto.R;
import com.timurcalmatui.kineto.api.Api;
import com.timurcalmatui.kineto.data.MoviesDbHelper;
import com.timurcalmatui.kineto.model.Movie;
import com.timurcalmatui.kineto.util.Compat;
import com.timurcalmatui.kineto.util.Themes;

/**
 * @author Timur Calmatui
 * @since 2015-08-19.
 */
// TODO: refactor this to reuse code from MoviesAdapter
public class MoviesCursorAdapter extends RecyclerView.Adapter<MoviesCursorAdapter.ViewHolder>
{
    private RecyclerView mRecyclerView;
    private final Drawable mNoImageDrawable;
    private Cursor mCursor;
    private LayoutInflater mInflater;
    private final MainFragment.Listener mListener;
    private Drawable mPlaceholder;
    private boolean mSingleChoiceMode = false;
    private int mSelectedPosition = -1;
    private Drawable mSelectedBackground;
    
    public MoviesCursorAdapter(Context context, Cursor cursor,
                               MainFragment.Listener listener,
                               boolean singleChoiceMode, int selectedPosition,
                               boolean notifyInitialSelectedPosition)
    {
        mCursor = cursor;
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
            
            if (mCursor != null && notifyInitialSelectedPosition)
            {
                notifySelectedPosition(selectedPosition);
                mCursor.moveToFirst();
            }
        }
        
        setHasStableIds(true);
    }
    
    @Override
    public int getItemCount()
    {
        if (mCursor != null)
        {
            return mCursor.getCount();
        }
        
        return 0;
    }
    
    @Override
    public long getItemId(int position)
    {
        if (mCursor != null && mCursor.moveToPosition(position))
        {
            return mCursor.getLong(mCursor.getColumnIndex(BaseColumns._ID));
        }
        
        return 0;
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
        if (!mCursor.moveToPosition(position))
        {
            return;
        }
        
        if (mSingleChoiceMode)
        {
            holder.mContainer.setBackground(
                    position == mSelectedPosition ? mSelectedBackground : null);
        }
        
        Movie movie = MoviesDbHelper.toMovie(mCursor);
        
        String posterPath = movie.getPosterPath();
        if (posterPath == null)
        {
            holder.mPoster.setImageDrawable(mNoImageDrawable);
            return;
        }
        
        // TODO: optimize/configure caching
        Glide.with(holder.mPoster.getContext())
             .load(Api.getPosterUrl(posterPath))
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
    
    public Cursor swapCursor(Cursor newCursor)
    {
        if (mCursor == newCursor)
        {
            return null;
        }
        
        boolean needsNotification = false;
        if (mSingleChoiceMode)
        {
            int newSelectedPosition = mSelectedPosition;
            if (newCursor != null)
            {
                if (mSelectedPosition >= newCursor.getCount())
                {
                    newSelectedPosition = newCursor.getCount() - 1;
                }
                else if (mSelectedPosition == -1)
                {
                    newSelectedPosition = 0;
                }
            }
            else
            {
                newSelectedPosition = -1;
            }
            
            // this check should be made before cursor is updated
            long oldSelectedId = getItemId(mSelectedPosition);
            long newSelectedId = getItemId(newCursor, newSelectedPosition);
            if (oldSelectedId != newSelectedId)
            {
                needsNotification = true;
            }
    
            mSelectedPosition = newSelectedPosition;
        }
        
        Cursor oldCursor = mCursor;
        mCursor = newCursor;
        
        if (needsNotification)
        {
            // should be called after cursor is updated
            notifySelectedPosition(mSelectedPosition);
        }
    
        notifyDataSetChanged();
        
        return oldCursor;
    }
    
    private long getItemId(Cursor cursor, int position)
    {
        if (cursor != null && cursor.moveToPosition(position))
        {
            return cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
        }
        
        return -1;
    }
    
    private void notifySelectedPosition(int selectedPosition)
    {
        if (mCursor == null || !mCursor.moveToPosition(selectedPosition))
        {
            mListener.onItemSelected(null);
            return;
        }
        
        mListener.onItemSelected(MoviesDbHelper.toMovie(mCursor));
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
                    
                    notifySelectedPosition(newSelectedPosition);
                }
            });
        }
    }
}
