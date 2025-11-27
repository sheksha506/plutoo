package com.example.chain.service;

import com.example.chain.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    public User saveUser(User user);

    List<User> getUsers();

    Optional<User> getUserByEmail(String email);

}
