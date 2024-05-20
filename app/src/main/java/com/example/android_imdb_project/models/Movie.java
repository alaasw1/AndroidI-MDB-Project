package com.example.android_imdb_project.models;

public class Movie {
    private String name;
    private String releaseDate;
    private String description;
    private double rate;
    private String photoUrl;

    public Movie() {}

    public String getName() {
        return name;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getDescription() {
        return description;
    }

    public double getRate() {
        return rate;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public Movie(String name, String releaseDate, String description, double rate, String photoUrl) {
        this.name = name;
        this.releaseDate = releaseDate;
        this.description = description;
        this.rate = rate;
        this.photoUrl = photoUrl;
    }

}
