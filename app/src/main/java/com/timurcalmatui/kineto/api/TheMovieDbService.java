package com.timurcalmatui.kineto.api;

import com.timurcalmatui.kineto.model.MoviesResponse;
import com.timurcalmatui.kineto.model.ReviewsResponse;
import com.timurcalmatui.kineto.model.VideosResponse;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * @author Timur Calmatui
 * @since 2015-08-18.
 */
public interface TheMovieDbService
{
    @GET("/discover/movie")
    void listMovies(@Query("sort_by") String sort, Callback<MoviesResponse> cb);
    
    @GET("/movie/{movieId}/videos")
    void listVideos(@Path("movieId") long movieId, Callback<VideosResponse> cb);
    
    @GET("/movie/{movieId}/reviews")
    void listReviews(@Path("movieId") long movieId, Callback<ReviewsResponse> cb);
}
