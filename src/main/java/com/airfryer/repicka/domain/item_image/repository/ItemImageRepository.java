package com.airfryer.repicka.domain.item_image.repository;

import com.airfryer.repicka.domain.item_image.entity.ItemImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemImageRepository extends JpaRepository<ItemImage, Long>
{
    // 상품 별 대표 이미지 찾기
    ItemImage findByDisplayOrderAndItemId(Integer displayOrder, Long itemId);

    // 상품 별 이미지 목록 찾기
    List<ItemImage> findAllByItemId(Long itemId);
}
