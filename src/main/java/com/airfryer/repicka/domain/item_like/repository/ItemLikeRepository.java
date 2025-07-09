package com.airfryer.repicka.domain.item_like.repository;

import com.airfryer.repicka.domain.item_like.entity.ItemLike;
import com.airfryer.repicka.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemLikeRepository extends JpaRepository<ItemLike, Long>
{
    Optional<ItemLike> findByItemIdAndLikerId(Long itemId, Long likerId);

    @Query("SELECT pl FROM ItemLike pl JOIN FETCH pl.post JOIN FETCH pl.post.item WHERE pl.liker = :user")
    List<ItemLike> findByLiker(@Param("user") User user);
}
