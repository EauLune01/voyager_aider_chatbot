package likelion13th.voyageaider.service.AI;

import likelion13th.voyageaider.dto.chat.response.AIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Service // ⭐ 핵심: 이게 있어야 ChatService에서 주입받을 수 있음!
@RequiredArgsConstructor
public class AIService {

    private final ChatClient chatClient; // AIConfig에서 설정한 ChatClient 주입

    /**
     * AI에게 질문을 보내고 응답을 받습니다.
     * @param userId 사용자 식별자 (대화 기억용)
     * @param userMessage 사용자 질문
     * @return AIResponse (응답 텍스트)
     */
    public AIResponse getAiResponse(String userId, String userMessage) {

        String response = chatClient.prompt()
                .user(userMessage)
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, userId) // 유저별로 대화방 분리
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10)      // 최근 10개 대화까지만 기억 (비용 절약)
                )
                .call()
                .content();

        return new AIResponse(response);
    }
}