package com.example.climbingnav.auth.service;

import com.example.climbingnav.auth.config.KakaoProperties;
import com.example.climbingnav.auth.dto.KakaoTokenResponse;
import com.example.climbingnav.auth.dto.KakaoUserInfo;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
public class KakaoAuthService {
    private final KakaoProperties props;
    private final RestClient rest;

    public KakaoAuthService(KakaoProperties props, RestClient restClient) {
        this.props = props;
        this.rest = restClient;
    }

    public String buildAuthorizeUrl(String state) {
        String base = props.getAuthorizeUri();
        String q = "client_id=" + url(props.getClientId())
                + "&redirect_uri=" + url(props.getRedirectUri())
                + "&response_type=code"
                + "&state=" + url(state);
        return base + "?" + q;
    }

    public KakaoTokenResponse exchangeCodeForToken(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", props.getClientId());
        form.add("redirect_uri", props.getRedirectUri());
        form.add("code", code);
        Optional.ofNullable(props.getClientSecret())
                .filter(s -> !s.isBlank())
                .ifPresent(s -> form.add("client_secret", s));


        return rest.post()
                .uri(props.getTokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(KakaoTokenResponse.class);
    }


    public KakaoUserInfo fetchUserInfo(String accessToken) {
        return rest.get()
                .uri(props.getUserinfoUri())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(KakaoUserInfo.class);
    }


    private static String url(String s) { return URLEncoder.encode(s, StandardCharsets.UTF_8); }
}
