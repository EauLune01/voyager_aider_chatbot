package likelion13th.voyageaider.config.socket;

import likelion13th.voyageaider.auth.jwt.JwtTokenProvider;
import likelion13th.voyageaider.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // 1. CONNECT: 최초 연결 시 JWT 검증 및 Authentication 저장
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String jwtToken = accessor.getFirstNativeHeader("Authorization");
            if (jwtToken != null && jwtToken.startsWith("Bearer ") && jwtTokenProvider.validateToken(jwtToken.substring(7))) {
                String token = jwtToken.substring(7);
                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                // SecurityContextHolder.getContext().setAuthentication(authentication); // <--- 여기서 설정해도 SEND 스레드에는 적용 안 됨
                accessor.setUser(authentication); // <--- 세션에 User 정보 저장 (핵심)
                log.info("[STOMP Connect] User authenticated: {}", authentication.getName());
            } else {
                log.warn("[STOMP Connect] Connection attempt with invalid/missing token.");
                // 여기서 에러를 throw하면 연결 거부 가능
            }
        }
        // 2. SEND: 메시지 전송 시, 저장된 Authentication을 SecurityContext에 설정 (★핵심★)
        else if (StompCommand.SEND.equals(accessor.getCommand())) {
            // accessor.getUser()로 CONNECT 시점에 저장해둔 Authentication 객체를 꺼냄
            Authentication authentication = (Authentication) accessor.getUser();

            // SecurityContextHolder에 설정 -> @AuthenticationPrincipal / Authentication 파라미터가 인식할 수 있게 됨
            if (authentication != null) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("[STOMP Send] Set SecurityContext for user: {}", authentication.getName());
            } else {
                // 인증 정보가 없으면 메시지 처리 거부 (예외 발생 등)
                log.warn("[STOMP Send] Message received without user authentication in session: {}", accessor.getSessionId());
                // throw new UnauthorizedException("Authentication required to send messages.");
            }
        }
        // 3. DISCONNECT: 연결 종료 시 Context 정리 (선택적)
        else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            Authentication authentication = (Authentication) accessor.getUser();
            if (authentication != null) {
                log.info("[STOMP Disconnect] User disconnected: {}", authentication.getName());
                SecurityContextHolder.clearContext(); // 스레드 Context 정리
            } else {
                log.warn("[STOMP Disconnect] Received for unknown user session: {}", accessor.getSessionId());
            }
        }

        return message;
    }
}