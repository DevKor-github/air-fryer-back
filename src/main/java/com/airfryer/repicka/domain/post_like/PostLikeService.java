package com.airfryer.repicka.domain.post_like;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.airfryer.repicka.domain.post_like.repository.PostLikeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.airfryer.repicka.domain.post_like.dto.PostLikeRes;
import com.airfryer.repicka.domain.post_like.entity.PostLike;
import com.airfryer.repicka.domain.item_image.ItemImageService;
import com.airfryer.repicka.domain.item.entity.Item;
import com.airfryer.repicka.domain.post.entity.Post;
import com.airfryer.repicka.domain.user.entity.User;
import com.airfryer.repicka.common.exception.CustomException;
import com.airfryer.repicka.common.exception.CustomExceptionCode;

@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final ItemImageService itemImageService;

    @Transactional
    public boolean likePost(Long postId, User user) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new CustomException(CustomExceptionCode.POST_NOT_FOUND, postId));
        
        Optional<PostLike> existingLike = postLikeRepository.findByPostAndLiker(post, user);
        if (existingLike.isPresent()) {
            // 이미 좋아요를 눌렀으면 좋아요 취소
            postLikeRepository.delete(existingLike.get());

            // 좋아요 취소 시 좋아요 수 감소
            post.removeLikeCount();

            return false;
        }
        else {
            // 좋아요를 누르지 않았으면 좋아요 추가
            PostLike postLike = PostLike.builder()
                .post(post)
                .liker(user)
                .build();
            
            postLikeRepository.save(postLike);

            // 좋아요 추가 시 좋아요 수 증가
            post.addLikeCount();

            return true;
        }
    }

    @Transactional(readOnly = true)
    public List<PostLikeRes> getPostLikes(User user) {
        List<PostLike> postLikes = postLikeRepository.findByLiker(user);

        // 모든 Item의 썸네일 배치 조회
        List<Item> items = postLikes.stream()
            .map(PostLike::getPost)
            .map(Post::getItem)
            .toList();
        Map<Long, String> thumbnailMap = itemImageService.getThumbnailsForItems(items);

        return postLikes.stream()
            .map(postLike -> PostLikeRes.from(postLike.getPost(), thumbnailMap.get(postLike.getPost().getItem().getId())))
            .toList();
    }
}
