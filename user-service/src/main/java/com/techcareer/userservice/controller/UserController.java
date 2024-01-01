package com.techcareer.userservice.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.validation.Valid;

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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techcareer.userservice.entity.ERole;
import com.techcareer.userservice.entity.Role;
import com.techcareer.userservice.entity.ShoppingCart;
import com.techcareer.userservice.entity.User;
import com.techcareer.userservice.payload.request.LoginRequest;
import com.techcareer.userservice.payload.request.SignupRequest;
import com.techcareer.userservice.payload.request.UpdateUserRequest;
import com.techcareer.userservice.payload.response.JwtResponse;
import com.techcareer.userservice.payload.response.MessageResponse;
import com.techcareer.userservice.repository.RoleRepository;
import com.techcareer.userservice.repository.UserRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/user")
@Tag(name = "user", description = "User Endpoints")
public class UserController {

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
	RoleRepository roleRepository;

	@Autowired
	RestTemplate restTemplate;

	@PostPersist
	@PostLoad
	@PostConstruct
	private void initializeRoles() {
		// Check if roles exist, and insert them if not
		if (!roleRepository.existsByName(ERole.ROLE_USER)) {
			roleRepository.save(new Role(ERole.ROLE_USER));
		}

		if (!roleRepository.existsByName(ERole.ROLE_MODERATOR)) {
			roleRepository.save(new Role(ERole.ROLE_MODERATOR));
		}

		if (!roleRepository.existsByName(ERole.ROLE_ADMIN)) {
			roleRepository.save(new Role(ERole.ROLE_ADMIN));
		}
	}

	@PostMapping("/signin")
	@Operation(summary = "Authenticate user", description = "Authenticate user with username and password")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "User authenticated successfully!"),
			@ApiResponse(responseCode = "400", description = "Error: Username is not found!"),
			@ApiResponse(responseCode = "400", description = "Error: Password is not correct!") })
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
		User user;
		try {
			user = userRepository.findByUsername(loginRequest.getUsername()).orElseThrow();
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: User not found!"));
		}

		PasswordEncoder encoder = new BCryptPasswordEncoder();
		if (!encoder.matches(loginRequest.getPassword(), user.getPassword())) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: User credentials are not valid"));
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
				.ok(new JwtResponse(accessToken, user.getId(), user.getUsername(), user.getEmail(), user.getRoles()));
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

	@PostMapping("/signup")
	@Operation(summary = "Create authenticated users", description = "Create Authenticated user with username, email and password")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "User registered successfully!"),
			@ApiResponse(responseCode = "400", description = "Error: Username is already taken!"),
			@ApiResponse(responseCode = "400", description = "Error: Email is already in use!") })
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
		if (userRepository.existsByUsername(signUpRequest.getUsername())) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
		}

		if (userRepository.existsByEmail(signUpRequest.getEmail())) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
		}

		PasswordEncoder encoder = new BCryptPasswordEncoder();
		// Create new user's account
		User user = new User(signUpRequest.getUsername(), signUpRequest.getEmail(),
				encoder.encode(signUpRequest.getPassword()));

		Set<String> strRoles = signUpRequest.getRole();
		Set<Role> roles = new HashSet<>();

		if (strRoles == null) {
			Role userRole = roleRepository.findByName(ERole.ROLE_USER)
					.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
			roles.add(userRole);
		} else {
			strRoles.forEach(role -> {
				switch (role) {
					case "admin":
						Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
								.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
						roles.add(adminRole);

						break;
					case "mod":
						Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
								.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
						roles.add(modRole);

						break;
					default:
						Role userRole = roleRepository.findByName(ERole.ROLE_USER)
								.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
						roles.add(userRole);
				}
			});
		}

		user.setRoles(roles);
		userRepository.save(user);

		return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
	}

	@DeleteMapping("/delete/{userId}")
	@Operation(summary = "Delete user account", description = "Delete user account by ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "User account deleted successfully!"),
			@ApiResponse(responseCode = "400", description = "Error: User not found!"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error")
	})
	public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
		try {
			// Check if the user exists
			User user = userRepository.findById(userId)
					.orElseThrow(() -> new EntityNotFoundException("Error: User not found!"));

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

	@PutMapping("/update/{userId}")
	@Operation(summary = "Update user account", description = "Update user account information by ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "User account updated successfully!"),
			@ApiResponse(responseCode = "400", description = "Error: User not found!"),
			@ApiResponse(responseCode = "400", description = "Error: New password is not valid!"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error")
	})
	public ResponseEntity<?> updateUser(@PathVariable Long userId,
			@Valid @RequestBody UpdateUserRequest updateUserRequest) {
		try {
			// Check if the user exists
			User user = userRepository.findById(userId)
					.orElseThrow(() -> new EntityNotFoundException("Error: User not found!"));

			// Update password if provided
			if (updateUserRequest.getPassword() != null && !updateUserRequest.getPassword().isEmpty()) {
				PasswordEncoder encoder = new BCryptPasswordEncoder();
				user.setPassword(encoder.encode(updateUserRequest.getPassword()));
			}

			// Update email if provided
			if (updateUserRequest.getEmail() != null && !updateUserRequest.getEmail().isEmpty()) {
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
