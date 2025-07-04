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
public class ItemImageService {
    private final ItemImageRepository itemImageRepository;
    private final S3Service s3Service;

    // 다수의 상품 이미지 저장
    @Transactional
    public List<ItemImage> createItemImage(String[] fileKeys, Item item) {
        List<ItemImage> itemImages = new ArrayList<>();
        int order = 1;
        for (String fileKey: fileKeys) {
            ItemImage itemImage = ItemImage.builder()
                    .displayOrder(order++)
                    .item(item)
                    .fileKey(fileKey)
                    .build();

            itemImages.add(itemImage);
        }

        itemImages = itemImageRepository.saveAll(itemImages);

        return itemImages;
    }

    public String getThumbnail(Item item) {
        Optional<ItemImage> itemImageOptional = itemImageRepository.findByDisplayOrderAndItemId(1, item.getId());
        ItemImage itemImage = itemImageOptional.orElse(null);
        return getFullImageUrl(itemImage);
    }

    public List<String> getItemImages(Item item) {
        List<ItemImage> itemImages = itemImageRepository.findAllByItemId(item.getId());
        List<String> imageUrls = itemImages.stream()
                .map(this::getFullImageUrl)
                .toList();
        return imageUrls;
    }
    
    // ItemImage 엔티티의 fileKey를 전체 URL로 변환
    public String getFullImageUrl(ItemImage itemImage) {
        if (itemImage == null) {
            return null;
        }
        return s3Service.getFullImageUrl(itemImage.getFileKey());
    }

    // 여러 Item의 썸네일을 한 번에 조회
    public Map<Long, String> getThumbnailsForItems(List<Item> items) {
        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .collect(Collectors.toList());
        
        List<ItemImage> thumbnails = itemImageRepository.findThumbnailListByItemIdList(itemIds);
        
        return thumbnails.stream()
                .collect(Collectors.toMap(
                    itemImage -> itemImage.getItem().getId(),
                    itemImage -> getFullImageUrl(itemImage)
                ));
    }
}
