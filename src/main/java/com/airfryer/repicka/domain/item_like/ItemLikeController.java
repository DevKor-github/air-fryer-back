package com.airfryer.repicka.domain.item_like;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.airfryer.repicka.domain.item_like.dto.ItemLikeRes;
import com.airfryer.repicka.common.response.SuccessResponseDto;
import com.airfryer.repicka.common.security.oauth2.CustomOAuth2User;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/like")
@RequiredArgsConstructor
public class ItemLikeController
{
    private final ItemLikeService itemLikeService;

    // 제품 좋아요
    @PostMapping("/{itemId}")
    public ResponseEntity<SuccessResponseDto> likeItem(@PathVariable(value="itemId") Long itemId,
                                                       @AuthenticationPrincipal CustomOAuth2User user)
    {
        boolean isLiked = itemLikeService.likeItem(itemId, user.getUser());

        return ResponseEntity.ok(SuccessResponseDto.builder()
            .message(isLiked ? "제품 좋아요를 성공적으로 추가했습니다." : "제품 좋아요를 성공적으로 취소했습니다.")
            .build());
    }

    // 좋아요 목록
    @GetMapping
    public ResponseEntity<SuccessResponseDto> getPostLikes(@AuthenticationPrincipal CustomOAuth2User user)
    {
        List<ItemLikeRes> postLikes = itemLikeService.getPostLikes(user.getUser());

        return ResponseEntity.ok(SuccessResponseDto.builder()
            .message("제품 좋아요 목록을 성공적으로 조회했습니다.")
            .data(postLikes)
            .build());
    }
}
