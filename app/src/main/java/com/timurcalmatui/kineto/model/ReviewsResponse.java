package com.timurcalmatui.kineto.model;

import java.util.ArrayList;

/**
 * @author Timur Calmatui
 * @since 2015-09-08.
 */
public class ReviewsResponse
{
    ArrayList<Review> results;
    
    public ArrayList<Review> getReviews()
    {
        return results;
    }
    
    @Override
    public String toString()
    {
        return "ReviewsResponse{" +
                "results=" + results +
                '}';
    }
}
