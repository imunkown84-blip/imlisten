package com.musiccatalog.app.exception;

/** Thrown when the iTunes Search API call fails or times out. */
public class UpstreamApiException extends RuntimeException {
    public UpstreamApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
