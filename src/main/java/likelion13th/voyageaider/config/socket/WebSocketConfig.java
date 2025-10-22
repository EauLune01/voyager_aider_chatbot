package likelion13th.voyageaider.config.socket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompHandler stompHandler;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 클라이언트가 WebSocket 연결을 시작할 엔드포인트
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        // 1. [Subscribe] (서버 -> 클라) "구독" 접두사 설정
        //    서버가 클라이언트에게 메시지를 보낼 때 사용하는 '메시지 브로커'를 활성화합니다.
        //    - /topic: 공용 게시판 (N:N, 그룹 채팅)
        //    - /queue: 개인 사서함 (1:1, 개인 채팅/알림)
        registry.enableSimpleBroker("/topic", "/queue");

        // 2. [Publish] (클라 -> 서버) "발행" 접두사 설정
        //    클라이언트가 서버로 메시지를 보낼 때 사용하는 주소의 접두사입니다.
        //    (예: /app/chat.sendMessage -> @MessageMapping("/chat.sendMessage") 호출)
        registry.setApplicationDestinationPrefixes("/app");

        // 3. [1:1 전용] User Destination 접두사 설정
        //    /queue(개인 사서함)와 함께 작동하여, 특정 사용자 1명에게 메시지를 보낼 때 사용됩니다.
        //    컨트롤러의 convertAndSendToUser() 메서드가 이 설정을 기반으로
        //    (예: /user/{username}/queue/messages) 같은 고유한 주소를 만들어냅니다.
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompHandler);
    }
}