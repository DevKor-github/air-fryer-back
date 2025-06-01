package com.airfryer.repicka.domain.post;

import com.airfryer.repicka.common.response.SuccessResponseDto;
import com.airfryer.repicka.common.security.oauth2.CustomOAuth2User;
import com.airfryer.repicka.domain.post.dto.CreatePostReq;
import com.airfryer.repicka.domain.post.dto.PostDetailRes;
import com.airfryer.repicka.domain.post.dto.PostPreviewRes;
import com.airfryer.repicka.domain.post.dto.SearchPostReq;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/post")
public class PostController {
    private final PostService postService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> createPost(@AuthenticationPrincipal CustomOAuth2User user,
                                                         @Valid @RequestBody CreatePostReq req) {
        List<PostDetailRes> postDetailResList = postService.createPostWithItemAndImages(req, user.getUser());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponseDto.builder()
                        .message("게시글을 성공적으로 생성하였습니다.")
                        .data(postDetailResList)
                        .build());
    }

    @GetMapping("/{postId}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<SuccessResponseDto> getPostDetail(@PathVariable Long postId) {
        PostDetailRes postDetailRes = postService.getPostDetail(postId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("게시글 상세 내용을 성공적으로 조회하였습니다.")
                        .data(postDetailRes)
                        .build());
    }

    @GetMapping("/search")
    @PreAuthorize("permitAll()")
    public ResponseEntity<SuccessResponseDto> searchPostList(@Valid @RequestBody SearchPostReq req) {
        List<PostPreviewRes> postPreviewResList = postService.searchPostList(req);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("조건에 따른 게시글 목록을 성공적으로 조회하였습니다.")
                        .data(postPreviewResList)
                        .build());
    }

}
