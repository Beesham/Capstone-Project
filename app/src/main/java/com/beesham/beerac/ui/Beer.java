package com.beesham.beerac.ui;

/**
 * Created by beesham on 24/01/17.
 */

public class Beer {
    private String name;
    private String id;
    private String description;
    private String abv;
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

    public boolean isLabels() {
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

}
