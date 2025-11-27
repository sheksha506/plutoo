package com.example.backend_nearbyService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class BackendNearbyServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendNearbyServiceApplication.class, args);
	}


    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

}
