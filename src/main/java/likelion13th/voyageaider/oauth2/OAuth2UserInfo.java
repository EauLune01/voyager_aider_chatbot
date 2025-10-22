package likelion13th.voyageaider.oauth2;

import java.util.Map;

public interface OAuth2UserInfo {

    // 각 제공자의 고유 사용자 ID
    String getProviderId();

    // 제공자 이름 (google, naver, kakao 등)
    String getProvider();

    // 사용자 이메일 주소
    String getEmail();

    // 사용자 이름 또는 닉네임
    String getName();

    // 사용자 정보가 담긴 원본 Map 데이터 반환
    Map<String, Object> getAttributes();
}
