package likelion13th.voyageaider.config.AI;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.RedisVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

@Configuration
public class AIConfig {

    // 1. 대화 기억 저장소
    @Bean
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }

    // 2. Redis 연결 객체 생성
    @Bean
    public JedisPooled jedisPooled() {
        // Docker Redis 주소 (localhost:6379)
        return new JedisPooled("localhost", 6379);
    }

    // 3. 벡터 저장소 설정
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel, JedisPooled jedisPooled) {

        RedisVectorStore.RedisVectorStoreConfig config = RedisVectorStore.RedisVectorStoreConfig.builder()
                .withIndexName("voyage-idx")
                .withPrefix("doc:")
                .build();

        // 생성자: (설정, 임베딩모델, 연결객체, 스키마초기화여부)
        return new RedisVectorStore(config, embeddingModel, jedisPooled, true);
    }

    // 4. ChatClient 생성
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory, VectorStore vectorStore) {
        return builder
                .defaultSystem("당신은 'VoyageAider'라는 이름의 여행 전문 챗봇입니다. 질문에 답할 때, 내가 제공하는 여행 정보(Context)를 적극적으로 활용해서 구체적으로 답변하세요.")
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        new QuestionAnswerAdvisor(vectorStore, SearchRequest.defaults())
                )
                .build();
    }
}