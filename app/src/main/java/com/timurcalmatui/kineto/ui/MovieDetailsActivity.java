package com.timurcalmatui.kineto.ui;

import android.os.Bundle;

import com.timurcalmatui.kineto.R;

/**
 * @author Timur Calmatui
 * @since 2015-08-20.
 */
public class MovieDetailsActivity extends BaseActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        
        if (savedInstanceState == null)
        {
            MovieDetailsFragment fragment = new MovieDetailsFragment();
            fragment.setArguments(getIntent().getExtras());
            
            getSupportFragmentManager().beginTransaction()
                                       .add(R.id.movie_detail_container, fragment)
                                       .commit();
        }
    }
}
