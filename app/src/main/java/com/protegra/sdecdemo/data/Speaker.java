package com.protegra.sdecdemo.data;

public class Speaker {
    public String id;
    public String photo_small;
    public String name;

    public Speaker() {

    }

    public Speaker(String id, String photo_small, String photo_large, String name, String role, String organization, String twitter, String website, String description) {
        this.id = id;
        this.photo_small = photo_small;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}