package com.airfryer.repicka.domain.post;

import com.airfryer.repicka.common.aws.s3.S3Service;
import com.airfryer.repicka.common.aws.s3.dto.PresignedUrlReq;
import com.airfryer.repicka.common.aws.s3.dto.PresignedUrlRes;
import com.airfryer.repicka.common.exception.CustomException;
import com.airfryer.repicka.common.exception.CustomExceptionCode;
import com.airfryer.repicka.domain.appointment.service.AppointmentService;
import com.airfryer.repicka.domain.item.ItemService;
import com.airfryer.repicka.domain.item.entity.Item;
import com.airfryer.repicka.domain.item_image.ItemImageService;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final ItemService itemService;
    private final ItemImageService itemImageService;
    private final AppointmentService appointmentService;
    private final S3Service s3Service;

    // 게시글 생성
    @Transactional
    public List<PostDetailRes> createPostWithItemAndImages(CreatePostReq postDetail, User user) {
        // 상품, 상품 이미지 저장
        Item item = itemService.createItem(postDetail.getItem());
        itemImageService.createItemImage(postDetail.getImages(), item);

        // 게시글 타입에 따라 저장
        List<Post> posts = new ArrayList<>();

        for (PostType postType: postDetail.getPostTypes()) {
            Post post = Post.builder()
                    .writer(user)
                    .item(item)
                    .postType(postType)
                    .price(postType == PostType.RENTAL ? postDetail.getRentalFee() : postDetail.getSalePrice())
                    .deposit(postType == PostType.RENTAL ? postDetail.getDeposit() : 0)
                    .build();

            posts.add(post);
        }

        posts = postRepository.saveAll(posts);

        // 각 Post에 대해 PostDetailRes 생성
        List<PostDetailRes> postDetailResList = posts.stream()
                .map(post -> { return PostDetailRes.from(post, itemImageService.getItemImages(post.getItem())); })
                .toList();

        return postDetailResList;
    }

    public PostDetailRes getPostDetail(Long postId) {
        // 게시글, 상품, 이미지 등 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.POST_NOT_FOUND, postId));

        List<String> imageUrls = itemImageService.getItemImages(post.getItem());

        return PostDetailRes.from(post, imageUrls);
    }

    // 게시글 목록 검색
    public List<PostPreviewRes> searchPostList(SearchPostReq condition) {
        // 태그로 게시글 리스트 찾기
        List<Post> posts = postRepository.findPostsByCondition(condition);

        // ProductType 필터링
        if (condition.getProductType() != null) {
            posts = posts.stream()
                .filter(post -> Arrays.stream(post.getItem().getProductTypes())
                    .anyMatch(productType -> productType == condition.getProductType()))
                .toList();
        }

        // 모든 Item의 썸네일 배치 조회
        List<Item> items = posts.stream()
            .map(Post::getItem)
            .toList();
        Map<Long, String> thumbnailMap = itemImageService.getThumbnailsForItems(items);

        // 게시글 정보 PostPreviewRes로 정제
        List<PostPreviewRes> postPreviewResList = posts.stream()
            .map(post -> {
                boolean isAvailable = appointmentService.isPostAvailableOnDate(post.getId(), condition.getDate());
                String thumbnailUrl = thumbnailMap.get(post.getItem().getId());
                return PostPreviewRes.from(post, thumbnailUrl, isAvailable);
            })
            .toList();

        return postPreviewResList;
    }

    public PresignedUrlRes getPresignedUrl(PresignedUrlReq req, User user) {
        return s3Service.generatePresignedUrl(req, "post");
    }

}
