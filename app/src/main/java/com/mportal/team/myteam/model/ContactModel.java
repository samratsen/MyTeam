package com.mportal.team.myteam.model;

import java.util.List;

/**
 * Created by samratsen on 2/2/15.
 */
public class ContactModel {
    private String name ="Unknown";
    private String idTag;
    private List<String> idTags;
    private String status="Offline";
    private List<Device> devices;

    public void setIdTags(List<String> idTags) {
        this.idTags = idTags;
        if (idTags == null || idTags.isEmpty()) return;
        this.idTag = idTags.get(0);
    }

    public void setIdTag(String idTag) {
        this.idTag = idTag;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Device> getDevices() {
        return devices;
    }

    public void setDevices(List<Device> devices) {
        this.devices = devices;
    }

    public List<String> getIdTags() {

        return idTags;
    }

    public String getIdTag() {

        return idTag;
    }

    public void setIdTag(List<String> tags) {

        this.idTags = tags;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }


}
