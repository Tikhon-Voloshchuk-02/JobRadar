package com.jobradar.application.dto.auth;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RegisterRequest {

    private String firstname;
    private String lastname;
    private String email;
    private String password;

    private String confirmPassword;

    public RegisterRequest() {}

    //GETTERS

    public String getFirstname() { return firstname; }

    public String getLastname() { return lastname; }

    public String getEmail() { return email; }

    public String getPassword() { return password; }

    public String getConfirmPassword() { return confirmPassword; }

    //SETTERS

    public void setFirstname(String firstname) { this.firstname = firstname; }

    public void setLastname(String lastname) { this.lastname = lastname; }

    public void setEmail(String email) { this.email = email; }

    public void setPassword(String password) { this.password = password; }

    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
