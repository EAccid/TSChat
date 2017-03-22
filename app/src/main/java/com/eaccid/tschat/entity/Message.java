package com.eaccid.tschat.entity;

public class Message {
    private String id;
    private String uid;
    private String text;
    private String name;
    private String photoUrl;
    private String imageUrl;

    public Message() {
    }

    public Message(String text, String uid, String name, String photoUrl, String imageUrl) {
        this.uid = uid;
        this.text = text;
        this.name = name;
        this.photoUrl = photoUrl;
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id='" + id + '\'' +
                ", uid='" + uid + '\'' +
                ", text='" + text + '\'' +
                ", name='" + name + '\'' +
                ", photoUrl='" + photoUrl + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
