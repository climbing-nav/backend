package com.example.climbingnav.auth.client;

import com.example.climbingnav.auth.dto.GoogleTokenResponse;
import com.example.climbingnav.auth.dto.GoogleUserInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class GoogleOAuthClient {
    private final String clientId, clientSecret, redirectUri, scope, authUri, tokenUri, userInfoUrl;
    private final RestClient restClient;

    public GoogleOAuthClient(
            @Value("${google.client-id}") String clientId,
            @Value("${google.client-secret}") String clientSecret,
            @Value("${google.redirect-uri}") String redirectUri,
            @Value("${google.scope:openid email profile}") String scope,
            @Value("${google.auth-uri:https://accounts.google.com/o/oauth2/v2/auth}") String authUri,
            @Value("${google.token-uri:https://oauth2.googleapis.com/token}") String tokenUri,
            @Value("${google.userinfo-uri:https://openidconnect.googleapis.com/v1/userinfo}") String userInfoUrl,
            RestClient restClient
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.scope = scope;
        this.authUri = authUri;
        this.tokenUri = tokenUri;
        this.userInfoUrl = userInfoUrl;
        this.restClient = restClient;
    }


    public String buildAuthRedirectUrl(String state) {
        String query = String.format(
                "client_id=%s&redirect_uri=%s&response_type=code&scope=%s&state=%s&access_type=offline&prompt=consent",
                enc(clientId), enc(redirectUri), enc(scope), enc(state));
        return authUri + "?" + query;
    }

    // Authorization code -> AccessToken으로 교환
    public GoogleTokenResponse exchangeCodeForToken(String code) {
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("code", code);
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("redirect_uri", redirectUri);
        form.add("grant_type", "authorization_code");

       return restClient.post()
               .uri(tokenUri)
               .contentType(MediaType.APPLICATION_FORM_URLENCODED)
               .body(form)
               .retrieve()
               .body(GoogleTokenResponse.class);
    }

    public GoogleUserInfo fetchUserInfo(String accessToken) {
        return restClient.get()
                .uri(userInfoUrl)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(GoogleUserInfo.class);
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
