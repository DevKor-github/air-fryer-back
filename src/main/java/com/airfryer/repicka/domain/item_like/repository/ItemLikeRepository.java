package com.airfryer.repicka.domain.item_like.repository;

import com.airfryer.repicka.domain.item_like.entity.ItemLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemLikeRepository extends JpaRepository<ItemLike, Long> {
}
