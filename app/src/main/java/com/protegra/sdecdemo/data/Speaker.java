package com.protegra.sdecdemo.data;

public class Speaker {
    public String id;
    public String photo_small;
    public String photo_large;
    public String name;
    public String role;
    public String organization;
    public String twitter;
    public String website;
    public String description;
    public String __version;
    public Boolean __deleted;

    public Speaker() {

    }

    public Speaker(String id, String photo_small, String photo_large, String name, String role, String organization, String twitter, String website, String description) {
        this.id = id;
        this.photo_small = photo_small;
        this.photo_large = photo_large;
        this.name = name;
        this.role = role;
        this.organization = organization;
        this.twitter = twitter;
        this.website = website;
        this.description = description;
    }

    @Override
    public String toString() {
        return name;
    }
}