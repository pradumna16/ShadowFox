package com.thestart.fitness_gain.Model;

public class UserDataModel {
    private String fullname;
    private String phone;
    private String email;
    private String passwd;
    private String profileImageURL;

    public UserDataModel() {
    }

    public UserDataModel(String fullname, String phone, String email, String passwd, String profileImageURL) {
        this.fullname = fullname;
        this.phone = phone;
        this.email = email;
        this.passwd = passwd;
        this.profileImageURL = profileImageURL;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public String getProfileImageURL() {
        return profileImageURL;
    }

    public void setProfileImageURL(String profileImageURL) {
        this.profileImageURL = profileImageURL;
    }
}
