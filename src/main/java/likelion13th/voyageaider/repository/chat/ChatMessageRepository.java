package likelion13th.voyageaider.repository.chat;

import likelion13th.voyageaider.domain.ChatMessage;
import likelion13th.voyageaider.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 생성 시간순으로 모든 채팅 정렬해서 가져오기
    List<ChatMessage> findAllByUserOrderByTimestampAsc(User user);
}
