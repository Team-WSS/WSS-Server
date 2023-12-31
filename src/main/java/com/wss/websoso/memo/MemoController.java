package com.wss.websoso.memo;

import java.net.URI;
import java.security.Principal;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/memos")
@RequiredArgsConstructor
public class MemoController {

    private final MemoService memoService;

    // 생성
    @PostMapping("{userNovelId}")
    public ResponseEntity<Map<String, String>> createMemo(
            @PathVariable Long userNovelId,
            @RequestBody MemoCreateRequest request,
            Principal principal
    ) {
        try {
            URI location = URI.create(memoService.create(Long.valueOf(principal.getName()), userNovelId, request));
            return ResponseEntity.created(location).build();
        } catch (IllegalArgumentException e) {
            if ("내 서재의 작품이 아닙니다.".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "내 서재의 작품이 아닙니다."));
            } else if ("memoContent의 최대 길이를 초과했습니다.".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "memoContent의 최대 길이를 초과했습니다."));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("message", "예상치 못한 오류가 발생했습니다."));
            }
        }
    }

    // 삭제
    @DeleteMapping("{memoId}")
    public ResponseEntity deleteMemo(@PathVariable Long memoId, Principal principal) {
        try {
            memoService.deleteMemo(Long.valueOf(principal.getName()), memoId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            if ("사용자의 메모가 아닙니다.".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "사용자의 메모가 아닙니다."));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("message", "예상치 못한 오류가 발생했습니다."));
            }
        }
    }

    // 수정
    @PatchMapping("{memoId}")
    public ResponseEntity<Map<String, String>> editMemo(
            @PathVariable Long memoId,
            @RequestBody MemoUpdateRequest request,
            Principal principal
    ) {
        try {
            memoService.editMemo(Long.valueOf(principal.getName()), memoId, request);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            if ("사용자의 메모가 아닙니다.".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "사용자의 메모가 아닙니다."));
            } else if ("memoContent의 최대 길이를 초과했습니다.".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "memoContent의 최대 길이를 초과했습니다."));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("message", "예상치 못한 오류가 발생했습니다."));
            }
        }
    }
}