package com.airfryer.repicka.domain.post.repository;

import com.airfryer.repicka.domain.post.dto.SearchPostReq;
import com.airfryer.repicka.domain.post.entity.Post;

import java.util.List;

public interface PostCustomRepository {
    List<Post> findPostsByCondition(SearchPostReq condition);
}
