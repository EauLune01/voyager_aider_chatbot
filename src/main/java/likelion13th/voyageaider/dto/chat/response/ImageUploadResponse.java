package likelion13th.voyageaider.dto.chat.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadResponse {
    private List<String> imageUrls; // 업로드 성공한 S3 URL 목록
}