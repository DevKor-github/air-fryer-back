package com.airfryer.repicka.domain.item_image;

import com.airfryer.repicka.domain.item.entity.Item;
import com.airfryer.repicka.domain.item_image.entity.ItemImage;
import com.airfryer.repicka.domain.item_image.repository.ItemImageRepository;
import com.airfryer.repicka.util.CreateEntityUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class ItemImageTest
{
    @Autowired ItemImageRepository itemImageRepository;

    @Autowired CreateEntityUtil createEntityUtil;

    @Test
    @DisplayName("ItemImage 엔티티 생성 테스트")
    void createItemImage()
    {
        /// Given

        ItemImage itemImage = createEntityUtil.createItemImage();

        /// Then

        ItemImage findItemImage = itemImageRepository.findById(itemImage.getId()).orElse(null);

        assertThat(findItemImage).isNotNull();
        assertThat(findItemImage.getItem()).isEqualTo(itemImage.getItem());
        assertThat(findItemImage.getImageUrl()).isEqualTo(itemImage.getImageUrl());
    }
}
