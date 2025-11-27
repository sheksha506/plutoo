package com.example.chain.dto;

public class JwtAuth {

    private String token;
    private String tokentype="Bearer";

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public JwtAuth(String token) {
        this.token = token;
    }
}
