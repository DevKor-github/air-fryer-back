package com.airfryer.repicka.domain.post.dto;

import com.airfryer.repicka.domain.item.dto.CreateItemReq;
import com.airfryer.repicka.domain.post.entity.PostType;
import lombok.Data;

@Data
public class CreatePostReq {
    private CreateItemReq item;
    private PostType postType;
    private int price;
    private int deposit = 0;
    // TODO: S3 연결 이후 프론트에서 이미지를 받는 형식으로 변경
    // question: 임의로 이미지 개수 10개 설정
    private String[] images = new String[10];
}
