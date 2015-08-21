package com.calmatui.timur.popularmovies.api;

import com.calmatui.timur.popularmovies.Config;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

/**
 * @author Timur Calmatui
 * @since 2015-08-18.
 */
public class Api
{
    public static final String SORT_POPULARITY_DESC = "popularity.desc";
    public static final String SORT_RATING_DESC = "vote_average.desc";

    public static final String POSTER_SIZE_342 = "w342";
    @SuppressWarnings("unused")
    public static final String POSTER_SIZE_185 = "w185";

    // TODO: fetch this periodically via /configuration endpoint
    private static final String IMAGES_BASE_URL = "https://image.tmdb.org/t/p/";

    private static final boolean DEBUG = false;
    private static TheMovieDbService sInstance;
    
    public static TheMovieDbService getInstance()
    {
        if (sInstance == null)
        {
            Gson gson = new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .setDateFormat("yyyy-MM-dd")
                    .create();

            RequestInterceptor requestInterceptor = new RequestInterceptor()
            {
                @Override
                public void intercept(RequestFacade request)
                {
                    request.addQueryParam("api_key", Config.MOVIE_DB_API_KEY);
                }
            };

            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setEndpoint(Config.MOVIE_DB_API_ENDPOINT)
                    .setConverter(new GsonConverter(gson))
                    .setRequestInterceptor(requestInterceptor)
                    .build();

            if (DEBUG)
            {
                restAdapter.setLogLevel(RestAdapter.LogLevel.FULL);
            }

            sInstance = restAdapter.create(TheMovieDbService.class);
        }

        return sInstance;
    }

    public static String getPosterUrl(String posterPath)
    {
        return getPosterUrl(posterPath, POSTER_SIZE_342);
    }

    public static String getPosterUrl(String posterPath, String posterSize)
    {
        return IMAGES_BASE_URL + posterSize + posterPath;
    }
}
