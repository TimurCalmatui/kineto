package com.calmatui.timur.popularmovies.api;

import com.calmatui.timur.popularmovies.model.MoviesResponse;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * @author Timur Calmatui
 * @since 2015-08-18.
 */
public interface TheMovieDbService
{
    @GET("/discover/movie")
    void listMovies(@Query("sort_by") String sort, Callback<MoviesResponse> cb);
}