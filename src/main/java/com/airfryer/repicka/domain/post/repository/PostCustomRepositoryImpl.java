package com.airfryer.repicka.domain.post.repository;

import com.airfryer.repicka.domain.post.dto.PostOrder;
import com.airfryer.repicka.domain.post.dto.SearchPostReq;
import com.airfryer.repicka.domain.post.entity.Post;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
public class PostCustomRepositoryImpl implements PostCustomRepository {

    @PersistenceContext
    private EntityManager entityManager;

    // 태그 기반 게시글 검색 - 네이티브 쿼리 방식
    @Override
    public List<Post> findPostsByCondition(SearchPostReq condition) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT p.* FROM post p ")
                   .append("JOIN item i ON p.item = i.id ")
                   .append("WHERE 1=1 ");

        List<Object> parameters = new ArrayList<>();
        int paramIndex = 1;

        // 키워드 조건
        if (StringUtils.hasText(condition.getKeyword())) {
            queryBuilder.append("AND LOWER(i.title) LIKE ?").append(paramIndex).append(" ");
            parameters.add("%" + condition.getKeyword().toLowerCase() + "%");
            paramIndex++;
        }

        // 제품 타입 필터링 - PostgreSQL 배열 연산자 사용
        if (condition.getProductType() != null) {
            queryBuilder.append("AND ?").append(paramIndex).append(" = ANY(i.product_type) ");
            parameters.add(condition.getProductType().name());
            paramIndex++;
        }

        // 사이즈 조건
        if (condition.getSize() != null) {
            queryBuilder.append("AND i.size = ?").append(paramIndex).append(" ");
            parameters.add(condition.getSize().name());
            paramIndex++;
        }

        // 색상 조건
        if (condition.getColor() != null) {
            queryBuilder.append("AND i.color = ?").append(paramIndex).append(" ");
            parameters.add(condition.getColor().name());
            paramIndex++;
        }

        // 게시글 타입 조건
        if (condition.getPostType() != null) {
            queryBuilder.append("AND p.post_type = ?").append(paramIndex).append(" ");
            parameters.add(condition.getPostType().name());
            paramIndex++;
        }

        // 정렬 조건
        queryBuilder.append(getOrderByClause(condition.getPostOrder()));
        
        // 페이징
        queryBuilder.append("LIMIT 10 OFFSET ?").append(paramIndex);
        parameters.add(condition.getPage() * 10);

        Query query = entityManager.createNativeQuery(queryBuilder.toString(), Post.class);
        
        // 파라미터 바인딩
        for (int i = 0; i < parameters.size(); i++) {
            query.setParameter(i + 1, parameters.get(i));
        }

        return query.getResultList();
    }

    private String getOrderByClause(PostOrder postOrder) {
        return switch (postOrder) {
            case PostOrder.RECENT -> "ORDER BY i.repost_date DESC ";
            case PostOrder.LIKE -> "ORDER BY p.like_count DESC ";
            case PostOrder.PRICE -> "ORDER BY p.price ASC ";
        };
    }
}

