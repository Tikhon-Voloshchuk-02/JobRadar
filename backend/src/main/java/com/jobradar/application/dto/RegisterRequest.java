package com.jobradar.application.dto;

public class RegisterRequest {

    private String firstname;
    private String lastname;
    private String email;
    private String password;

    public RegisterRequest() {}

    //GETTERS

    public String getFirstname() { return firstname; }

    public String getLastname() { return lastname; }

    public String getEmail() { return email; }

    public String getPassword() { return password; }

    //SETTERS

    public void setFirstname(String firstname) { this.firstname = firstname; }

    public void setLastname(String lastname) { this.lastname = lastname; }

    public void setEmail(String email) { this.email = email; }

    public void setPassword(String password) { this.password = password; }
}
