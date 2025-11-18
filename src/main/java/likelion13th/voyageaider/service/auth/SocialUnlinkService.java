package likelion13th.voyageaider.service.auth;

import likelion13th.voyageaider.dto.auth.response.OAuth2TokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialUnlinkService {

    private final WebClient webClient;

    // =================================================================
    // 🔐 Client ID & Secret 주입 (application.yml)
    // =================================================================

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;


    // =================================================================
    // 🚀 메인 메서드: 연동 해제 (Unlink)
    // =================================================================
    public void unlink(String provider, String providerId, String accessToken, String refreshToken) {
        // 1. 토큰 갱신 시도 (Refresh Token이 있을 경우)
        String validAccessToken = accessToken;
        if (StringUtils.hasText(refreshToken)) {
            String newAccessToken = refreshAccessToken(provider, refreshToken);
            if (newAccessToken != null) {
                validAccessToken = newAccessToken;
                log.info("✅ {} Access Token 갱신 완료, 갱신된 토큰으로 연동 해제를 진행합니다.", provider);
            }
        }

        // 2. 연동 해제 요청
        try {
            switch (provider.toLowerCase()) {
                case "google" -> unlinkGoogle(validAccessToken);
                default -> log.warn("지원하지 않는 Provider입니다: {}", provider);
            }
        } catch (Exception e) {
            // 소셜 연동 해제가 실패하더라도 우리 서비스 내부 회원 탈퇴는 계속 진행되어야 하므로 에러를 삼킴
            log.error("❌ 소셜 연동 해제 실패 (provider: {}): {}", provider, e.getMessage());
        }
    }

    // =================================================================
    // 🔄 공통: Access Token 갱신 로직
    // =================================================================
    private String refreshAccessToken(String provider, String refreshToken) {
        String url = "";
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        if ("google".equals(provider)) {
            url = "https://oauth2.googleapis.com/token";
            params.add("grant_type", "refresh_token");
            params.add("client_id", googleClientId);
            params.add("client_secret", googleClientSecret);
            params.add("refresh_token", refreshToken);
        } else {
            return null;
        }

        try {
            // ✅ 수정됨: uri(url)에 String을 바로 넣습니다. (uriBuilder 사용 X -> 에러 해결)
            OAuth2TokenResponse response = webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(params))
                    .retrieve()
                    .bodyToMono(OAuth2TokenResponse.class)
                    .block();

            if (response != null && StringUtils.hasText(response.getAccessToken())) {
                return response.getAccessToken();
            }
        } catch (Exception e) {
            log.warn("⚠️ {} 토큰 갱신 실패 (기존 Access Token으로 시도합니다): {}", provider, e.getMessage());
        }
        return null;
    }

    // =================================================================
    // ✂️ 각 Provider별 연동 해제 구현 (Host Not Specified 해결 버전)
    // =================================================================

    private void unlinkGoogle(String accessToken) {
        String url = "https://oauth2.googleapis.com/revoke";

        // ✅ UriComponentsBuilder 사용
        URI uri = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("token", accessToken)
                .build().toUri();

        webClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        log.info("✅ 구글 연동 해제 완료");
    }

}

