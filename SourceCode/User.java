package com.example.spotme;

public class User {
    private String userId;
    private String email;
    private String phone;
    private String name;
    private String userType;

    public User() {}

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
}