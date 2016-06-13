package com.timurcalmatui.kineto.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;

import com.timurcalmatui.kineto.R;
import com.timurcalmatui.kineto.model.Movie;
import com.timurcalmatui.kineto.util.Compat;

/**
 * @author Timur Calmatui
 * @since 2015-08-18.
 */
public class MainActivity extends BaseActivity implements MainFragment.Listener
{
    private boolean mTwoPane;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_main);
        
        if (findViewById(R.id.movie_detail_container) != null)
        {
            mTwoPane = true;
            if (savedInstanceState == null)
            {
                MainFragment mainFragment = new MainFragment();
                Bundle args = new Bundle();
                args.putBoolean(MainFragment.ARG_TWO_PANE_MODE, true);
                mainFragment.setArguments(args);
                
                getSupportFragmentManager().beginTransaction()
                                           .replace(R.id.main_container, mainFragment, null)
                                           .replace(R.id.movie_detail_container,
                                                   new MovieDetailsFragment(), null)
                                           .commit();
            }
        }
        else
        {
            mTwoPane = false;
        }
    }
    
    @Override
    public void onItemSelected(final Movie movie)
    {
        if (mTwoPane)
        {
            new Handler().postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    if (Compat.isActivityDestroyed(MainActivity.this))
                    {
                        return;
                    }
                    
                    if (movie != null)
                    {
                        Bundle args = new Bundle();
                        args.putParcelable(MovieDetailsFragment.ARG_MOVIE, movie);
                        
                        MovieDetailsFragment fragment = new MovieDetailsFragment();
                        fragment.setArguments(args);
                        
                        getSupportFragmentManager().beginTransaction()
                                                   .replace(R.id.movie_detail_container, fragment)
                                                   .commit();
                    }
                    else
                    {
                        Fragment fragment = getSupportFragmentManager().findFragmentById(
                                R.id.movie_detail_container);
                        
                        getSupportFragmentManager().beginTransaction()
                                                   .remove(fragment)
                                                   .commit();
                    }
                }
            }, 10);
        }
        else
        {
            Intent intent = new Intent(this, MovieDetailsActivity.class);
            intent.putExtra(MovieDetailsFragment.ARG_MOVIE, movie);
            startActivity(intent);
        }
    }
}
