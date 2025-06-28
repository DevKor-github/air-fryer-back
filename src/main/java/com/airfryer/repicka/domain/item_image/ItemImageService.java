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
        ItemImage itemImage = itemImageRepository.findByDisplayOrderAndItemId(1, item.getId());
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
}
