package likelion13th.voyageaider.dto.chat.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatMessageRequest {
    @NotBlank(message = "메시지 내용은 필수입니다.")
    private String content;
}
