package likelion13th.voyageaider.dto.chat.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class ChatMessageRequest {

    @NotBlank(message = "메시지 내용은 필수입니다.")
    private String content; // 텍스트 메시지 (필수)

    // (추가) 사용자가 첨부한 이미지 URL 목록 (선택 사항, 최대 3개)
    @Size(max = 3, message = "이미지는 최대 3개까지 전송할 수 있습니다.")
    private List<String> imageUrls;
}
