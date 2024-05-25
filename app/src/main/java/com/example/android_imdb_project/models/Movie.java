package com.example.android_imdb_project.models;

import java.util.HashMap;
import java.util.Map;

public class Movie {
    private String id;
    private String name;
    private String releaseDate;
    private String description;
    private double rate;
    private String photoUrl;
    private Map<String, Object> likesDislikes;

    public Movie() {
        // Default constructor required for calls to DataSnapshot.getValue(Movie.class)
    }

    public Movie(String id, String name, String releaseDate, String description, double rate, String photoUrl) {
        this.id = id;
        this.name = name;
        this.releaseDate = releaseDate;
        this.description = description;
        this.rate = rate;
        this.photoUrl = photoUrl;
        this.likesDislikes = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Map<String, Object> getLikesDislikes() {
        return likesDislikes;
    }

    public void setLikesDislikes(Map<String, Object> likesDislikes) {
        this.likesDislikes = likesDislikes;
    }

    public void addLike(String userId) {
        likesDislikes.put(userId, "like");
    }

    public void addDislike(String userId) {
        likesDislikes.put(userId, "dislike");
    }

    public void removeReaction(String userId) {
        likesDislikes.remove(userId);
    }

    public int getLikeCount() {
        int count = 0;
        for (Object reaction : likesDislikes.values()) {
            if (reaction.equals("like")) {
                count++;
            }
        }
        return count;
    }

    public int getDislikeCount() {
        int count = 0;
        for (Object reaction : likesDislikes.values()) {
            if (reaction.equals("dislike")) {
                count++;
            }
        }
        return count;
    }
}
