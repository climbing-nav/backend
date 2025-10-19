package com.example.climbingnav.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleUserInfo(
   String sub,
   String email,
   @JsonProperty("email_verified")
   boolean emailVerified,
   String name,
   @JsonProperty("picture")
   String pictureUrl
) {}
