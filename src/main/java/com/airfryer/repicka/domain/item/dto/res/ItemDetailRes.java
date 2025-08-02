package com.airfryer.repicka.domain.item.dto.res;

import com.airfryer.repicka.domain.item.dto.BaseItemDto;
import com.airfryer.repicka.domain.item.entity.Item;
import com.airfryer.repicka.domain.user.dto.BaseUserDto;
import com.airfryer.repicka.domain.user.entity.User;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ItemDetailRes
{
    private Long itemId;            // 제품 ID
    private BaseUserDto owner;      // 게시글 올린 사용자 정보
    private BaseItemDto itemInfo;   // 상품 정보

    public static ItemDetailRes from(Item item, List<String> imageUrls, User currentUser, boolean isLiked)
    {
        boolean isMine = (currentUser != null) && (currentUser.getId().equals(item.getOwner().getId()));

        return ItemDetailRes.builder()
                .itemId(item.getId())
                .owner(BaseUserDto.from(item.getOwner()))
                .itemInfo(BaseItemDto.of(item, imageUrls, isMine, isLiked))
                .build();
    }
}
