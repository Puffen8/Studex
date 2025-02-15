package com.example.studex;

import androidx.annotation.Nullable;

import java.io.Serializable;

public class ListingData implements Serializable {
    private String title;
    private String description;
    private Float price;
    private String location;
    private String owner_id;
    private String image;
    private String id;

    public ListingData(String title, Float price, String description, String location, String owner_id, @Nullable String image, @Nullable String id) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.location = location;
        this.owner_id = owner_id;
        this.image = image;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getPrice() {
        return price.toString();
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public String getOwner_id() {
        return owner_id;
    }

    public String getImage() {
        return image;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setOwner_id(String owner_id) {
        this.owner_id = owner_id;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
