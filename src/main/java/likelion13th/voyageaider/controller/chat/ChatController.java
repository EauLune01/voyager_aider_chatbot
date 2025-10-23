package likelion13th.voyageaider.controller.chat;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import likelion13th.voyageaider.dto.chat.request.ChatMessageRequest;
import likelion13th.voyageaider.dto.chat.response.AIResponse;
import likelion13th.voyageaider.dto.chat.response.ChatMessageResponse;
import likelion13th.voyageaider.dto.chat.response.ImageUploadResponse;
import likelion13th.voyageaider.dto.global.ApiResponse;
import likelion13th.voyageaider.exception.auth.UnauthorizedException;
import likelion13th.voyageaider.service.S3.S3UploaderService;
import lombok.RequiredArgsConstructor;
import likelion13th.voyageaider.domain.User;
import likelion13th.voyageaider.repository.user.UserRepository;
import likelion13th.voyageaider.service.AI.AIService;
import likelion13th.voyageaider.service.chat.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final AIService aiService;
    private final UserRepository userRepository;
    private final S3UploaderService s3UploaderService;

    private static final String CHAT_DESTINATION = "/queue/messages";
    private static final String S3_DIR_NAME = "chat-images"; // S3 저장 폴더 이름

    // --- 이미지 업로드 API ---
    /**
     * 채팅 이미지 업로드 (최소 1개, 최대 3개)
     */
    @PostMapping(value = "/api/chat/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse<ImageUploadResponse>> uploadChatImages(
            Authentication authentication,
            @RequestParam("images") List<MultipartFile> images
    ) throws IOException {

        if (authentication == null || authentication.getName() == null) {
            throw new UnauthorizedException("Image upload requires authentication.");
        }

        // 파일 개수: 1~3개
        if (images == null || images.isEmpty() || images.size() > 3) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, HttpStatus.BAD_REQUEST.value(),
                            "이미지는 1개 이상, 3개 이하로 업로드해야 합니다."));
        }

        // S3 업로드
        List<String> imageUrls = s3UploaderService.uploadImages(images, S3_DIR_NAME);

        ImageUploadResponse responseDto = new ImageUploadResponse(imageUrls);
        return ResponseEntity.ok(
                new ApiResponse<>(true, HttpStatus.OK.value(), "이미지 업로드 성공", responseDto)
        );
    }

    // --- WebSocket 메시지 처리 ---
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(
            Authentication authentication,
            @Valid @Payload ChatMessageRequest requestDto // STOMP는 @Payload 사용
    ) {
        if (authentication == null || authentication.getName() == null) {
            throw new UnauthorizedException("WebSocket: Authentication is required.");
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("WebSocket: User not found with username: " + username));

        // 1) 사용자 메시지 저장 (텍스트 + 선택적 이미지 0~3개, null 허용)
        chatService.saveUserMessage(user, requestDto);

        // 2) 사용자 메시지 에코 (이미지 없으면 null 그대로 내려보냄)
        ChatMessageResponse userResponse = ChatMessageResponse.builder()
                .senderName(user.getName())
                .content(requestDto.getContent())
                .imageUrls((requestDto.getImageUrls() == null || requestDto.getImageUrls().isEmpty())
                        ? null
                        : requestDto.getImageUrls())
                .build();

        messagingTemplate.convertAndSendToUser(
                user.getUsername(),
                CHAT_DESTINATION,
                userResponse
        );

        // 3) AI 응답 생성 (텍스트만)
        AIResponse ai;
        try {
            ai = aiService.getAiResponse(requestDto.getContent());
        } catch (Exception e) {
            ai = new AIResponse("죄송해요, 잠시 응답을 생성하지 못했어요. 잠시 후 다시 시도해 주세요.");
        }

        // 4) AI 응답 DB 저장 (이미지 없음 → null)
        chatService.saveAiMessage(user, ai.getContent(), null);

        // 5) AI 응답 전송 (이미지 없이)
        ChatMessageResponse aiResponse = ChatMessageResponse.builder()
                .senderName("AI")
                .content(ai.getContent())
                .imageUrls(null)
                .build();

        messagingTemplate.convertAndSendToUser(
                user.getUsername(),
                CHAT_DESTINATION,
                aiResponse
        );
    }

    // --- 채팅 기록 API ---
    @GetMapping("/api/chat/history")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getChatHistory(
            Authentication authentication
    ) {
        if (authentication == null || authentication.getName() == null) {
            throw new UnauthorizedException("채팅 기록을 보려면 로그인이 필요합니다.");
        }
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("History: User not found with username: " + username));

        List<ChatMessageResponse> chatHistory = chatService.getChatHistory(user);
        return ResponseEntity.ok(
                new ApiResponse<>(true, 200, "채팅 기록 조회 성공", chatHistory)
        );
    }
}
