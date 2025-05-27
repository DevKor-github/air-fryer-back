package com.airfryer.repicka.domain.post.repository;

import com.airfryer.repicka.domain.item.entity.Item;
import com.airfryer.repicka.domain.post.entity.Post;
import com.airfryer.repicka.domain.post.entity.PostType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long>
{
    // 상품과 게시글 타입으로 게시글 찾기
    List<Post> findByItemAndPostType(Item item, PostType postType);
}
