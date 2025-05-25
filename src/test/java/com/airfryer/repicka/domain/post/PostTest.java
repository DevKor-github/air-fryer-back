package com.airfryer.repicka.domain.post;

import com.airfryer.repicka.domain.post.entity.Post;
import com.airfryer.repicka.domain.post.repository.PostRepository;
import com.airfryer.repicka.domain.user.entity.User;
import com.airfryer.repicka.util.CreateEntityUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class PostTest
{
    @Autowired PostRepository postRepository;

    @Autowired CreateEntityUtil createEntityUtil;

    @Test
    @DisplayName("Post 엔티티 생성 테스트")
    void createPost()
    {
        /// Given

        Post post = createEntityUtil.createPost();

        /// Then

        Post findPost = postRepository.findById(post.getId()).orElse(null);

        assertThat(findPost).isNotNull();
        assertThat(findPost.getWriter()).isEqualTo(post.getWriter());
        assertThat(findPost.getItem()).isEqualTo(post.getItem());
        assertThat(findPost.getPostType()).isEqualTo(post.getPostType());
        assertThat(findPost.getPrice()).isEqualTo(post.getPrice());
        assertThat(findPost.getDeposit()).isEqualTo(post.getDeposit()).isEqualTo(0);
        assertThat(findPost.getLikeCount()).isEqualTo(post.getLikeCount()).isEqualTo(0);
    }
}
