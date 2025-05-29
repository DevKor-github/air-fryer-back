package com.airfryer.repicka.domain.item_image;

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

    // 다수의 상품 이미지 저장
    @Transactional
    public List<ItemImage> saveItemImage(String[] urls, Item item) {
        List<ItemImage> itemImages = new ArrayList<>();
        // TODO: image to url로 변환을 여기에서 처리하거나 s3 서비스에서 처리
        for (String url: urls) {
            ItemImage itemImage = ItemImage.builder()
                    .item(item)
                    .imageUrl(url)
                    .build();

            itemImages.add(itemImage);
        }

        itemImages = itemImageRepository.saveAll(itemImages);

        return itemImages;
    }
}
