package com.example.chain.repo;

import com.example.chain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRespository extends JpaRepository<User, Long> {

        boolean existsByEmail(String email);
        Optional<User> getUserByEmail(String email);
        Optional<User> findByEmail(String email);

}
