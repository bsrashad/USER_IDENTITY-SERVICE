package com.thbs.usercreation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Generates getters, setters, toString, equals, and hashCode methods
@Builder // Builder pattern for easy object creation
@AllArgsConstructor // Generates a constructor with all arguments
@NoArgsConstructor
public class AuthenticationResponse {

	  @JsonProperty("access_token") // Specifies the JSON property name for the access token field
	  private String accessToken; // Represents the access token field of the authentication response

	  private String message;
	}
