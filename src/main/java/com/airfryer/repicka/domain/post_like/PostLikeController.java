package com.airfryer.repicka.domain.post_like;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.airfryer.repicka.domain.post_like.dto.PostLikeRes;
import com.airfryer.repicka.common.response.SuccessResponseDto;
import com.airfryer.repicka.common.security.oauth2.CustomOAuth2User;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/like")
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;

    @PostMapping("/{postId}")
    public ResponseEntity<SuccessResponseDto> likePost(@PathVariable(value="postId") Long postId,
                                                        @AuthenticationPrincipal CustomOAuth2User user) {
        boolean isLiked = postLikeService.likePost(postId, user.getUser());

        return ResponseEntity.ok(SuccessResponseDto.builder()
            .message(isLiked ? "게시글 좋아요를 성공적으로 추가했습니다." : "게시글 좋아요를 성공적으로 취소했습니다.")
            .build());
    }

    @GetMapping
    public ResponseEntity<SuccessResponseDto> getPostLikes(@AuthenticationPrincipal CustomOAuth2User user) {

        List<PostLikeRes> postLikes = postLikeService.getPostLikes(user.getUser());
        return ResponseEntity.ok(SuccessResponseDto.builder()
            .message("게시글 좋아요 목록을 성공적으로 조회했습니다.")
            .data(postLikes)
            .build());
    }
}
