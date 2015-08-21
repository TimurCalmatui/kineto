package com.calmatui.timur.popularmovies.model;

import java.util.List;

/**
 * @author Timur Calmatui
 * @since 2015-08-18.
 */
@SuppressWarnings("unused")
public class MoviesResponse
{
    private int page;
    private List<Movie> results;

    public List<Movie> getMovies()
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
