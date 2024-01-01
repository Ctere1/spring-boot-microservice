package com.techcareer.userservice.payload.request;

import jakarta.validation.constraints.*;

public class UpdateUserRequest {

    @Size(min = 6, max = 40)
    @NotEmpty(message = "Password cannot be empty")
    private String password;

    @NotBlank
    @Size(max = 50)
    @Email(message = "Invalid email address")
    private String email;

    // Getters and setters

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}