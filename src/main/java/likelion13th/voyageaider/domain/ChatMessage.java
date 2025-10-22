package likelion13th.voyageaider.domain;

import jakarta.persistence.*;
import likelion13th.voyageaider.dto.chat.request.ChatMessageRequest;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // AI가 보낸 메시지든, 사용자가 보낸 메시지든 "누구와의 대화"인지를 나타내는 필드
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 2. (변경) 발신자 타입 (Enum)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Sender sender; // {USER, AI}

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Builder
    public ChatMessage(User user, Sender sender, String content) {
        this.user = user;
        this.sender = sender;
        this.content = content;
    }

    // --- Static Factory Methods ---

    /**
     * [사용자] 메시지 엔티티 생성
     */
    public static ChatMessage createUserMessage(User user, ChatMessageRequest request) {
        return ChatMessage.builder()
                .user(user)
                .sender(Sender.USER)
                .content(request.getContent())
                .build();
    }

    /**
     * [AI] 메시지 엔티티 생성
     */
    public static ChatMessage createAiMessage(User user, String aiContent) {
        return ChatMessage.builder()
                .user(user)
                .sender(Sender.AI)
                .content(aiContent)
                .build();
    }
}