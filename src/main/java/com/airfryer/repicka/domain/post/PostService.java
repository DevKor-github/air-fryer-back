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

import java.time.LocalDateTime;
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

    // 게시글 이미지 업로드 위해 presigned url 발급
    public PresignedUrlRes getPresignedUrl(PresignedUrlReq req, User user) {
        return s3Service.generatePresignedUrl(req, "post");
    }

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

        // 모든 Item의 썸네일 배치 조회
        List<Item> items = posts.stream()
            .map(Post::getItem)
            .toList();
        Map<Long, String> thumbnailMap = itemImageService.getThumbnailsForItems(items);

        // 게시글 정보 PostPreviewRes로 정제
        List<PostPreviewRes> postPreviewResList = posts.stream()
            .map(post -> {
                boolean isAvailable = appointmentService.isPostAvailableOnDate(post.getId(), condition.getDate()); // 원하는 날짜에 대여나 구매 가능 여부
                String thumbnailUrl = thumbnailMap.get(post.getItem().getId()); // 대표 사진
                return PostPreviewRes.from(post, thumbnailUrl, isAvailable);
            })
            .toList();

        return postPreviewResList;
    }

    // 게시글 수정
    @Transactional
    public List<PostDetailRes> updatePost(Long postId, CreatePostReq req, User user) {
        // 게시글 조회 및 작성자 권한 확인
        Post post = validatePostOwnership(postId, user);

        // 제품 수정
        Item updatedItem = itemService.updateItem(post.getItem().getId(), req.getItem());

        // 같은 제품의 게시글 모두 수정
        List<Post> postsWithSameItem = postRepository.findByItemId(post.getItem().getId());
        for (Post postWithSameItem : postsWithSameItem) {
            postWithSameItem.updatePriceAndDeposit(postWithSameItem.getPostType() == PostType.RENTAL ? req.getRentalFee() : req.getSalePrice(),
                postWithSameItem.getPostType() == PostType.RENTAL ? req.getDeposit() : 0);
        }

        return postsWithSameItem.stream()
            .map(postWithSameItem -> PostDetailRes.from(postWithSameItem, itemImageService.getItemImages(updatedItem)))
            .toList();
    }   

    // 게시글 삭제
    @Transactional
    public void deletePost(Long postId, User user) {
        // 게시글 조회 및 작성자 권한 확인
        Post post = validatePostOwnership(postId, user);

        // 게시글에 대한 약속이 존재하면 삭제 불가
        if (!appointmentService.isPostAvailableOnInterval(post.getId(), LocalDateTime.now())) {
            throw new CustomException(CustomExceptionCode.ALREADY_RESERVED_POST, null);
        }

        // 게시글 삭제
        postRepository.delete(post);
    }

    // 게시글 조회 및 작성자 권한을 확인하여 에러를 반환
    private Post validatePostOwnership(Long postId, User user) {
        // 게시글 조회
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new CustomException(CustomExceptionCode.POST_NOT_FOUND, postId));

        // 게시글 작성자 권한 확인
        if (!post.getWriter().getId().equals(user.getId())) {
            throw new CustomException(CustomExceptionCode.POST_ACCESS_FORBIDDEN, null);
        }

        return post;
    }
}
