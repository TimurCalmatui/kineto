package com.timurcalmatui.kineto.model;

import java.util.ArrayList;

/**
 * @author Timur Calmatui
 * @since 2015-08-18.
 */
@SuppressWarnings("unused")
public class MoviesResponse
{
    private int page;
    private ArrayList<Movie> results;

    public ArrayList<Movie> getMovies()
    {
        return results;
    }

    @Override
    public String toString()
    {
        return "MoviesResponse{" +
                "page=" + page +
                ", results=" + results +
                '}';
    }
}
