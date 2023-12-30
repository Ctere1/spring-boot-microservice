package com.techcareer.userservice.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.annotation.PostConstruct;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techcareer.userservice.entity.ERole;
import com.techcareer.userservice.entity.Role;
import com.techcareer.userservice.entity.User;
import com.techcareer.userservice.payload.request.LoginRequest;
import com.techcareer.userservice.payload.request.SignupRequest;
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
			int statusCode = response.getStatusLine().getStatusCode();
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

}
