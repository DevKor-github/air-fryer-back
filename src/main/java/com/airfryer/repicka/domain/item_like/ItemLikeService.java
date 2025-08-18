package com.airfryer.repicka.domain.item_like;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.airfryer.repicka.domain.item.repository.ItemRepository;
import com.airfryer.repicka.domain.item_like.repository.ItemLikeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.airfryer.repicka.domain.item.dto.ItemPreviewDto;
import com.airfryer.repicka.domain.item_like.entity.ItemLike;
import com.airfryer.repicka.domain.item_image.ItemImageService;
import com.airfryer.repicka.domain.item.entity.Item;
import com.airfryer.repicka.domain.user.entity.user.User;
import com.airfryer.repicka.common.exception.CustomException;
import com.airfryer.repicka.common.exception.CustomExceptionCode;

@Service
@RequiredArgsConstructor
public class ItemLikeService
{
    private final ItemRepository itemRepository;
    private final ItemLikeRepository itemLikeRepository;
    private final ItemImageService itemImageService;

    // 제품 좋아요 등록 및 취소
    @Transactional
    public boolean likeItem(Long itemId, User user)
    {
        // 제품 조회
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.ITEM_NOT_FOUND, itemId));

        // 제품 삭제 여부 확인
        if(item.getIsDeleted()) {
            throw new CustomException(CustomExceptionCode.ALREADY_DELETED_ITEM, null);
        }

        // 기존의 제품 좋아요 데이터 조회
        Optional<ItemLike> existingLike = itemLikeRepository.findByItemIdAndLikerId(itemId, user.getId());

        // 제품 좋아요 데이터가 존재한다면, 좋아요 취소
        // 제품 좋아요 데이터가 존재하지 않는다면, 좋아요
        if(existingLike.isPresent())
        {
            // 이미 좋아요를 눌렀으면 좋아요 취소
            itemLikeRepository.delete(existingLike.get());

            // 좋아요 취소 시 좋아요 수 감소
            item.removeLikeCount();

            return false;
        }
        else
        {
            // 좋아요를 누르지 않았으면 좋아요 추가
            ItemLike itemLike = ItemLike.builder()
                .item(item)
                .liker(user)
                .build();
            
            itemLikeRepository.save(itemLike);

            // 좋아요 추가 시 좋아요 수 증가
            item.addLikeCount();

            return true;
        }
    }

    // 좋아요 목록
    @Transactional(readOnly = true)
    public List<ItemPreviewDto> getPostLikes(User user)
    {
        // 좋아요 리스트 조회
        List<ItemLike> itemLikes = itemLikeRepository.findByLikerId(user.getId());

        // 좋아요를 누른 제품 리스트 조회
        List<Item> items = itemLikes.stream()
            .map(ItemLike::getItem)
            .filter(item -> !item.getIsDeleted())
            .toList();

        // 모든 제품의 썸네일 배치
        Map<Long, String> thumbnailMap = itemImageService.getThumbnailsForItems(items);

        return items.stream()
            .map(item -> ItemPreviewDto.from(item, thumbnailMap.get(item.getId())))
            .toList();
    }
}
