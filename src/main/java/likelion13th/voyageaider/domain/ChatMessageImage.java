package likelion13th.voyageaider.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class ChatMessageImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ChatMessage와 다대일 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_message_id", nullable = false)
    private ChatMessage chatMessage;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String url; // S3 이미지 URL

    @Builder
    public ChatMessageImage(ChatMessage chatMessage, String url) {
        this.chatMessage = chatMessage;
        this.url = url;
    }
}
