package com.study.codingswamp.auth.oauth;

import com.study.codingswamp.auth.oauth.response.GithubOauthTokenResponse;
import com.study.codingswamp.auth.oauth.response.GithubProfileResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

@Slf4j
@Component
public class GithubOauthClient {

    private final String clientId;
    private final String clientSecrets;
    private final String redirectUri;
    private final String accessTokenUrl;
    private final String profileUrl;

    public GithubOauthClient(
            @Value("${oauth2.github.client-id}") String clientId,
            @Value("${oauth2.github.client-secrets}") String clientSecrets,
            @Value("${oauth2.github.redirect-uri}") String redirectUri,
            @Value("${oauth2.github.access-token-url}") String accessTokenUrl,
            @Value("${oauth2.github.profile-url}") String profileUrl) {
        this.clientId = clientId;
        this.clientSecrets = clientSecrets;
        this.redirectUri = redirectUri;
        this.accessTokenUrl = accessTokenUrl;
        this.profileUrl = profileUrl;
    }

    public GithubProfileResponse getProfile(String code) {
        GithubOauthTokenResponse tokenResponse = getTokenResponse(code);
        log.info("tokenResponse={}", tokenResponse);
        GithubProfileResponse githubProfile = getGithubProfile(tokenResponse);
        log.info("githubProfile={}", githubProfile);
        return githubProfile;
    }

    private GithubOauthTokenResponse getTokenResponse(String code) {
        return WebClient.create()
                .post()
                .uri(accessTokenUrl)
                .headers(header -> {
                    header.setBasicAuth(clientId, clientSecrets);
                    header.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    header.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                    header.setAcceptCharset(Collections.singletonList(StandardCharsets.UTF_8));
                })
                .bodyValue(tokenRequest(code))
                .retrieve()
                .bodyToMono(GithubOauthTokenResponse.class)
                .block();
    }

    private MultiValueMap<String, String> tokenRequest(String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", code);
        formData.add("grant_type", "authorization_code");
        formData.add("redirect_uri", redirectUri);
        return formData;
    }

    private GithubProfileResponse getGithubProfile(GithubOauthTokenResponse tokenResponse) {
        Map<String, Object> userAttributes = getUserAttributes(tokenResponse);
        log.info("userAttributes={}", userAttributes);
        return GithubProfileResponse.builder()
                .githubId(String.valueOf(userAttributes.get("id")))
                .username(String.valueOf(userAttributes.get("login")))
                .email(String.valueOf(userAttributes.get("email")))
                .imageUrl(String.valueOf(userAttributes.get("avatar_url")))
                .profileUrl(String.valueOf(userAttributes.get("html_url")))
                .build();
    }

    private Map<String, Object> getUserAttributes(GithubOauthTokenResponse tokenResponse) {
        return WebClient.create()
                .get()
                .uri(profileUrl)
                .headers(header -> header.setBearerAuth(tokenResponse.getAccessToken()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
    }
}
