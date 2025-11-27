package com.example.backend_followService.service;


import com.example.backend_followService.dto.UserDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class UserClient {

    @Autowired
    private RestTemplate restTemplate;

    public Long getUserIdByEmail(String email) {

        String url = "http://localhost:8080/api/email/" + email;

        ResponseEntity<UserDto> response = restTemplate.getForEntity(url, UserDto.class);

        return response.getBody().getId();
    }
}

