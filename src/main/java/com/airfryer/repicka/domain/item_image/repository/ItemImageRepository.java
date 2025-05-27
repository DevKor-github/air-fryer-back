package com.airfryer.repicka.domain.item_image.repository;

import com.airfryer.repicka.domain.item.entity.Item;
import com.airfryer.repicka.domain.item_image.entity.ItemImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemImageRepository extends JpaRepository<ItemImage, Long>
{
    // 상품 id로 해당하는 이미지 목록 찾기
    List<ItemImage> findByItem(Item item);
}
