package com.example.climbingnav.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "kakao")
public class KakaoProperties {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String authorizeUri;
    private String tokenUri;
    private String userinfoUri;
    private String frontendRedirect;
    private String jwtSecret;
    private long jwtExpSeconds;
}
