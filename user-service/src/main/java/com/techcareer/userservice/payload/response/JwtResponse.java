package com.techcareer.userservice.payload.response;

import java.util.HashSet;
import java.util.Set;

import com.techcareer.userservice.entity.Role;

public class JwtResponse {

	private Long id;
	private String username;
	private String email;
	private Set<Role> roles = new HashSet<>();

	private String type = "Bearer";
	private String token;

	public JwtResponse(String accessToken, Long id, String username, String email, Set<Role> roles) {
		this.token = accessToken;
		this.id = id;
		this.username = username;
		this.email = email;
		this.roles = roles;
	}

	public String getTokenType() {
		return type;
	}

	public void setTokenType(String tokenType) {
		this.type = tokenType;
	}

	public String getAccessToken() {
		return token;
	}

	public void setAccessToken(String accessToken) {
		this.token = accessToken;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Set<Role> getRoles() {
		return roles;
	}
}