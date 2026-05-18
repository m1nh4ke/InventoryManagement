package com.myproject.inventorymanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String tokenType;
    private String role;

    public AuthResponse(String accessToken, String role) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
        this.role = role;
    }
}
