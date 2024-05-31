package com.example.android_imdb_project.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a review for a movie, including user details, content, and interactions (likes and dislikes).
 */
public class Review {
    private String reviewId;
    private String userId;
    private String userName;
    private String userProfilePicture;
    private String content;
    private long timestamp;
    private int likes;
    private int dislikes;
    private List<String> likedBy;
    private List<String> dislikedBy;

    /**
     * Default constructor for Review.
     */
    public Review() {
        // Initialize likes and dislikes lists to avoid null pointer exceptions
        this.likedBy = new ArrayList<>();
        this.dislikedBy = new ArrayList<>();
    }

    /**
     * Constructor for Review with all fields.
     *
     * @param reviewId           The ID of the review.
     * @param userId             The ID of the user who wrote the review.
     * @param userName           The name of the user who wrote the review.
     * @param userProfilePicture The profile picture URL of the user.
     * @param content            The content of the review.
     * @param timestamp          The timestamp of when the review was written.
     * @param movieId            The ID of the movie the review is associated with.
     */
    public Review(String reviewId, String userId, String userName, String userProfilePicture, String content, long timestamp, String movieId) {
        this.reviewId = reviewId;
        this.userId = userId;
        this.userName = userName;
        this.userProfilePicture = userProfilePicture;
        this.content = content;
        this.timestamp = timestamp;
        this.likes = 0; // Initialize likes to 0
        this.dislikes = 0; // Initialize dislikes to 0
        this.likedBy = new ArrayList<>();
        this.dislikedBy = new ArrayList<>();
    }

    // Getters and setters

    /**
     * Gets the ID of the review.
     *
     * @return The ID of the review.
     */
    public String getReviewId() {
        return reviewId;
    }

    /**
     * Sets the ID of the review.
     *
     * @param reviewId The ID to set.
     */
    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    /**
     * Gets the ID of the user who wrote the review.
     *
     * @return The ID of the user who wrote the review.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the ID of the user who wrote the review.
     *
     * @param userId The ID to set.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Gets the name of the user who wrote the review.
     *
     * @return The name of the user who wrote the review.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the name of the user who wrote the review.
     *
     * @param userName The name to set.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Gets the profile picture URL of the user who wrote the review.
     *
     * @return The profile picture URL of the user.
     */
    public String getUserProfilePicture() {
        return userProfilePicture;
    }

    /**
     * Sets the profile picture URL of the user who wrote the review.
     *
     * @param userProfilePicture The profile picture URL to set.
     */
    public void setUserProfilePicture(String userProfilePicture) {
        this.userProfilePicture = userProfilePicture;
    }

    /**
     * Gets the content of the review.
     *
     * @return The content of the review.
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the content of the review.
     *
     * @param content The content to set.
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Gets the timestamp of when the review was written.
     *
     * @return The timestamp of the review.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp of when the review was written.
     *
     * @param timestamp The timestamp to set.
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Gets the number of likes the review has received.
     *
     * @return The number of likes.
     */
    public int getLikes() {
        return likes;
    }

    /**
     * Sets the number of likes the review has received.
     *
     * @param likes The number of likes to set.
     */
    public void setLikes(int likes) {
        this.likes = likes;
    }

    /**
     * Gets the number of dislikes the review has received.
     *
     * @return The number of dislikes.
     */
    public int getDislikes() {
        return dislikes;
    }

    /**
     * Sets the number of dislikes the review has received.
     *
     * @param dislikes The number of dislikes to set.
     */
    public void setDislikes(int dislikes) {
        this.dislikes = dislikes;
    }

    /**
     * Gets the list of user IDs who liked the review.
     *
     * @return The list of user IDs who liked the review.
     */
    public List<String> getLikedBy() {
        return likedBy;
    }

    /**
     * Sets the list of user IDs who liked the review.
     *
     * @param likedBy The list of user IDs to set.
     */
    public void setLikedBy(List<String> likedBy) {
        this.likedBy = likedBy;
    }

    /**
     * Gets the list of user IDs who disliked the review.
     *
     * @return The list of user IDs who disliked the review.
     */
    public List<String> getDislikedBy() {
        return dislikedBy;
    }

    /**
     * Sets the list of user IDs who disliked the review.
     *
     * @param dislikedBy The list of user IDs to set.
     */
    public void setDislikedBy(List<String> dislikedBy) {
        this.dislikedBy = dislikedBy;
    }
}
