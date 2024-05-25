package com.example.android_imdb_project.models;

public class Review {
    private String reviewId;
    private String userId;
    private String userName;
    private String userProfilePicture;
    private String content;
    private long timestamp;
    private int likes;
    private int dislikes;

    public Review() {
        // Default constructor required for calls to DataSnapshot.getValue(Review.class)
    }

    public Review(String reviewId, String userId, String userName, String userProfilePicture, String content, long timestamp) {
        this.reviewId = reviewId;
        this.userId = userId;
        this.userName = userName;
        this.userProfilePicture = userProfilePicture;
        this.content = content;
        this.timestamp = timestamp;
        this.likes = 0;
        this.dislikes = 0;
    }

    // Getters and setters
    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserProfilePicture() {
        return userProfilePicture;
    }

    public void setUserProfilePicture(String userProfilePicture) {
        this.userProfilePicture = userProfilePicture;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getDislikes() {
        return dislikes;
    }

    public void setDislikes(int dislikes) {
        this.dislikes = dislikes;
    }
}
