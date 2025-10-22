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

    @Transactional
    public ChatMessage saveUserMessage(User user, ChatMessageRequest request) {
        ChatMessage chatMessage = ChatMessage.createUserMessage(user, request);
        return chatMessageRepository.save(chatMessage);
    }

    @Transactional
    public ChatMessage saveAiMessage(User user, String aiContent) {
        ChatMessage chatMessage = ChatMessage.createAiMessage(user, aiContent);
        return chatMessageRepository.save(chatMessage);
    }

    public List<ChatMessageResponse> getChatHistory(User user) {
        List<ChatMessage> history = chatMessageRepository.findAllByUserOrderByTimestampAsc(user);
        return history.stream()
                .map(message -> ChatMessageResponse.of(
                        message.getSender() == Sender.USER ? user.getName() : AI_SENDER_NAME,
                        message.getContent()
                ))
                .collect(Collectors.toList());
    }
}