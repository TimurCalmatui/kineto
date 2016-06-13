package com.timurcalmatui.kineto.model;

/**
 * @author Timur Calmatui
 * @since 2015-08-21.
 */
@SuppressWarnings("unused")
public class ApiError
{
    private long statusCode;
    private String statusMessage;

    public long getStatusCode()
    {
        return statusCode;
    }

    public String getStatusMessage()
    {
        return statusMessage;
    }

    @Override
    public String toString()
    {
        return "ApiError{" +
                "statusCode=" + statusCode +
                ", statusMessage='" + statusMessage + '\'' +
                '}';
    }
}
