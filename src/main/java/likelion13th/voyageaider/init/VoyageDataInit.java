package likelion13th.voyageaider.init;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class VoyageDataInit {

    private final VectorStore vectorStore;

    @Bean
    @Profile("!test") // 테스트 코드 돌릴 땐 실행 안 함
    public ApplicationRunner initializeData() {
        return args -> {
            log.info("🚀 [RAG] Redis 벡터 저장소에 여행 데이터 적재 시작...");

            // 1. GPT가 절대 모를 가상의 여행 정보 (테스트용)
            List<Document> documents = List.of(
                    new Document("제주도 서귀포시에는 '고등어 샌드위치'로 유명한 비밀 맛집 '해녀의 부엌 2호점'이 있습니다. 가격은 15,000원입니다."),
                    new Document("부산 해운대에는 밤 12시 이후에만 문을 여는 '유령 포장마차'가 있으며, 암호는 '갈매기'입니다."),
                    new Document("서울 성수동 '보이지 않는 카페'는 입구가 자판기 모양이며, 라떼를 시키면 점을 봐줍니다.")
            );

            // 2. Redis에 저장 (여기서 Vector로 변환돼서 들어감)
            vectorStore.add(documents);

            log.info("✅ [RAG] 데이터 적재 완료! 총 {}건", documents.size());
        };
    }
}