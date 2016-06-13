package com.timurcalmatui.kineto.model;

import java.util.ArrayList;

/**
 * @author Timur Calmatui
 * @since 2015-09-08.
 */
public class VideosResponse
{
    ArrayList<Video> results;
    
    public ArrayList<Video> getVideos()
    {
        return results;
    }
    
    @Override
    public String toString()
    {
        return "VideosResponse{" +
                "results=" + results +
                '}';
    }
}
