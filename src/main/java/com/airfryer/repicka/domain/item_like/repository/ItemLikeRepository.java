package com.airfryer.repicka.domain.item_like.repository;

import com.airfryer.repicka.domain.item_like.entity.ItemLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemLikeRepository extends JpaRepository<ItemLike, Long>
{
    // 제품 ID와 사용자 ID로 제품 좋아요 데이터 조회
    Optional<ItemLike> findByItemIdAndLikerId(Long itemId, Long likerId);

    // 사용자 ID로 삭제 되지 않은 제품 좋아요 데이터 조회
    @Query("""
        SELECT il FROM ItemLike il
        WHERE il.liker.id = :likerId AND il.item.isDeleted = false
    """)
    List<ItemLike> findByLikerId(@Param("likerId") Long likerId);
}
