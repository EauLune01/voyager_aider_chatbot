package likelion13th.voyageaider.service.chat;
import likelion13th.voyageaider.domain.ChatMessage;
import likelion13th.voyageaider.domain.Sender;
import likelion13th.voyageaider.domain.User;
import likelion13th.voyageaider.dto.chat.request.ChatMessageRequest;
import likelion13th.voyageaider.dto.chat.response.ChatMessageResponse;
import likelion13th.voyageaider.repository.chat.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final String AI_SENDER_NAME = "AI";

    /**
     * [사용자] 메시지 (텍스트 + 선택적 이미지)를 DB에 저장
     */
    @Transactional
    public ChatMessage saveUserMessage(User user, ChatMessageRequest request) {
        // 1. 텍스트 메시지 엔티티 생성
        ChatMessage chatMessage = ChatMessage.createUserTextMessage(user, request.getContent());
        // 2.  DTO에 이미지 URL이 있으면 엔티티에 추가
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            chatMessage.addImages(request.getImageUrls());
        }
        // 3. DB에 저장
        return chatMessageRepository.save(chatMessage);
    }

    /**
     * [AI] 메시지 (텍스트 + 선택적 이미지 1개)를 DB에 저장
     */
    @Transactional
    public ChatMessage saveAiMessage(User user, String aiContent, String aiImageUrl) {
        // 1. 텍스트 메시지 엔티티 생성
        ChatMessage chatMessage = ChatMessage.createAiTextMessage(user, aiContent);
        // 2.  AI가 생성한 이미지 URL이 있으면 엔티티에 추가
        if (aiImageUrl != null && !aiImageUrl.isBlank()) {
            chatMessage.addImage(aiImageUrl);
        }
        // 3. DB에 저장
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