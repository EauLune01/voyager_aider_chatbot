package likelion13th.voyageaider.config.AI;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {

    /**
     * Spring Boot가 자동으로 생성/설정한 ChatClient.Builder를 주입받아
     * 최종 ChatClient Bean을 생성합니다.
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {

        return builder
                .defaultOptions( // ⬅️ gpt-4o-mini 등 기본 옵션 설정
                        OpenAiChatOptions.builder()
                                .model("gpt-4o-mini")
                                .temperature(0.7)
                                .maxTokens(1024)
                                .build()
                )
                .build();
    }
}