package com.airfryer.repicka.domain.post.dto;

import com.airfryer.repicka.domain.item.dto.CreateItemReq;
import com.airfryer.repicka.domain.post.entity.PostType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CreatePostReq {
    @NotNull(message = "상품 내용을 입력해주세요.")
    @Valid
    private CreateItemReq item; // 상품 관련 정보 dto

    @NotNull(message = "게시글 타입을 입력해주세요.")
    private PostType[] postType = new PostType[2]; // 게시글 타입 배열 (대여, 판매 복수 설정 가능)

    @NotNull(message = "대여료 및 판매료를 입력해주세요.")
    @Min(value = 0, message = "대여료 또는 판매값은 0원 이상이어야 합니다.")
    private int price; // 대여료 및 판매값

    @Min(value = 0, message = "보증금은 0원 이상이어야 합니다.")
    private int deposit = 0; // 보증금

    // TODO: S3 연결 이후 프론트에서 이미지를 받는 형식으로 변경
    @NotNull(message = "이미지를 첨부해주세요.")
    @Size(min = 1, max = 6, message = "이미지는 최소 1개 이상, 최대 6개 이하이어야 합니다.")
    private String[] images = new String[6]; // 상품 이미지
}
