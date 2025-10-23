package likelion13th.voyageaider.service.S3;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import likelion13th.voyageaider.exception.image.ImageNotFoundException;
import likelion13th.voyageaider.util.S3KeyUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3UploaderService {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    // 여러 파일 업로드 (User 첨부용)
    public List<String> uploadImages(List<MultipartFile> files, String dirName) throws IOException {
        if (files == null || files.isEmpty()) {
            return List.of(); // 빈 리스트 반환
        }

        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue; // 비어있는 파일 건너뛰기

            // 파일명 생성 (UUID 사용)
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            }
            String key = dirName + "/" + UUID.randomUUID() + extension;

            // 메타데이터 설정
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            // S3 업로드
            try (InputStream inputStream = file.getInputStream()) {
                PutObjectRequest request = new PutObjectRequest(bucket, key, inputStream, metadata)
                        .withCannedAcl(CannedAccessControlList.PublicRead); // PublicRead ACL 설정
                amazonS3.putObject(request);
                urls.add(amazonS3.getUrl(bucket, key).toString());
                log.info("S3 Upload Success: Key={}, URL={}", key, urls.get(urls.size() - 1));
            } catch (AmazonServiceException e) {
                log.error("S3 Upload Error (AmazonServiceException): Key={}, Error={}", key, e.getMessage(), e);
                throw e; // 예외 다시 던지기
            } catch (SdkClientException e) {
                log.error("S3 Upload Error (SdkClientException): Key={}, Error={}", key, e.getMessage(), e);
                throw e; // 예외 다시 던지기
            }
        }
        return urls;
    }
}