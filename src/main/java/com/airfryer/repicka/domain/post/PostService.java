package com.airfryer.repicka.domain.post;

import com.airfryer.repicka.common.exception.CustomException;
import com.airfryer.repicka.common.exception.CustomExceptionCode;
import com.airfryer.repicka.domain.item.ItemService;
import com.airfryer.repicka.domain.item.entity.Item;
import com.airfryer.repicka.domain.item_image.ItemImageService;
import com.airfryer.repicka.domain.item_image.entity.ItemImage;
import com.airfryer.repicka.domain.post.dto.CreatePostReq;
import com.airfryer.repicka.domain.post.dto.PostDetailRes;
import com.airfryer.repicka.domain.post.dto.PostPreviewRes;
import com.airfryer.repicka.domain.post.dto.SearchPostReq;
import com.airfryer.repicka.domain.post.entity.Post;
import com.airfryer.repicka.domain.post.entity.PostType;
import com.airfryer.repicka.domain.post.repository.PostRepository;
import com.airfryer.repicka.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final ItemService itemService;
    private final ItemImageService itemImageService;

    // 게시글 생성
    @Transactional
    public List<PostDetailRes> createPostWithItemAndImages(CreatePostReq postDetail, User user) {
        // 상품, 상품 이미지 저장
        Item item = itemService.createItem(postDetail.getItem());
        List<ItemImage> itemImages = itemImageService.createItemImage(postDetail.getImages(), item);

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
            PostDetailRes postDetailRes = PostDetailRes.from(post, itemImages);
            postDetailResList.add(postDetailRes);
        }

        return postDetailResList;
    }

    public PostDetailRes getPostDetail(Long postId) {
        // 게시글, 상품, 이미지 등 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.POST_NOT_FOUND, postId));

        List<ItemImage> itemImages = itemImageService.getItemImages(post.getItem());

        return PostDetailRes.from(post, itemImages);
    }

    // 게시글 목록 검색
    @Transactional(readOnly = true)
    public List<PostPreviewRes> searchPostList(SearchPostReq condition) {
        // 태그로 게시글 리스트 찾기
        List<Post> posts = postRepository.findPostsByCondition(condition);

        // TODO: 대여 날짜에 대한 필터링 appointmentService와 연결

        // 게시글 정보 PostPreviewRes로 정제
        List<PostPreviewRes> postPreviewResList = new ArrayList<>();
        for (Post post : posts) {
            ItemImage itemImage = itemImageService.getThumbnail(post.getItem());
            PostPreviewRes postPreviewRes = PostPreviewRes.from(post, itemImage);
            postPreviewResList.add(postPreviewRes);
        }

        return postPreviewResList;
    }

}
