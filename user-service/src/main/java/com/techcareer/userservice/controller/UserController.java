package com.techcareer.userservice.controller;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techcareer.userservice.payload.request.LoginRequest;
import com.techcareer.userservice.payload.request.SignupRequest;
import com.techcareer.userservice.payload.request.UpdateUserRequest;
import com.techcareer.userservice.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/user")
@Tag(name = "user", description = "User Endpoints")
public class UserController {

	@Autowired
	UserService userService;

	@PostMapping("/signin")
	@Operation(summary = "Authenticate user", description = "Authenticate user with username and password")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "User authenticated successfully!"),
			@ApiResponse(responseCode = "400", description = "Username is not found!"),
			@ApiResponse(responseCode = "400", description = "Password is not correct!") })
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
		return userService.authenticateUser(loginRequest);
	}

	@PostMapping("/signup")
	@Operation(summary = "Create authenticated users", description = "Create Authenticated user with username, email and password")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "User registered successfully!"),
			@ApiResponse(responseCode = "400", description = "Username is already taken!"),
			@ApiResponse(responseCode = "400", description = "Email is already in use!") })
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
		return userService.registerUser(signUpRequest);
	}

	@DeleteMapping("/delete/{userId}")
	@Operation(summary = "Delete user account", description = "Delete user account by ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "User account deleted successfully!"),
			@ApiResponse(responseCode = "400", description = "User not found!"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error")
	})
	public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
		return userService.deleteUser(userId);
	}

	@PutMapping("/update/{userId}")
	@Operation(summary = "Update user account", description = "Update user account information by ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "User account updated successfully!"),
			@ApiResponse(responseCode = "400", description = "User not found!"),
			@ApiResponse(responseCode = "400", description = "New password is not valid!"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error")
	})
	public ResponseEntity<?> updateUser(@PathVariable Long userId,
			@Valid @RequestBody UpdateUserRequest updateUserRequest) {
		return userService.updateUser(userId, updateUserRequest);
	}

}
