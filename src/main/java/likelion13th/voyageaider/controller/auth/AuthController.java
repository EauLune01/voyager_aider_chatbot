package likelion13th.voyageaider.controller.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import likelion13th.voyageaider.auth.jwt.JwtConstants;
import likelion13th.voyageaider.domain.User;
import likelion13th.voyageaider.dto.auth.request.RefreshTokenRequest;
import likelion13th.voyageaider.dto.auth.response.TokenResponse;
import likelion13th.voyageaider.dto.global.ApiResponse;
import likelion13th.voyageaider.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "인증 (Auth)", description = "토큰 재발급, 로그아웃 등 사용자 인증 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Access Token 재발급",
            description = "만료된 Access Token과 Refresh Token을 함께 보내 새로운 토큰들을 발급받습니다.",
            security = {} // Swagger 문서에서 보안 요구 제거
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "블랙리스트 또는 유효하지 않은 Refresh Token",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "DB에 Refresh Token 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody RefreshTokenRequest request) {

        String accessToken = null;
        if (authorization != null && authorization.startsWith("Bearer ")) {
            accessToken = authorization.substring(7);
        }

        TokenResponse tokenResponse = authService.reissueTokens(
                accessToken, request.getRefreshToken()
        );

        return ResponseEntity.ok(
                new ApiResponse<>(true, 200, "토큰이 성공적으로 재발급되었습니다.", tokenResponse)
        );
    }


    @Operation(
            summary = "로그아웃",
            description = "현재 로그인된 사용자를 로그아웃 처리하고 Refresh Token을 무효화합니다.",
            security = { @SecurityRequirement(name = "bearerAuth") } // 🔒 Swagger에서 JWT 인증 필요
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그아웃 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Access Token이 유효하지 않아 인증 실패",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal User loginUser,
            @RequestHeader(value = JwtConstants.HEADER_STRING, required = false) String authHeader
    ) {
        // ✅ Access Token 추출
        String accessToken = null;
        if (authHeader != null && authHeader.startsWith(JwtConstants.TOKEN_PREFIX)) {
            accessToken = authHeader.substring(JwtConstants.TOKEN_PREFIX.length());
        }

        // ✅ 로그아웃 처리 (DB RefreshToken 삭제 + Redis 블랙리스트 등록)
        authService.logout(loginUser, accessToken);

        return ResponseEntity.ok(
                new ApiResponse<>(true, HttpStatus.OK.value(), "성공적으로 로그아웃되었습니다.")
        );
    }
}