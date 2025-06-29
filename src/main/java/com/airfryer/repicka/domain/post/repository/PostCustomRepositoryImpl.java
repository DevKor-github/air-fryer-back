package com.airfryer.repicka.domain.post.repository;

import com.airfryer.repicka.domain.item.entity.QItem;
import com.airfryer.repicka.domain.post.dto.PostOrder;
import com.airfryer.repicka.domain.post.dto.SearchPostReq;
import com.airfryer.repicka.domain.post.entity.Post;
import com.airfryer.repicka.domain.post.entity.QPost;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
public class PostCustomRepositoryImpl implements PostCustomRepository {

    private final JPAQueryFactory queryFactory;

    public PostCustomRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    // 태그 기반 게시글 검색
    @Override
    public List<Post> findPostsByCondition(SearchPostReq condition) {
        QPost post = QPost.post;
        QItem item = QItem.item;
        int offset = condition.getPage() * 10;

        BooleanBuilder builder = new BooleanBuilder();

        if (StringUtils.hasText(condition.getKeyword())) {
            builder.and(item.title.containsIgnoreCase(condition.getKeyword()));
        }

        // 제품 타입 필터링은 Service에서 별도 처리

        if (condition.getSize() != null) {
            builder.and(item.size.eq(condition.getSize()));
        }

        if (condition.getColor() != null) {
            builder.and(item.color.eq(condition.getColor()));
        }

        if (condition.getPostType() != null) {
            builder.and(post.postType.eq(condition.getPostType()));
        }

        OrderSpecifier<?>[] orderSpecifiers = getOrderSpecifiers(condition.getPostOrder(), post, item);

        return queryFactory
                .selectFrom(post)
                .join(post.item, item).fetchJoin()
                .where(builder)
                .orderBy(orderSpecifiers)
                .offset(offset)
                .limit(10)
                .fetch();
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(PostOrder postOrder, QPost post, QItem item) {
        return switch (postOrder) {
            case PostOrder.RECENT -> new OrderSpecifier[]{item.repostDate.desc()};
            case PostOrder.LIKE -> new OrderSpecifier[]{post.likeCount.desc()};
            case PostOrder.PRICE -> new OrderSpecifier[]{post.price.asc()};
        };
    }
}

