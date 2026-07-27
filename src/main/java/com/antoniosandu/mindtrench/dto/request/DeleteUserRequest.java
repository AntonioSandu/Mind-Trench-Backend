package com.antoniosandu.mindtrench.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DeleteUserRequest {

    @NotBlank
    @Size(min = 8)
    private String password;

    public DeleteUserRequest() {
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
