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
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final ItemService itemService;
    private final ItemImageService itemImageService;

    public PostDetailRes createPostWithItemAndImages(CreatePostReq postDetail, User user) {
        // 상품, 상품 이미지 저장
        Item item = itemService.saveItem(postDetail.getItem());
        List<ItemImage> images = itemImageService.saveItemImage(postDetail.getImages(), item);

        // 게시글 타입에 따라 저장
        // TODO: postType을 복수로 선택하여 엔터티가 두 개 생성된 경우 둘 중 어느 화면으로 이동해야 하는지 명확히 설정 (현재는 임시 설정)
        List<Post> posts = new ArrayList<>();
        for (PostType postType: postDetail.getPostType()) {
            Post post = Post.builder()
                    .writer(user)
                    .item(item)
                    .postType(postType)
                    .price(postDetail.getPrice())
                    .deposit(postType == PostType.RENTAL ? postDetail.getDeposit() : 0)
                    .build();
            posts.add(postRepository.save(post));
        }

        // entity 정보를 PostDetailResDto로 정제
        PostDetailRes postDetailRes = PostDetailRes.from(user, item, posts.get(0), postDetail.getImages());

        return postDetailRes;
    }

}
