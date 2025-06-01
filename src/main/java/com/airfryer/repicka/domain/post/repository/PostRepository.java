package com.airfryer.repicka.domain.post.repository;

import com.airfryer.repicka.domain.post.entity.Post;
import com.airfryer.repicka.domain.post.entity.PostType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, PostCustomRepository
{
    // 제품 ID, 게시글 타입으로 게시글 데이터 조회
    Optional<Post> findByItemIdAndPostType(Long itemId, PostType postType);
}
