/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.abondar.javaLabs;


import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 *
 * @author alex
 */

public class EventBean {
    
    private String name;
    private String category;
    private String date ;
    private String time;
    @JsonIgnore
    private String eventID;
    private String place ;
    private String description;
    
    private String geoPos;
    
    
    public String getCategory() {
        return category;
    }

    public String getGeoPos() {
        return geoPos;
    }

    public void setGeoPos(String geoPos) {
        this.geoPos = geoPos;
    }

    
    public void setCategory(String category) {
        this.category = category;
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

  

    
    
}
