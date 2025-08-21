package com.airfryer.repicka.domain.item_like.repository;

import com.airfryer.repicka.domain.item_like.entity.ItemLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemLikeRepository extends JpaRepository<ItemLike, Long>
{
    // 제품 ID와 사용자 ID로 제품 좋아요 데이터 조회
    Optional<ItemLike> findByItemIdAndLikerId(Long itemId, Long likerId);

    // 사용자 ID로 제품 좋아요 데이터 조회
    List<ItemLike> findByLikerId(Long likerId);
}
