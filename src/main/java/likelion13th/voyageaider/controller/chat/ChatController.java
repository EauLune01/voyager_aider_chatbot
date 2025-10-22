package likelion13th.voyageaider.controller.chat;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import likelion13th.voyageaider.domain.ChatMessage;
import likelion13th.voyageaider.domain.User;
import likelion13th.voyageaider.dto.chat.request.ChatMessageRequest;
import likelion13th.voyageaider.dto.chat.response.ChatMessageResponse;
import likelion13th.voyageaider.dto.global.ApiResponse;
import likelion13th.voyageaider.exception.auth.UnauthorizedException;
import likelion13th.voyageaider.repository.user.UserRepository;
import likelion13th.voyageaider.service.AI.AIService;
import likelion13th.voyageaider.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final AIService aiService;

    // 1:1 채팅 메시지를 보낼 목적지 (Destination)
    // 클라이언트는 '/user/queue/messages'를 구독해야 함
    private static final String CHAT_DESTINATION = "/queue/messages";

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(
            Authentication authentication,
            @Valid ChatMessageRequest requestDto
    ) {
        // 1. Authentication 객체에서 username 추출
        if (authentication == null || authentication.getName() == null) {
            throw new UnauthorizedException("WebSocket: Authentication is required.");
        }
        String username = authentication.getName(); // 예: "google_110..."

        // 2.  username으로 DB에서 '진짜' User 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("WebSocket: User not found with username: " + username));

        // 3. User 객체 사용
        chatService.saveUserMessage(user, requestDto);
        ChatMessageResponse userResponse = new ChatMessageResponse(
                user.getName(),
                requestDto.getContent()
        );
        messagingTemplate.convertAndSendToUser(
                user.getUsername(),
                CHAT_DESTINATION,
                userResponse
        );

        // --- AI 연동 ---
        String aiContent = aiService.getAiResponse(requestDto.getContent());
        chatService.saveAiMessage(user, aiContent);
        ChatMessageResponse aiResponse = new ChatMessageResponse("AI", aiContent);
        messagingTemplate.convertAndSendToUser(
                user.getUsername(),
                CHAT_DESTINATION,
                aiResponse
        );
    }

    @GetMapping("/api/chat/history")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getChatHistory(
            @AuthenticationPrincipal User user
    ) {
        // 1. 예외 처리
        if (user == null) {
            throw new UnauthorizedException("채팅 기록을 보려면 로그인이 필요합니다.");
        }

        // 2. 서비스 호출 (DB에서 기록 조회)
        List<ChatMessageResponse> chatHistory = chatService.getChatHistory(user);

        // 3. 반환
        return ResponseEntity.ok(
                new ApiResponse<>(true, 200, "채팅 기록 조회 성공", chatHistory)
        );
    }
}