package com.example.chain.serviceImp;

import com.example.chain.entity.User;
import com.example.chain.repo.UserRespository;
import com.example.chain.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class UserSerivceImp implements UserService {

    @Autowired
    private UserRespository userRespository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Override
    public User saveUser(User user) {

        if(userRespository.existsByEmail(user.getEmail())){
            throw new IllegalArgumentException("Email already exists");
        }
        String password = passwordEncoder.encode(user.getPassword());
        user.setPassword(password);
        return userRespository.save(user);
    }



    @Override
    public List<User> getUsers() {
        return userRespository.findAll();
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRespository.findByEmail(email);
    }


}
