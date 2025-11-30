package com.example.chain.controller;


import com.example.chain.dto.JwtAuth;
import com.example.chain.dto.LoginDto;
import com.example.chain.dto.SignupDTO;
import com.example.chain.dto.UserDto;
import com.example.chain.entity.User;
import com.example.chain.jwt.JwtTokenProvider;
import com.example.chain.repo.UserRespository;
import com.example.chain.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping(value = "/api")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRespository userRespository;





    @PostMapping("/signup")
    public ResponseEntity<String> register(@Valid @RequestBody SignupDTO signupDTO) {
        try {
            User user = new User();
            user.setPassword(signupDTO.getPassword());
            user.setEmail(signupDTO.getEmail());
            user.setUsername(signupDTO.getUsername());


            userService.saveUser(user);
            return new ResponseEntity<>("User successfully registered", HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDto loginDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            Optional<User> optionalUser = userService.getUserByEmail(loginDto.getEmail());
            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
            }
            User user = optionalUser.get();


            String token = jwtTokenProvider.generateToken(user);
            return ResponseEntity.ok(new JwtAuth(token));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }



    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(@RequestHeader ("Authorization") String authHeader){
        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7); // remove "Bearer "

        if(!jwtTokenProvider.isValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(userService.getUsers());
    }


    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email){
        User user = userRespository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return ResponseEntity.ok(new UserDto(user.getId(), user.getUsername(), user.getEmail()));
    }







}
