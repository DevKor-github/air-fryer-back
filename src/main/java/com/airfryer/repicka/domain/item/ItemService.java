package com.airfryer.repicka.domain.item;

import com.airfryer.repicka.domain.item.dto.CreateItemReq;
import com.airfryer.repicka.domain.item.entity.*;
import com.airfryer.repicka.domain.item.repository.ItemRepository;
import com.airfryer.repicka.common.exception.CustomException;
import com.airfryer.repicka.common.exception.CustomExceptionCode;

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

    @Transactional
    public Item updateItem(Long itemId, CreateItemReq itemDetail) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.ITEM_NOT_FOUND, itemId));

        item.updateItem(itemDetail);
        item = itemRepository.save(item);

        return item;
    }

}
