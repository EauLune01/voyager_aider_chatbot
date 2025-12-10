package likelion13th.voyageaider.controller.chat;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import likelion13th.voyageaider.domain.User;
import likelion13th.voyageaider.dto.chat.request.ChatMessageRequest;
import likelion13th.voyageaider.dto.chat.response.ChatMessageResponse;
import likelion13th.voyageaider.dto.chat.response.ImageUploadResponse;
import likelion13th.voyageaider.dto.global.ApiResponse;
import likelion13th.voyageaider.exception.auth.UnauthorizedException;
import likelion13th.voyageaider.repository.user.UserRepository;
import likelion13th.voyageaider.service.S3.S3UploaderService;
import likelion13th.voyageaider.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserRepository userRepository;
    private final S3UploaderService s3UploaderService;

    private static final String S3_DIR_NAME = "chat-images";

    // --- WebSocket 메시지 처리 ---
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(
            Authentication authentication,
            @Valid @Payload ChatMessageRequest requestDto
    ) {
        // 1. 인증 확인
        if (authentication == null || authentication.getName() == null) {
            throw new UnauthorizedException("WebSocket: Authentication is required.");
        }

        // 2. 유저 조회
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("WebSocket: User not found with username: " + username));

        // 3. 서비스 호출
        chatService.processMessage(user, requestDto);
    }

    // --- 이미지 업로드 API  ---
    @PostMapping(value = "/api/chat/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse<ImageUploadResponse>> uploadChatImages(
            Authentication authentication,
            @RequestParam("images") List<MultipartFile> images
    ) throws IOException {

        if (authentication == null || authentication.getName() == null) {
            throw new UnauthorizedException("Image upload requires authentication.");
        }
        if (images == null || images.isEmpty() || images.size() > 3) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, HttpStatus.BAD_REQUEST.value(), "이미지는 1~3개만 가능합니다."));
        }

        List<String> imageUrls = s3UploaderService.uploadImages(images, S3_DIR_NAME);
        return ResponseEntity.ok(new ApiResponse<>(true, HttpStatus.OK.value(), "이미지 업로드 성공", new ImageUploadResponse(imageUrls)));
    }

    // --- 채팅 기록 조회 ---
    @GetMapping("/api/chat/history")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getChatHistory(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return ResponseEntity.ok(new ApiResponse<>(true, 200, "조회 성공", chatService.getChatHistory(user)));
    }
}
