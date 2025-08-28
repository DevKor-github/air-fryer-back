package com.airfryer.repicka.domain.item_image;

import com.airfryer.repicka.common.aws.s3.S3Service;
import com.airfryer.repicka.domain.item.entity.Item;
import com.airfryer.repicka.domain.item_image.entity.ItemImage;
import com.airfryer.repicka.domain.item_image.repository.ItemImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemImageService
{
    private final ItemImageRepository itemImageRepository;
    private final S3Service s3Service;

    /// 여러 제품 이미지 저장

    @Transactional
    public List<String> createItemImage(String[] fileKeys, Item item)
    {
        // 제품 이미지 리스트 생성
        List<ItemImage> itemImages = new ArrayList<>();
        for(int i = 0; i < fileKeys.length; i++)
        {
            ItemImage itemImage = ItemImage.builder()
                    .displayOrder(i + 1)
                    .item(item)
                    .fileKey(fileKeys[i])
                    .build();

            itemImages.add(itemImage);
        }

        // 제품 이미지 리스트 저장
        itemImages = itemImageRepository.saveAll(itemImages);

        // 이미지 리스트 반환
        return getItemImages(itemImages);
    }

    /// 제품 이미지 리스트 반환

    public List<String> getItemImages(Item item)
    {
        List<ItemImage> itemImages = itemImageRepository.findAllByItemId(item.getId());
        return getItemImages(itemImages);
    }

    public List<String> getItemImages(List<ItemImage> itemImages) {
        return itemImages.stream()
                .map(ItemImage::getFileKey)
                .toList();
    }

    /// 제품의 썸네일 조회

    public String getThumbnail(Item item)
    {
        if(item == null) {
            return null;
        }

        Optional<ItemImage> itemImageOptional = itemImageRepository.findByDisplayOrderAndItemId(1, item.getId());
        ItemImage itemImage = itemImageOptional.orElse(null);
        return itemImage != null ? itemImage.getFileKey() : null;
    }

    /// 여러 제품의 썸네일 조회

    public Map<Long, String> getThumbnailsForItems(List<Item> items) {
        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .collect(Collectors.toList());
        
        List<ItemImage> thumbnails = itemImageRepository.findThumbnailListByItemIdList(itemIds);
        
        return thumbnails.stream()
                .collect(Collectors.toMap(
                        itemImage -> itemImage.getItem().getId(),
                        ItemImage::getFileKey
                ));
    }

    /// 제품 이미지 삭제
    @Transactional
    public void deleteItemImage(Long itemId) {
        itemImageRepository.deleteAllByItemId(itemId);
    }

    /// 제품 이미지의 fileKey를 전체 URL로 변환

    public String getFullImageUrl(ItemImage itemImage) {
        if (itemImage == null) {
            return null;
        }
        return s3Service.getFullImageUrl(itemImage.getFileKey());
    }
}
