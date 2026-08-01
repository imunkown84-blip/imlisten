package com.musiccatalog.app.exception;

/** Thrown when the iTunes Search API call fails or times out. */
public class UpstreamApiException extends RuntimeException {

    private final boolean rateLimited;

    public UpstreamApiException(String message, Throwable cause) {
        this(message, cause, false);
    }

    public UpstreamApiException(String message, Throwable cause, boolean rateLimited) {
        super(message, cause);
        this.rateLimited = rateLimited;
    }

    public boolean isRateLimited() {
        return rateLimited;
    }
}
