package com.airfryer.repicka.domain.post.repository;

import com.airfryer.repicka.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
