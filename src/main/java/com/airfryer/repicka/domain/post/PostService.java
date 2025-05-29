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

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final ItemService itemService;
    private final ItemImageService itemImageService;

    @Transactional
    public List<PostDetailRes> createPostWithItemAndImages(CreatePostReq postDetail, User user) {
        // 상품, 상품 이미지 저장
        Item item = itemService.createItem(postDetail.getItem());
        List<ItemImage> images = itemImageService.createItemImage(postDetail.getImages(), item);

        // 게시글 타입에 따라 저장
        List<Post> posts = new ArrayList<>();

        for (PostType postType: postDetail.getPostType()) {
            Post post = Post.builder()
                    .writer(user)
                    .item(item)
                    .postType(postType)
                    .price(postDetail.getPrice())
                    .deposit(postType == PostType.RENTAL ? postDetail.getDeposit() : 0)
                    .build();

            posts.add(post);
        }

        posts = postRepository.saveAll(posts);

        // entity 정보를 사용하여 각 Post에 대해 PostDetailRes 생성
        List<PostDetailRes> postDetailResList = new ArrayList<>();
        for (Post post : posts) {
            PostDetailRes postDetailRes = PostDetailRes.from(user, item, post, postDetail.getImages());
            postDetailResList.add(postDetailRes);
        }

        return postDetailResList;
    }

}
