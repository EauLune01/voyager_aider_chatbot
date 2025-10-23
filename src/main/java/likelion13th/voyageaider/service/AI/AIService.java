package likelion13th.voyageaider.service.AI;

import likelion13th.voyageaider.dto.chat.response.AIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AIService {

    private final ChatClient chatClient;   // ⬅️ 텍스트만 사용

    private static final String SYSTEM_PROMPT = """
        당신은 'VoyageAider'라는 이름의 여행 전문 챗봇입니다.
        - 여행 관련 질문에 구체적이고 실용적인 답을 제공합니다.
        - 정치/연예 등 여행과 무관한 질문은 정중히 거절합니다.
        """;

    public AIResponse getAiResponse(String userMessage) {
        final String text = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(userMessage)
                .call()
                .content();
        return new AIResponse(text);
    }
}