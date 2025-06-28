package com.airfryer.repicka.domain.item_image;

import com.airfryer.repicka.domain.item.entity.Item;
import com.airfryer.repicka.domain.item_image.entity.ItemImage;
import com.airfryer.repicka.domain.item_image.repository.ItemImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemImageService {
    private final ItemImageRepository itemImageRepository;

    // 다수의 상품 이미지 저장
    @Transactional
    public List<ItemImage> createItemImage(String[] urls, Item item) {
        List<ItemImage> itemImages = new ArrayList<>();
        // TODO: image to url로 변환을 여기에서 처리하거나 s3 서비스에서 처리
        int order = 1;
        for (String url: urls) {
            ItemImage itemImage = ItemImage.builder()
                    .displayOrder(order++)
                    .item(item)
                    .imageUrl(url)
                    .build();

            itemImages.add(itemImage);
        }

        itemImages = itemImageRepository.saveAll(itemImages);

        return itemImages;
    }

    public ItemImage getThumbnail(Item item) {
        Optional<ItemImage> itemImageOptional = itemImageRepository.findByDisplayOrderAndItemId(1, item.getId());
        return itemImageOptional.orElse(null);
    }

    public List<ItemImage> getItemImages(Item item) {
        return itemImageRepository.findAllByItemId(item.getId());
    }
}
