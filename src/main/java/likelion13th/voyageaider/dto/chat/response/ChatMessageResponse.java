package likelion13th.voyageaider.dto.chat.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    private String senderName; // "AI" or "유저이름"
    private String content;

    public static ChatMessageResponse of(String senderName, String content) {
        return new ChatMessageResponse(senderName, content);
    }
}
