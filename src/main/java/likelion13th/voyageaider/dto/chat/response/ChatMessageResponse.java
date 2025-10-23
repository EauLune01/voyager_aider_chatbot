package likelion13th.voyageaider.dto.chat.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import likelion13th.voyageaider.domain.ChatMessage;
import likelion13th.voyageaider.domain.ChatMessageImage;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageResponse {

    private String senderName; // "AI" or "유저이름"
    private String content;    // 텍스트 메시지
    @JsonInclude(JsonInclude.Include.NON_NULL) // 응답에서 null이면 필드 생략
    private List<String> imageUrls; // 메시지에 포함된 이미지 URL 목록

    public static ChatMessageResponse fromEntity(ChatMessage message, String senderName) {
        // ChatMessageImage 리스트에서 URL만 추출
        List<String> urls = message.getImages().stream()
                .map(ChatMessageImage::getUrl)
                .collect(Collectors.toList());

        return ChatMessageResponse.builder()
                .senderName(senderName)
                .content(message.getContent())
                .imageUrls(urls) // 이미지 URL 목록 포함
                .build();
    }
}
