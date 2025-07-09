package com.airfryer.repicka.domain.item.repository;

import com.airfryer.repicka.domain.item.entity.Item;
import com.airfryer.repicka.domain.item.dto.ItemOrder;
import com.airfryer.repicka.domain.item.dto.SearchItemReq;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Repository
public class ItemCustomRepositoryImpl implements ItemCustomRepository {

    @PersistenceContext
    private EntityManager entityManager;

    // 태그 기반 게시글 검색 - 네이티브 쿼리 방식
    @Override
    public List<Item> findItemsByCondition(SearchItemReq condition)
    {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT i.* FROM post i ").append("WHERE 1=1 ");

        List<Object> parameters = new ArrayList<>();
        int paramIndex = 1;

        // 키워드 조건
        if (StringUtils.hasText(condition.getKeyword())) {
            queryBuilder.append("AND LOWER(i.title) LIKE ?").append(paramIndex).append(" ");
            parameters.add("%" + condition.getKeyword().toLowerCase() + "%");
            paramIndex++;
        }

        // 제품 타입 필터링
        if (condition.getProductTypes() != null && condition.getProductTypes().length > 0) {
            queryBuilder.append("AND i.product_types @> ?").append(paramIndex).append("::text[] ");
            parameters.add(Arrays.stream(condition.getProductTypes())
                    .map(Enum::name)
                    .toArray(String[]::new));
            paramIndex++;
        }

        // 사이즈 조건
        if (condition.getSizes() != null && condition.getSizes().length > 0) {
            queryBuilder.append("AND i.size = ANY(?").append(paramIndex).append("::text[]) ");
            parameters.add(Arrays.stream(condition.getSizes())
                    .map(Enum::name)
                    .toArray(String[]::new));
            paramIndex++;
        }

        // 색상 조건
        if (condition.getColors() != null && condition.getColors().length > 0) {
            queryBuilder.append("AND i.color = ANY(?").append(paramIndex).append("::text[]) ");
            parameters.add(Arrays.stream(condition.getColors())
                    .map(Enum::name)
                    .toArray(String[]::new));
            paramIndex++;
        }

        // 거래 타입 조건
        if (condition.getTransactionTypes() != null && condition.getTransactionTypes().length > 0) {
            queryBuilder.append("AND i.transaction_types @> ?").append(paramIndex).append("::text[] ");
            parameters.add(Arrays.stream(condition.getTransactionTypes())
                    .map(Enum::name)
                    .toArray(String[]::new));
            paramIndex++;
        }

        // 정렬 조건
        queryBuilder.append(getOrderByClause(condition.getItemOrder()));
        
        // 페이징
        queryBuilder.append("LIMIT 10 OFFSET ?").append(paramIndex);
        parameters.add(condition.getPage() * 10);

        Query query = entityManager.createNativeQuery(queryBuilder.toString(), Item.class);
        
        // 파라미터 바인딩
        for (int i = 0; i < parameters.size(); i++) {
            query.setParameter(i + 1, parameters.get(i));
        }

        return query.getResultList();
    }

    private String getOrderByClause(ItemOrder itemOrder) {
        return switch (itemOrder) {
            case ItemOrder.RECENT -> "ORDER BY i.repost_date DESC ";
            case ItemOrder.LIKE -> "ORDER BY i.like_count DESC ";
            case ItemOrder.PRICE -> "ORDER BY i.price ASC ";
        };
    }
}

