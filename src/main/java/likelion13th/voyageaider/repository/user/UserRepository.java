package likelion13th.voyageaider.repository.user;

import likelion13th.voyageaider.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 최초 소셜 로그인 시 사용 (회원가입 여부 확인)
    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    // 토큰 재발급 시 사용
    Optional<User> findByRefreshToken(String refreshToken);

    //중복 X 필드로 User 객체 1개 조회
    Optional<User> findByUsername(String username);
}