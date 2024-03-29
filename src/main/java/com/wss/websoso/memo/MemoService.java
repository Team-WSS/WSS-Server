package com.wss.websoso.memo;

import com.wss.websoso.memo.dto.MemoCreateRequest;
import com.wss.websoso.memo.dto.MemoCreateResponse;
import com.wss.websoso.memo.dto.MemoDetailGetResponse;
import com.wss.websoso.memo.dto.MemoUpdateRequest;
import com.wss.websoso.memo.dto.MemosGetResponse;
import com.wss.websoso.user.User;
import com.wss.websoso.user.UserRepository;
import com.wss.websoso.userAvatar.UserAvatarRepository;
import com.wss.websoso.userNovel.UserNovel;
import com.wss.websoso.userNovel.UserNovelRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemoService {

    private static final int DEFAULT_PAGE_NUMBER = 0;
    private static final long SECOND_AVATAR_UNLOCK_CONDITION_MEMO_COUNT = 1L;
    private static final long THIRD_AVATAR_UNLOCK_CONDITION_MEMO_COUNT = 10L;
    private static final long SECOND_AVATAR_ID = 2L;
    private static final long THIRD_AVATAR_ID = 3L;
    private static final long MAX_MEMO_CONTENT_LENGTH = 2000L;

    private final MemoRepository memoRepository;
    private final UserRepository userRepository;
    private final UserNovelRepository userNovelRepository;
    private final UserAvatarRepository userAvatarRepository;

    @Transactional
    public MemoCreateResponse createMemo(Long userId, Long userNovelId, MemoCreateRequest memoCreateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 사용자가 없습니다."));

        UserNovel userNovel = userNovelRepository.findById(userNovelId)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 작품이 서재에 없습니다."));

        if (userNovel.getUser() != user) {
            throw new IllegalArgumentException("내 서재의 작품이 아닙니다.");
        }

        if (memoCreateRequest.memoContent().length() > MAX_MEMO_CONTENT_LENGTH) {
            throw new IllegalArgumentException("memoContent의 최대 길이를 초과했습니다.");
        }

        memoRepository.save(Memo.builder()
                .memoContent(memoCreateRequest.memoContent())
                .userNovel(userNovel)
                .build());

        user.updateUserWrittenMemoCount();
        Long userWrittenMemoCount = user.getUserWrittenMemoCount();
        if (userWrittenMemoCount == SECOND_AVATAR_UNLOCK_CONDITION_MEMO_COUNT) {
            userAvatarRepository.createUserAvatar(userId, SECOND_AVATAR_ID);
        }
        if (userWrittenMemoCount == THIRD_AVATAR_UNLOCK_CONDITION_MEMO_COUNT) {
            userAvatarRepository.createUserAvatar(userId, THIRD_AVATAR_ID);
        }
        boolean isAvatarUnlocked = userWrittenMemoCount == SECOND_AVATAR_UNLOCK_CONDITION_MEMO_COUNT
                || userWrittenMemoCount == THIRD_AVATAR_UNLOCK_CONDITION_MEMO_COUNT;
        return MemoCreateResponse.of(isAvatarUnlocked);
    }

    public MemosGetResponse getMemos(Long userId, Long lastMemoId, int size, String sortType) {
        PageRequest pageRequest = PageRequest.of(DEFAULT_PAGE_NUMBER, size);
        long memoCount = memoRepository.countByUserId(userId);
        if (Objects.equals(sortType, "NEWEST")) {
            Slice<Memo> entitySlice = memoRepository.findMemosByNewest(userId, lastMemoId, pageRequest);
            List<Memo> memos = entitySlice.getContent();
            return MemosGetResponse.of(memoCount, memos);
        } else {
            Slice<Memo> entitySlice = memoRepository.findMemosByOldest(userId, lastMemoId, pageRequest);
            List<Memo> memos = entitySlice.getContent();
            return MemosGetResponse.of(memoCount, memos);
        }
    }

    public MemoDetailGetResponse getMemo(Long memoId, Long userId) {
        Memo memo = memoRepository.findById(memoId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 메모입니다."));

        if (memo.getUserNovel().getUser().getUserId() != userId) {
            throw new IllegalArgumentException("사용자의 메모가 아닙니다.");
        }

        return MemoDetailGetResponse.of(memo);
    }

    @Transactional
    public void deleteMemo(Long userId, Long memoId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 사용자가 없습니다."));

        Memo memo = memoRepository.findById(memoId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 메모입니다."));

        if (memo.getUserNovel().getUser() != user) {
            throw new IllegalArgumentException("사용자의 메모가 아닙니다.");
        }
        memoRepository.delete(memo);
    }

    @Transactional
    public void editMemo(Long userId, Long memoId, MemoUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 사용자가 없습니다."));

        Memo memo = memoRepository.findById(memoId)
                .orElseThrow(() -> new EntityNotFoundException("해당하는 메모가 없습니다."));

        if (memo.getUserNovel().getUser() != user) {
            throw new IllegalArgumentException("사용자의 메모가 아닙니다.");
        }

        if (request.memoContent().length() > MAX_MEMO_CONTENT_LENGTH) {
            throw new IllegalArgumentException("memoContent의 최대 길이를 초과했습니다.");
        }

        memo.updateContent(request.memoContent());
    }
}
