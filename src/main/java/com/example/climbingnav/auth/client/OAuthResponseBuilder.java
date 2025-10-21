package com.example.climbingnav.auth.client;

import com.example.climbingnav.auth.dto.OAuthTokenResponse;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class OAuthResponseBuilder {

    public ResponseEntity<Void> successRedirect(String redirectUrl, OAuthTokenResponse tokenResponse) {
        HttpHeaders headers = new HttpHeaders();

        ResponseCookie access = ResponseCookie.from("ACCESS", tokenResponse.access())
                .httpOnly(false)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(DurationStyle.detectAndParse(tokenResponse.accessSeconds()))
                .build();

        ResponseCookie refresh = ResponseCookie.from("REFRESH", tokenResponse.refresh())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(DurationStyle.detectAndParse(tokenResponse.refreshSeconds()))
                .build();

        headers.add(HttpHeaders.SET_COOKIE, refresh.toString());
        headers.add(HttpHeaders.SET_COOKIE, access.toString());
        headers.add(HttpHeaders.LOCATION, redirectUrl);
        return ResponseEntity.status(HttpStatus.FOUND).headers(headers).build();
    }

    public ResponseEntity<Void> failureRedirect(String redirectUrl, String reason) {
        return ResponseEntity.status(302)
                .header(HttpHeaders.LOCATION, redirectUrl + "?error")
                .build();
    }
}
