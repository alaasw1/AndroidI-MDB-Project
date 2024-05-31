package com.example.android_imdb_project.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a Movie with its details such as name, release date, description, rate, photo URL, and likes.
 */
public class Movie {
    private String id;
    private String name;
    private String releaseDate;
    private String description;
    private double rate;
    private String photoUrl;
    private Map<String, Boolean> likes;

    /**
     * Default constructor for Movie. Initializes the likes map.
     */
    public Movie() {
        this.likes = new HashMap<>();
    }

    /**
     * Constructor for Movie with all fields.
     *
     * @param id          The ID of the movie.
     * @param name        The name of the movie.
     * @param releaseDate The release date of the movie.
     * @param description The description of the movie.
     * @param rate        The rating of the movie.
     * @param photoUrl    The URL of the movie's photo.
     */
    public Movie(String id, String name, String releaseDate, String description, double rate, String photoUrl) {
        this.id = id;
        this.name = name;
        this.releaseDate = releaseDate;
        this.description = description;
        this.rate = rate;
        this.photoUrl = photoUrl;
        this.likes = new HashMap<>();
    }

    /**
     * Gets the ID of the movie.
     *
     * @return The ID of the movie.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the ID of the movie.
     *
     * @param id The ID to set.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the name of the movie.
     *
     * @return The name of the movie.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the movie.
     *
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the release date of the movie.
     *
     * @return The release date of the movie.
     */
    public String getReleaseDate() {
        return releaseDate;
    }

    /**
     * Sets the release date of the movie.
     *
     * @param releaseDate The release date to set.
     */
    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    /**
     * Gets the description of the movie.
     *
     * @return The description of the movie.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the movie.
     *
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the rating of the movie.
     *
     * @return The rating of the movie.
     */
    public double getRate() {
        return rate;
    }

    /**
     * Sets the rating of the movie.
     *
     * @param rate The rating to set.
     */
    public void setRate(double rate) {
        this.rate = rate;
    }

    /**
     * Gets the photo URL of the movie.
     *
     * @return The photo URL of the movie.
     */
    public String getPhotoUrl() {
        return photoUrl;
    }

    /**
     * Sets the photo URL of the movie.
     *
     * @param photoUrl The photo URL to set.
     */
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    /**
     * Gets the likes map of the movie. The map contains user IDs as keys and a boolean value indicating if the user liked the movie.
     *
     * @return The likes map of the movie.
     */
    public Map<String, Boolean> getLikes() {
        return likes;
    }

    /**
     * Sets the likes map of the movie.
     *
     * @param likes The likes map to set.
     */
    public void setLikes(Map<String, Boolean> likes) {
        this.likes = likes;
    }

    /**
     * Gets the count of likes for the movie.
     *
     * @return The count of likes.
     */
    public int getLikeCount() {
        if (likes == null) {
            return 0;
        }
        return likes.size();
    }
}
