package likelion13th.voyageaider.exception;

import jakarta.persistence.EntityNotFoundException;
import likelion13th.voyageaider.dto.global.ApiResponse;
import likelion13th.voyageaider.exception.auth.InvalidTokenException;
import likelion13th.voyageaider.exception.auth.TokenNotFoundException;
import likelion13th.voyageaider.exception.auth.UnauthorizedException;
import likelion13th.voyageaider.exception.image.ImageNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.IOException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 유효성 검증 실패 (DTO Validation)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiResponse<?>> handleValidationExceptions(MethodArgumentNotValidException e) {
        // 모든 필드 에러 메시지를 문자열로 합치기
        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", ")); // 쉼표로 구분

        return buildErrorResponse(HttpStatus.BAD_REQUEST, errorMessage);
    }

    //UnauthorizedException
    @ExceptionHandler(UnauthorizedException.class)
    protected ResponseEntity<ApiResponse<?>> handleUnauthorizedException(UnauthorizedException e){
        return buildErrorResponse(HttpStatus.UNAUTHORIZED,e.getMessage());
    }

    //InvalidTokenException
    @ExceptionHandler(InvalidTokenException.class)
    protected ResponseEntity<ApiResponse<?>> handleInvalidTokenException(InvalidTokenException e) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    //TokenNotFoundException
    @ExceptionHandler(TokenNotFoundException.class)
    protected ResponseEntity<ApiResponse<?>> handleTokenNotFoundException(TokenNotFoundException e) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    // 데이터 조회 실패
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleEntityNotFoundException(EntityNotFoundException e) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    // 파일 업로드 실패
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ApiResponse<?>> handleIOException(IOException e) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "파일 처리 중 오류가 발생했습니다.");
    }

    //업로드 파일 크기 초과
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handle(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(413)
                .body(new ApiResponse<>(false, 413, "업로드 가능한 최대 파일 크기를 초과했습니다.", null));
    }

    //이미지 누락
    @ExceptionHandler(ImageNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> NoImageException(ImageNotFoundException e){
        return buildErrorResponse(HttpStatus.NOT_FOUND,e.getMessage());
    }

    // 기타 모든 예외 처리 (500 Internal Server Error)
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponse<?>> handleException(Exception e) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다: " + e.getMessage());
    }

    private ResponseEntity<ApiResponse<?>> buildErrorResponse(HttpStatus status, String message) {
        ApiResponse<?> response = new ApiResponse<>(false, status.value(), message);
        return ResponseEntity.status(status).body(response);
    }

    private <T> ResponseEntity<ApiResponse<T>> buildErrorResponse(HttpStatus status, String message, T data) {
        ApiResponse<T> response = new ApiResponse<>(false, status.value(), message, data);
        return ResponseEntity.status(status).body(response);
    }
}