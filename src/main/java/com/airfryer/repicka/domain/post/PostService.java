package com.airfryer.repicka.domain.post;

import com.airfryer.repicka.domain.item.ItemService;
import com.airfryer.repicka.domain.item.entity.Item;
import com.airfryer.repicka.domain.item_image.ItemImageService;
import com.airfryer.repicka.domain.item_image.entity.ItemImage;
import com.airfryer.repicka.domain.post.dto.CreatePostReq;
import com.airfryer.repicka.domain.post.dto.PostDetailRes;
import com.airfryer.repicka.domain.post.entity.Post;
import com.airfryer.repicka.domain.post.entity.PostType;
import com.airfryer.repicka.domain.post.repository.PostRepository;
import com.airfryer.repicka.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final ItemService itemService;
    private final ItemImageService itemImageService;

    public PostDetailRes createPostWithItemAndImages(CreatePostReq postDetail, User user) {
        // 상품, 상품 이미지, 게시글 저장
        Item item = itemService.saveItem(postDetail.getItem());
        List<ItemImage> images = itemImageService.saveItemImage(postDetail.getImages(), item);
        Post post = Post.builder()
                .writer(user)
                .item(item)
                .postType(postDetail.getPostType())
                .price(postDetail.getPrice())
                .deposit(postDetail.getPostType() == PostType.RENTAL ? postDetail.getDeposit() : 0)
                .build();
        post = postRepository.save(post);

        // entity 정보를 PostDetailResDto로 정제
        PostDetailRes postDetailRes = PostDetailRes.from(user, item, post, postDetail.getImages());

        return postDetailRes;
    }

}
