package com.airfryer.repicka.domain.post_like.repository;

import com.airfryer.repicka.domain.post_like.entity.PostLike;
import com.airfryer.repicka.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long>
{
    Optional<PostLike> findByPostAndLiker(Post post, User user);

    @Query("SELECT pl FROM PostLike pl JOIN FETCH pl.post JOIN FETCH pl.post.item WHERE pl.liker = :user")
    List<PostLike> findByLiker(@Param("user") User user);
}
