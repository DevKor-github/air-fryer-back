package com.airfryer.repicka.domain.post_like;

import com.airfryer.repicka.domain.post.entity.Post;
import com.airfryer.repicka.domain.post_like.entity.PostLike;
import com.airfryer.repicka.domain.post_like.repository.PostLikeRepository;
import com.airfryer.repicka.util.CreateEntityUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class PostLikeTest
{
    @Autowired PostLikeRepository postLikeRepository;

    @Autowired CreateEntityUtil createEntityUtil;

    @Test
    @DisplayName("PostLike 엔티티 생성 테스트")
    void createPostLike()
    {
        /// Given

        PostLike postLike = createEntityUtil.createPostLike();

        /// Then

        PostLike findPostLike = postLikeRepository.findById(postLike.getId()).orElse(null);

        assertThat(findPostLike).isNotNull();
        assertThat(findPostLike.getLiker()).isEqualTo(postLike.getLiker());
        assertThat(findPostLike.getPost()).isEqualTo(postLike.getPost());
    }
}
