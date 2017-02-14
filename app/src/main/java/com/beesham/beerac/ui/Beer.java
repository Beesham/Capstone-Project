package com.beesham.beerac.ui;

/**
 * Created by beesham on 24/01/17.
 * Beer object
 * Holds details about a given beer
 */

public class Beer {
    private String name;
    private String id;
    private String description;
    private String abv;
    private String foodParings;
    private String isOrganic;
    private boolean labels;
    private int year;
    private String url_icon;
    private String url_medium;
    private String url_large;

    public Beer(String name,
                String id,
                String description,
                String abv,
                boolean labels,
                int year) {

        this.name = name;
        this.id = id;
        this.description = description;
        this.abv = abv;
        this.labels = labels;
        this.year = year;
    }


    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getAbv() {
        return abv;
    }

    public boolean hasLabels() {
        return labels;
    }

    public int getYear() {
        return year;
    }

    public String getUrl_icon() {
        return url_icon;
    }

    public void setUrl_icon(String url_icon) {
        this.url_icon = url_icon;
    }

    public String getUrl_medium() {
        return url_medium;
    }

    public void setUrl_medium(String url_medium) {
        this.url_medium = url_medium;
    }

    public String getUrl_large() {
        return url_large;
    }

    public void setUrl_large(String url_large) {
        this.url_large = url_large;
    }

    public String getFoodParings() {
        return foodParings;
    }

    public void setFoodPairings(String foodParings) {
        this.foodParings = foodParings;
    }

    public String getIsOrganic() {
        return isOrganic;
    }

    public void setIsOrganic(String isOrganic) {
        this.isOrganic = isOrganic;
    }


    @Override
    public String toString() {
        return "Beer{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", description='" + description + '\'' +
                ", abv='" + abv + '\'' +
                ", labels=" + labels +
                ", year=" + year +
                ", url_icon='" + url_icon + '\'' +
                ", url_medium='" + url_medium + '\'' +
                ", url_large='" + url_large + '\'' +
                '}';
    }
}
