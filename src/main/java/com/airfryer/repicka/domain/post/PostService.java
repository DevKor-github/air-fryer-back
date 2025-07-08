package com.airfryer.repicka.domain.post;

import com.airfryer.repicka.common.aws.s3.S3Service;
import com.airfryer.repicka.common.aws.s3.dto.PresignedUrlReq;
import com.airfryer.repicka.common.aws.s3.dto.PresignedUrlRes;
import com.airfryer.repicka.common.exception.CustomException;
import com.airfryer.repicka.common.exception.CustomExceptionCode;
import com.airfryer.repicka.domain.appointment.dto.GetItemAvailabilityRes;
import com.airfryer.repicka.domain.appointment.entity.Appointment;
import com.airfryer.repicka.domain.appointment.entity.AppointmentState;
import com.airfryer.repicka.domain.appointment.repository.AppointmentRepository;
import com.airfryer.repicka.domain.appointment.service.AppointmentService;
import com.airfryer.repicka.domain.item.ItemService;
import com.airfryer.repicka.domain.item.entity.Item;
import com.airfryer.repicka.domain.item_image.ItemImageService;
import com.airfryer.repicka.domain.post.dto.CreatePostReq;
import com.airfryer.repicka.domain.post.dto.PostDetailRes;
import com.airfryer.repicka.domain.post.dto.PostPreviewRes;
import com.airfryer.repicka.domain.post.dto.SearchPostReq;
import com.airfryer.repicka.domain.item.entity.PostType;
import com.airfryer.repicka.domain.post.repository.PostRepository;
import com.airfryer.repicka.domain.post_like.repository.PostLikeRepository;
import com.airfryer.repicka.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.time.YearMonth;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final ItemService itemService;
    private final ItemImageService itemImageService;
    
    private final AppointmentRepository appointmentRepository;
    private final AppointmentService appointmentService;

    private final PostLikeRepository postLikeRepository;

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
                .map(post -> { return PostDetailRes.from(post, itemImageService.getItemImages(post.getItem()), user, false); })
                .toList();

        return postDetailResList;
    }

    // 게시글 상세 조회
    @Transactional(readOnly = true)
    public PostDetailRes getPostDetail(Long postId, User user) {
        // 게시글, 상품, 이미지 등 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.POST_NOT_FOUND, postId));

        List<String> imageUrls = itemImageService.getItemImages(post.getItem());

        // 좋아요 여부 조회
        boolean isLiked = postLikeRepository.findByPostAndLiker(post, user).isPresent();

        return PostDetailRes.from(post, imageUrls, user, isLiked);
    }

    // 게시글 목록 검색
    @Transactional(readOnly = true)
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
            .map(postWithSameItem -> {
                List<String> itemImages = itemImageService.getItemImages(updatedItem);
                boolean isLiked = postLikeRepository.findByPostAndLiker(postWithSameItem, user).isPresent();
                return PostDetailRes.from(postWithSameItem, itemImages, user, isLiked);
            })
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
    
    // 게시글 끌올
    @Transactional
    public LocalDateTime repostPost(Long postId, User user) {
        // 게시글 조회 및 작성자 권한 확인
        Post post = validatePostOwnership(postId, user);

        // 제품 끌올
        post.getItem().repostItem();

        return post.getItem().getRepostDate();
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

    // 월 단위로 날짜별 제품 대여 가능 여부 조회
    @Transactional(readOnly = true)
    public GetItemAvailabilityRes getItemRentalAvailability(Long rentalPostId, int year, int month)
    {
        // 해당 월의 길이
        int lastDayOfMonth = YearMonth.of(year, month).lengthOfMonth();

        /// 게시글 데이터 조회

        // 게시글 데이터 조회
        Post rentalPost = postRepository.findById(rentalPostId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.POST_NOT_FOUND, rentalPostId));

        // 대여 게시글인지 체크
        if (rentalPost.getPostType() != PostType.RENTAL) {
            throw new CustomException(CustomExceptionCode.NOT_RENTAL_POST, rentalPost.getPostType());
        }

        /// 제품 데이터 조회

        // 제품 데이터 조회
        Item item = rentalPost.getItem();

        /// 반환할 날짜별 제품 대여 가능 여부 해시맵 생성 및 초기화

        // 반환할 날짜별 제품 대여 가능 여부 해시맵
        Map<LocalDate, Boolean> map = new LinkedHashMap<>();

        // 일단, 모든 날짜에 대여가 가능한 것으로 초기화
        for (int i = 1; i <= lastDayOfMonth; i++) {
            map.put(LocalDate.of(year, month, i), true);
        }

        /// 불가능 처리
        /// 1. 현재 이전의 날짜들은 전부 불가능 처리
        /// 2. 제품 판매 날짜부터는 전부 대여 불가능 처리
        /// 3. 해당 월 동안 예정된 모든 대여 약속들에 대해, 각 구간마다 대여 불가능 처리

        // 현재 이전의 날짜들은 전부 불가능 처리
        for(int i = 1; i <= lastDayOfMonth && LocalDate.of(year, month, i).isBefore(LocalDate.now()); i++) {
            map.put(LocalDate.of(year, month, i), false);
        }

        // 제품이 판매 예정 혹은 판매된 경우, 이후의 날짜들은 전부 대여 불가능 처리
        if(item.getSaleDate() != null)
        {
            LocalDate saleDate = item.getSaleDate().toLocalDate();  // 제품 판매 예정 날짜

            // 해당 월 이전에 이미 판매된 경우
            if(saleDate.isBefore(LocalDate.of(year, month, 1))) {
                for(int i = 1; i <= lastDayOfMonth; i++) {
                    map.put(LocalDate.of(year, month, i), false);
                }
            }
            // 해당 월 동안 판매되는 경우
            else if(
                    !saleDate.isBefore(LocalDate.of(year, month, 1)) &&
                            !saleDate.isAfter(LocalDate.of(year, month, lastDayOfMonth))
            ) {
                for(int i = saleDate.getDayOfMonth(); i <= lastDayOfMonth; i++) {
                    map.put(LocalDate.of(year, month, i), false);
                }
            }
        }

        // 해당 월 동안 존재하는 모든 대여 약속 조회
        List<Appointment> appointmentList = appointmentRepository.findListOverlappingWithPeriod(
                rentalPostId,
                AppointmentState.CONFIRMED,
                LocalDateTime.of(year, month, 1, 0, 0, 0),
                LocalDateTime.of(year, month, YearMonth.of(year, month).lengthOfMonth(), 23, 59, 59, 0)
        );

        // 모든 대여 약속 구간에 대해, 대여 불가능 처리
        for(Appointment appointment : appointmentList)
        {
            // 대여 시작 날짜가 해당 월 이전이고, 대여 종료 날짜가 해당 월 이후인 경우
            if(appointment.getRentalDate().getMonthValue() < month && appointment.getReturnDate().getMonthValue() > month)
            {
                map.replaceAll((key, value) -> false);
                break;
            }
            // 대여 시작 날짜가 해당 월에 속하고, 대여 종료 날짜가 해당 월 이후인 경우
            else if(appointment.getRentalDate().getMonthValue() == month && appointment.getReturnDate().getMonthValue() > month)
            {
                for(int i = appointment.getRentalDate().getDayOfMonth(); i <= lastDayOfMonth; i++)
                {
                    map.put(LocalDate.of(year, month, i), false);
                }
            }
            // 대여 시작 날짜가 해당 월 이전이고, 대여 종료 날짜가 해당 월에 속하는 경우
            else if(appointment.getRentalDate().getMonthValue() < month && appointment.getReturnDate().getMonthValue() == month)
            {
                for(int i = 1; i <= appointment.getReturnDate().getDayOfMonth(); i++)
                {
                    map.put(LocalDate.of(year, month, i), false);
                }
            }
            // 대여 시작 날짜 및 대여 종료 날짜가 둘 다 해당 월에 속하는 경우
            else
            {
                for(int i = appointment.getRentalDate().getDayOfMonth(); i <= appointment.getReturnDate().getDayOfMonth(); i++)
                {
                    map.put(LocalDate.of(year, month, i), false);
                }
            }
        }

        return GetItemAvailabilityRes.builder()
                .itemId(item.getId())
                .postId(rentalPost.getId())
                .year(year)
                .month(month)
                .availability(map)
                .build();
    }

    // 제품 구매가 가능한 첫 날짜 조회
    @Transactional(readOnly = true)
    public LocalDate getItemSaleAvailability(Long salePostId)
    {
        /// 게시글 데이터 조회

        // 판매 게시글 데이터 조회
        Post salePost = postRepository.findById(salePostId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.POST_NOT_FOUND, salePostId));

        // 판매 게시글인지 체크
        if(salePost.getPostType() != PostType.SALE) {
            throw new CustomException(CustomExceptionCode.NOT_SALE_POST, salePost.getPostType());
        }

        /// 제품 데이터 조회

        // 제품 데이터 조회
        Item item = salePost.getItem();

        // 판매 예정이거나 판매된 제품이 아닌지 체크
        if(item.getSaleDate() != null) {
            throw new CustomException(CustomExceptionCode.ALREADY_SALE_RESERVED, item.getId());
        }

        /// 제품 구매가 가능한 첫 날짜 반환

        return appointmentService.getFirstSaleAvailableDate(item.getId());
    }
}
