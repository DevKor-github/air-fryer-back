package com.airfryer.repicka.domain.item_like;

import com.airfryer.repicka.domain.item_like.entity.ItemLike;
import com.airfryer.repicka.domain.item_like.repository.ItemLikeRepository;
import com.airfryer.repicka.util.CreateEntityUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class ItemLikeTest
{
    @Autowired
    ItemLikeRepository itemLikeRepository;

    @Autowired CreateEntityUtil createEntityUtil;

    @Test
    @DisplayName("PostLike 엔티티 생성 테스트")
    void createPostLike()
    {
        /// Given

        ItemLike itemLike = createEntityUtil.createPostLike();

        /// Then

        ItemLike findItemLike = itemLikeRepository.findById(itemLike.getId()).orElse(null);

        assertThat(findItemLike).isNotNull();
        assertThat(findItemLike.getLiker()).isEqualTo(itemLike.getLiker());
        assertThat(findItemLike.getPost()).isEqualTo(itemLike.getPost());
    }
}
