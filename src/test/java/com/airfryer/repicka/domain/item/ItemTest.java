package com.airfryer.repicka.domain.item;

import com.airfryer.repicka.domain.item.entity.*;
import com.airfryer.repicka.domain.item.repository.ItemRepository;
import com.airfryer.repicka.domain.user.entity.Gender;
import com.airfryer.repicka.domain.user.entity.LoginMethod;
import com.airfryer.repicka.domain.user.entity.Role;
import com.airfryer.repicka.domain.user.entity.User;
import com.airfryer.repicka.util.CreateEntityUtil;
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

    @Autowired CreateEntityUtil createEntityUtil;

    @Test
    @DisplayName("Item 엔티티 생성 테스트")
    void createItem()
    {
        /// Given

        Item item = createEntityUtil.createItem();

        /// Then

        Item findItem = itemRepository.findById(item.getId()).orElse(null);

        assertThat(findItem).isNotNull();
        assertThat(findItem.getProductTypes()).isEqualTo(item.getProductTypes());
        assertThat(findItem.getSize()).isEqualTo(item.getSize());
        assertThat(findItem.getTitle()).isEqualTo(item.getTitle());
        assertThat(findItem.getDescription()).isEqualTo(item.getDescription());
        assertThat(findItem.getColor()).isEqualTo(item.getColor());
        assertThat(findItem.getQuality()).isEqualTo(item.getQuality());
        assertThat(findItem.getLocation()).isEqualTo(item.getLocation());
        assertThat(findItem.getTradeMethod()).isEqualTo(item.getTradeMethod());
        assertThat(findItem.getCanDeal()).isEqualTo(item.getCanDeal());
        assertThat(findItem.getState()).isEqualTo(item.getState());
        assertThat(findItem.getRepostDate()).isEqualTo(item.getRepostDate());
    }
}
