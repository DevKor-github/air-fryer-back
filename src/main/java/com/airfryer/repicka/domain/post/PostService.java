package com.airfryer.repicka.domain.post;

import com.airfryer.repicka.domain.item.entity.Item;
import com.airfryer.repicka.domain.post.dto.CreatePostReq;
import com.airfryer.repicka.domain.post.entity.Post;
import com.airfryer.repicka.domain.post.entity.PostType;
import com.airfryer.repicka.domain.post.repository.PostRepository;
import com.airfryer.repicka.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;

    public Post savePost(CreatePostReq postDetail, User user, Item item) {
        Post post = Post.builder()
                .writer(user)
                .item(item)
                .postType(postDetail.getPostType())
                .price(postDetail.getPrice())
                .deposit(postDetail.getPostType() == PostType.RENTAL ? postDetail.getDeposit() : 0)
                .build();

        post = postRepository.save(post);

        return post;
    }

}
