package com.airfryer.repicka.domain.item;

import com.airfryer.repicka.domain.item.dto.CreateItemReq;
import com.airfryer.repicka.domain.item.entity.*;
import com.airfryer.repicka.domain.item.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    @Transactional
    public Item createItem(CreateItemReq itemDetail) {
        Item item = Item.builder()
                .productTypes(itemDetail.getProductTypes())
                .size(itemDetail.getSize())
                .title(itemDetail.getTitle().trim())
                .description(itemDetail.getDescription().trim())
                .color(itemDetail.getColor())
                .quality(itemDetail.getQuality())
                .location(itemDetail.getLocation().trim())
                .tradeMethod(itemDetail.getTradeMethod())
                .canDeal(itemDetail.getCanDeal())
                .saleDate(null)
                .repostDate(LocalDateTime.now())
                .build();

        item = itemRepository.save(item);

        return item;
    }

}
