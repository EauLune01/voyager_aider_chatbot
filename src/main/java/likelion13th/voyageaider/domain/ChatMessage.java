package likelion13th.voyageaider.domain;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Sender sender;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Builder.Default
    @OneToMany(mappedBy = "chatMessage", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ChatMessageImage> images = new ArrayList<>();

    public static ChatMessage createUserTextMessage(User user, String textContent) {
        return ChatMessage.builder()
                .user(user)
                .sender(Sender.USER)
                .content(textContent)
                .build();
    }

    public static ChatMessage createAiTextMessage(User user, String aiContent) {
        return ChatMessage.builder()
                .user(user)
                .sender(Sender.AI)
                .content(aiContent)
                .build();
    }

    // --- 이미지 추가 편의 메서드  ---
    public void addImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return;
        ChatMessageImage image = ChatMessageImage.builder()
                .chatMessage(this)
                .url(imageUrl)
                .build();
        this.images.add(image);
    }

    public void addImages(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) return;
        imageUrls.stream()
                .filter(url -> url != null && !url.isBlank())
                .forEach(this::addImage);
    }
}