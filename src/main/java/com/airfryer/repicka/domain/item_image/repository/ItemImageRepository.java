package com.airfryer.repicka.domain.item_image.repository;

import com.airfryer.repicka.domain.item_image.entity.ItemImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemImageRepository extends JpaRepository<ItemImage, Long>
{
    // 상품 별 이미지 찾기
    Optional<ItemImage> findByDisplayOrderAndItemId(Integer displayOrder, Long itemId);

    // 상품 리스트로부터 대표 이미지 리스트 조회
    @Query("SELECT i FROM ItemImage i WHERE i.displayOrder = 1 AND i.item.id IN :itemIdList")
    List<ItemImage> findThumbnailListByItemIdList(@Param("itemIdList") List<Long> itemIdList);

    // 상품 별 이미지 목록 찾기
    List<ItemImage> findAllByItemId(Long itemId);
}
