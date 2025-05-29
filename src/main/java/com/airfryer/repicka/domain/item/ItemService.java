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

    // question: MapStruct와 set을 사용 or 정적 팩토리 메서드 or 지금 방법 뭐가 나을까요?
    @Transactional
    public Item saveItem(CreateItemReq itemDetail) {
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
                .state(itemDetail.getState())
                .repostDate(LocalDateTime.now())
                .build();

        item = itemRepository.save(item);

        return item;
    }

}
