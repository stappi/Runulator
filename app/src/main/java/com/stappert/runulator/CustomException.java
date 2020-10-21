package com.stappert.runulator;

/**
 * Custom exception for general errors in Runulator.
 */
public class CustomException extends Exception {
    /**
     * Title.
     */
    private String title;

    /**
     * Creates exception.
     *
     * @param title   title
     * @param message message
     */
    public CustomException(String title, String message) {
        super(message);
        this.title = title;
    }

    /**
     * Returns title.
     */
    public String getTitle() {
        return title;
    }
}
