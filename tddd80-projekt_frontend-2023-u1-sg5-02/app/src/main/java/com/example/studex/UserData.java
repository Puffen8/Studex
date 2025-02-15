package com.example.studex;

import org.jetbrains.annotations.Nullable;
public class UserData {
    private String username;
    private String email;
    private String password;
    private String first_name;
    private String last_name;
    private String phone_number;
    private String created_at;
    private String id;

    public UserData(String username, String email, String password, String firstName, String lastName, String phoneNumber, @Nullable String created_at, @Nullable String id) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.first_name = firstName;
        this.last_name = lastName;
        this.phone_number = phoneNumber;
        this.created_at = created_at;
        this.id = id;
    }

    public UserData(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public UserData(String firstName, String lastName, String phoneNumber) {
        this.first_name = firstName;
        this.last_name = lastName;
        this.phone_number = phoneNumber;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
