package likelion13th.voyageaider.repository.chat;

import likelion13th.voyageaider.domain.ChatMessage;
import likelion13th.voyageaider.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // 특정 User의 모든 채팅 기록을 시간순으로 조회
    List<ChatMessage> findAllByUserOrderByTimestampAsc(User user);
}
