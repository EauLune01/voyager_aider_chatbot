package likelion13th.voyageaider.service.AI;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AIService {

    // application.yml의 OpenAI 설정을 기반으로 자동 주입됨
    private final ChatClient chatClient;

    // 1. AI의 정체성을 정해주는 '시스템 프롬프트'
    // (여기서 '프롬프트 엔지니어링'이 일어납니다)
    private final String SYSTEM_PROMPT = """
        당신은 'VoyageAider'라는 이름을 가진 친절한 여행 전문 챗봇입니다.
        사용자의 여행 관련 질문에 대해 구체적이고 유용한 답변을 제공해야 합니다.
        여행과 관련 없는 질문 (예: 정치, 연예)에는
        "저는 여행 전문 챗봇이라 그 질문에는 답변하기 어려워요."라고 정중하게 거절하세요.
        """;

    /**
     * 사용자 메시지를 받아 AI의 응답을 반환
     * @param userMessage 사용자가 보낸 채팅 내용
     * @return AI가 생성한 응답 내용
     */
    public String getAiResponse(String userMessage) {

        // 2. Spring AI의 Prompt 객체 생성
        Prompt prompt = new Prompt(
                List.of(
                        new SystemMessage(SYSTEM_PROMPT), // 시스템 프롬프트 (역할 부여)
                        new UserMessage(userMessage)      // 사용자 메시지
                )
        );

        // 3. AI API 호출 및 텍스트 응답 반환
        return chatClient.prompt() // 1. prompt()로 시작
                .system(SYSTEM_PROMPT) // 2. 시스템 프롬프트 설정
                .user(userMessage)     // 3. 유저 메시지 설정
                .call()                // 4. API 호출
                .content();
    }
}