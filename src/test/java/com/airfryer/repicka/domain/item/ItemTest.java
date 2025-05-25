package com.airfryer.repicka.domain.item;

import com.airfryer.repicka.domain.item.entity.*;
import com.airfryer.repicka.domain.item.repository.ItemRepository;
import com.airfryer.repicka.domain.user.entity.Gender;
import com.airfryer.repicka.domain.user.entity.LoginMethod;
import com.airfryer.repicka.domain.user.entity.Role;
import com.airfryer.repicka.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class ItemTest
{
    @Autowired ItemRepository itemRepository;

    @Test
    @DisplayName("Item 엔티티 생성 테스트")
    void createItem()
    {
        /// Given

        ProductType[] productType = {ProductType.HOCKEY, ProductType.ACCESSORY};
        ItemSize size = ItemSize.XL;
        String title = "title";
        String description = "description";
        ItemColor color = ItemColor.BLACK;
        ItemQuality quality = ItemQuality.LOW;
        String location = "location";
        TradeMethod tradeMethod = TradeMethod.DIRECT;
        Boolean canDeal = true;
        CurrentItemState state = CurrentItemState.AVAILABLE;
        LocalDateTime repostDate = LocalDateTime.now();

        /// When

        Item item = Item.builder()
                .productType(productType)
                .size(size)
                .title(title)
                .description(description)
                .color(color)
                .quality(quality)
                .location(location)
                .tradeMethod(tradeMethod)
                .canDeal(canDeal)
                .state(state)
                .repostDate(repostDate)
                .build();

        item = itemRepository.save(item);

        /// Then

        Item findItem = itemRepository.findById(item.getId()).orElse(null);

        assertThat(findItem).isNotNull();
        assertThat(findItem.getProductType()).isEqualTo(productType);
        assertThat(findItem.getSize()).isEqualTo(size);
        assertThat(findItem.getTitle()).isEqualTo(title);
        assertThat(findItem.getDescription()).isEqualTo(description);
        assertThat(findItem.getColor()).isEqualTo(color);
        assertThat(findItem.getQuality()).isEqualTo(quality);
        assertThat(findItem.getLocation()).isEqualTo(location);
        assertThat(findItem.getTradeMethod()).isEqualTo(tradeMethod);
        assertThat(findItem.getCanDeal()).isEqualTo(canDeal);
        assertThat(findItem.getState()).isEqualTo(state);
        assertThat(findItem.getRepostDate()).isEqualTo(repostDate);
    }
}
