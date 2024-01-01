package com.techcareer.userservice.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techcareer.userservice.entity.ShoppingCart;
import com.techcareer.userservice.entity.User;
import com.techcareer.userservice.payload.request.LoginRequest;
import com.techcareer.userservice.payload.request.SignupRequest;
import com.techcareer.userservice.payload.request.UpdateUserRequest;
import com.techcareer.userservice.payload.response.JwtResponse;
import com.techcareer.userservice.payload.response.MessageResponse;
import com.techcareer.userservice.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class UserService {
    @Value("${jwt.issuer_uri}")
    String jwtIssuerUri;

    @Value("${jwt.client_id}")
    String jwtClientId;

    @Value("${jwt.client_secret}")
    String jwtClientSecret;

    @Value("${jwt.grant_type}")
    String jwtGrantType;

    @Value("${jwt.scope}")
    String jwtScope;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RestTemplate restTemplate;

    public ResponseEntity<?> authenticateUser(LoginRequest loginRequest) {
        User user;
        try {
            user = userRepository.findByUsername(loginRequest.getUsername()).orElseThrow();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("User not found!"));
        }

        PasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body(new MessageResponse("User credentials are not valid"));
        }

        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(jwtIssuerUri);

        List<BasicNameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("grant_type", jwtGrantType));
        params.add(new BasicNameValuePair("client_id", jwtClientId));
        params.add(new BasicNameValuePair("client_secret", jwtClientSecret));
        params.add(new BasicNameValuePair("scope", jwtScope));

        String accessToken = "";
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(params));
            HttpResponse response = httpClient.execute(httpPost);

            // Handle the response
            String responseBody = EntityUtils.toString(response.getEntity());

            accessToken = extractAccessToken(responseBody);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity
                .ok(new JwtResponse(accessToken, user.getId(), user.getUsername(), user.getEmail()));
    }

    private static String extractAccessToken(String jsonResponse) {
        // Use Jackson for JSON parsing
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            return rootNode.path("access_token").asText();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ResponseEntity<?> registerUser(SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Email is already in use!"));
        }

        PasswordEncoder encoder = new BCryptPasswordEncoder();
        // Create new user's account
        User user = new User(signUpRequest.getUsername(), signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    public ResponseEntity<?> deleteUser(Long userId) {
        try {
            // Check if the user exists
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found!"));

            try {
                // Check user's shopping cart
                ShoppingCart shoppingCart = restTemplate.getForObject(
                        "http://SHOPPING-CART-SERVICE/api/shopping-cart/by-name/" + user.getUsername(),
                        ShoppingCart.class);

                restTemplate.delete("http://SHOPPING-CART-SERVICE/api/shopping-cart/" + shoppingCart.getId());
            } catch (Exception e) {
                // If shopping cart not found, continue with user deletion
            }

            // Delete the user
            userRepository.delete(user);

            return ResponseEntity.ok(new MessageResponse("User account deleted successfully!"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new MessageResponse("Internal Server Error"));
        }
    }

    public ResponseEntity<?> updateUser(Long userId, UpdateUserRequest updateUserRequest) {
        try {
            // Check if the user exists
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found!"));

            // Update password if provided
            if (updateUserRequest.getPassword() != null && !updateUserRequest.getPassword().isEmpty()) {
                PasswordEncoder encoder = new BCryptPasswordEncoder();
                user.setPassword(encoder.encode(updateUserRequest.getPassword()));
            }

            // Update email if provided
            if (updateUserRequest.getEmail() != null && !updateUserRequest.getEmail().isEmpty()) {
                if (userRepository.existsByEmail(updateUserRequest.getEmail())) {
                    return ResponseEntity.badRequest().body(new MessageResponse("Email is already in use!"));
                }
                user.setEmail(updateUserRequest.getEmail());
            }

            // Save the updated user
            userRepository.save(user);

            return ResponseEntity.ok(new MessageResponse("User account updated successfully!"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new MessageResponse("Internal Server Error"));
        }
    }
}
