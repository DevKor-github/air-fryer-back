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
@Transactional
public class ItemImageService {
    private final ItemImageRepository itemImageRepository;

    public List<ItemImage> saveItemImage(String[] urls, Item item) {
        List<ItemImage> itemImages = new ArrayList<>();

        for (String url: urls) {
            ItemImage itemImage = ItemImage.builder()
                    .item(item)
                    .imageUrl(url)
                    .build();

            itemImage = itemImageRepository.save(itemImage);
            itemImages.add(itemImage);
        }

        return  itemImages;
    }
}
