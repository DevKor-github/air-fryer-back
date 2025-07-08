package com.airfryer.repicka.domain.post.dto;

import com.airfryer.repicka.domain.item.dto.CreateItemReq;
import com.airfryer.repicka.domain.item.entity.PostType;
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
    private PostType[] postTypes = new PostType[2]; // 게시글 타입 배열 (대여, 판매 복수 설정 가능)

    @Min(value = 0, message = "대여료는 0원 이상이어야 합니다.")
    private int rentalFee; // 대여료

    @Min(value = 0, message = "판매요금은 0원 이상이어야 합니다.")
    private int salePrice; // 판매요금

    @Min(value = 0, message = "보증금은 0원 이상이어야 합니다.")
    private int deposit = 0; // 보증금

    @NotNull(message = "이미지 키를 첨부해주세요.")
    @Size(min = 1, max = 6, message = "이미지는 최소 1개 이상, 최대 6개 이하이어야 합니다.")
    private String[] images = new String[6]; // 상품 이미지
}
