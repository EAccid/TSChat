package com.eaccid.tschat;

public class User {

    private String uid;
    private String name;
    private String email;
    private String photoUrl;

    public User() {
    }

    public User(String name, String email, String photoUrl) {
        this.name = name;
        this.email = email;
        this.photoUrl = photoUrl;
    }

    public User(String uid, String name, String email, String photoUrl) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.photoUrl = photoUrl;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String id) {
        this.uid = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + uid +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", photoUrl='" + photoUrl + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (!getUid().equals(user.getUid())) return false;
        if (!getName().equals(user.getName())) return false;
        return getEmail().equals(user.getEmail());

    }

    @Override
    public int hashCode() {
        int result = getUid().hashCode();
        result = 31 * result + getName().hashCode();
        result = 31 * result + getEmail().hashCode();
        return result;
    }
}
