package likelion13th.voyageaider.service.chat;

import likelion13th.voyageaider.domain.ChatMessage;
import likelion13th.voyageaider.domain.Sender;
import likelion13th.voyageaider.domain.User;
import likelion13th.voyageaider.dto.chat.request.ChatMessageRequest;
import likelion13th.voyageaider.dto.chat.response.AIResponse;
import likelion13th.voyageaider.dto.chat.response.ChatMessageResponse;
import likelion13th.voyageaider.repository.chat.ChatMessageRepository;
import likelion13th.voyageaider.service.AI.AIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final AIService aiService;                     // AI 기능 추가
    private final SimpMessagingTemplate messagingTemplate; // 소켓 기능 추가

    private final String AI_SENDER_NAME = "AI";
    private static final String CHAT_DESTINATION = "/queue/messages";

    /**
     * 메시지 처리의 전체 흐름을 담당 (Facade 패턴)
     */
    @Transactional
    public void processMessage(User user, ChatMessageRequest request) {
        // 1. [User] 메시지 저장
        ChatMessage userMessage = saveUserMessage(user, request);

        // 2. [User] 메시지 소켓 전송 (화면에 즉시 표시)
        sendToUser(user, ChatMessageResponse.fromEntity(userMessage, user.getName()));

        // 3. [AI] 응답 생성
        AIResponse aiResponse;
        try {
            // AIService 호출 (username을 식별자로 사용하여 기억력 활용)
            aiResponse = aiService.getAiResponse(user.getUsername(), request.getContent());
        } catch (Exception e) {
            log.error("AI 응답 생성 중 오류 발생: ", e);
            aiResponse = new AIResponse("죄송해요, 잠시 문제가 생겼어요. 다시 시도해 주세요.");
        }

        // 4. [AI] 메시지 저장 (기존 메서드 활용)
        ChatMessage aiMessage = saveAiMessage(user, aiResponse.getContent(), null);

        // 5. [AI] 메시지 소켓 전송 (답변 표시)
        sendToUser(user, ChatMessageResponse.fromEntity(aiMessage, AI_SENDER_NAME));
    }

    /**
     * [내부 로직] 사용자에게 WebSocket 메시지 전송
     */
    private void sendToUser(User user, ChatMessageResponse response) {
        messagingTemplate.convertAndSendToUser(
                user.getUsername(),
                CHAT_DESTINATION,
                response
        );
    }

    /**
     * [사용자] 메시지 (텍스트 + 선택적 이미지)를 DB에 저장
     */
    @Transactional
    public ChatMessage saveUserMessage(User user, ChatMessageRequest request) {
        ChatMessage chatMessage = ChatMessage.createUserTextMessage(user, request.getContent());
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            chatMessage.addImages(request.getImageUrls());
        }
        return chatMessageRepository.save(chatMessage);
    }

    /**
     * [AI] 메시지 (텍스트 + 선택적 이미지 1개)를 DB에 저장
     */
    @Transactional
    public ChatMessage saveAiMessage(User user, String aiContent, String aiImageUrl) {
        ChatMessage chatMessage = ChatMessage.createAiTextMessage(user, aiContent);
        if (aiImageUrl != null && !aiImageUrl.isBlank()) {
            chatMessage.addImage(aiImageUrl);
        }
        return chatMessageRepository.save(chatMessage);
    }

    /**
     * 특정 유저의 전체 채팅 기록 불러오기
     */
    public List<ChatMessageResponse> getChatHistory(User user) {
        List<ChatMessage> history = chatMessageRepository.findAllByUserOrderByTimestampAsc(user);
        return history.stream()
                .map(message -> ChatMessageResponse.fromEntity(
                        message,
                        message.getSender() == Sender.USER ? user.getName() : AI_SENDER_NAME
                ))
                .collect(Collectors.toList());
    }
}