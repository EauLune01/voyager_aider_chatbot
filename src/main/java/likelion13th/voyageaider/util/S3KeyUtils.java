package likelion13th.voyageaider.util;

import java.net.URI;

//S3 URL과 Key를 안전하게 다루는 유틸리티 클래스
public final class S3KeyUtils {
    private S3KeyUtils() {}

    // url이 이미 key 형태면 그대로 반환, S3 URL이면 key만 추출
    public static String toKey(String bucket, String urlOrKey) {
        if (urlOrKey == null || urlOrKey.isBlank()) return urlOrKey;
        //이미 key인 경우
        if (!urlOrKey.startsWith("http://") && !urlOrKey.startsWith("https://")) {
            return stripLeadingSlash(urlOrKey);
        } //URL인 경우
        try {
            URI uri = URI.create(urlOrKey);
            String host = uri.getHost(); // null 가능
            String rawPath = uri.getPath() == null ? "" : uri.getPath();
            String path = stripLeadingSlash(rawPath);

            // 1. virtual-hosted 스타일일 때
            if (host != null && host.startsWith(bucket + ".")) {
                return path;
            }
            // 2. Path-style 접근 방식
            if (path.startsWith(bucket + "/")) {
                return path.substring(bucket.length() + 1);  // path-style
            }
            //3. fallback(그 외)
            if (host != null && host.contains("amazonaws.com")) {
                return path;
            }
            return stripLeadingSlash(urlOrKey); //문자열이 /로 시작하면 제거
        } catch (IllegalArgumentException e) {
            // 잘못된 URL이면 원본을 key로 간주 (하위호환)
            return stripLeadingSlash(urlOrKey);
        }
    }

    // 문자열이 /로 시작하면 제거하는 헬퍼 메서드
    private static String stripLeadingSlash(String s) {
        if (s == null) return null;
        return s.startsWith("/") ? s.substring(1) : s;
    }

    // URL이나 Key에서 파일명만 추출
    public static String extractFileName(String urlOrKey) {
        //파일명이 비어있는 경우
        if (urlOrKey == null || urlOrKey.isBlank()) {
            return "download";
        }
        //URL이라면 URI로 파싱해 path만 남김
        String s = urlOrKey;
        if (s.startsWith("http://") || s.startsWith("https://")) {
            s = URI.create(s).getPath();
        }
        // / 기준 마지막 요소 잘라 반환
        int pos = s.lastIndexOf('/');
        return (pos >= 0 ? s.substring(pos + 1) : s);
    }
}
