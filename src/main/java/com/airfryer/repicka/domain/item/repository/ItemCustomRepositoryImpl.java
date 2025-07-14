package com.airfryer.repicka.domain.item.repository;

import com.airfryer.repicka.domain.item.dto.SearchItemResult;
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
public class ItemCustomRepositoryImpl implements ItemCustomRepository
{
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public SearchItemResult findItemsByCondition(SearchItemReq condition)
    {
        /// ====== 공통 WHERE 절 ======

        StringBuilder whereBuilder = new StringBuilder("WHERE i.is_deleted=false ");
        List<Object> parameters = new ArrayList<>();

        // 키워드 조건
        if (StringUtils.hasText(condition.getKeyword())) {
            whereBuilder.append("AND LOWER(i.title) LIKE ? ");
            parameters.add("%" + condition.getKeyword().toLowerCase() + "%");
        }

        // 제품 타입 필터링
        if (condition.getProductTypes() != null && condition.getProductTypes().length > 0) {
            whereBuilder.append("AND i.product_types && ?::text[] ");
            parameters.add(Arrays.stream(condition.getProductTypes())
                    .map(Enum::name)
                    .toArray(String[]::new));
        }

        // 사이즈 조건
        if (condition.getSizes() != null && condition.getSizes().length > 0) {
            whereBuilder.append("AND i.size = ANY(?::text[]) ");
            parameters.add(Arrays.stream(condition.getSizes())
                    .map(Enum::name)
                    .toArray(String[]::new));
        }

        // 색상 조건
        if (condition.getColors() != null && condition.getColors().length > 0) {
            whereBuilder.append("AND i.color = ANY(?::text[]) ");
            parameters.add(Arrays.stream(condition.getColors())
                    .map(Enum::name)
                    .toArray(String[]::new));
        }

        // 거래 타입 조건
        if (condition.getTransactionTypes() != null && condition.getTransactionTypes().length > 0) {
            whereBuilder.append("AND i.transaction_types && ?::text[] ");
            parameters.add(Arrays.stream(condition.getTransactionTypes())
                    .map(Enum::name)
                    .toArray(String[]::new));
        }

        // 거래 방식 조건
        if (condition.getTradeMethods() != null && condition.getTradeMethods().length > 0) {
            whereBuilder.append("AND i.trade_methods && ?::text[] ");
            parameters.add(Arrays.stream(condition.getTradeMethods())
                    .map(Enum::name)
                    .toArray(String[]::new));
        }

        /// ====== Item 목록 조회 쿼리 ======

        StringBuilder itemQueryBuilder = new StringBuilder();
        itemQueryBuilder.append("SELECT * FROM item i ");
        itemQueryBuilder.append(whereBuilder);

        // 정렬 조건
        itemQueryBuilder.append(getOrderByClause(condition.getItemOrder()));

        // 페이징
        itemQueryBuilder.append("LIMIT 10 OFFSET ? ");
        parameters.add(condition.getPage() * 10);

        // 최종 쿼리
        Query itemQuery = entityManager.createNativeQuery(itemQueryBuilder.toString(), Item.class);

        // 파라미터 바인딩
        for(int i = 0; i < parameters.size(); i++) {
            itemQuery.setParameter(i + 1, parameters.get(i));
        }

        // 쿼리 실행 결과
        @SuppressWarnings("unchecked")
        List<Item> items = itemQuery.getResultList();

        /// ====== 전체 개수 조회 쿼리 ======

        StringBuilder countQueryBuilder = new StringBuilder();
        countQueryBuilder.append("SELECT COUNT(*) FROM item i ");
        countQueryBuilder.append(whereBuilder);

        // 최종 쿼리
        Query countQuery = entityManager.createNativeQuery(countQueryBuilder.toString());

        // 파라미터 바인딩
        for (int i = 0; i < parameters.size() - 1; i++) {
            countQuery.setParameter(i + 1, parameters.get(i));
        }

        // 쿼리 실행 결과
        int totalCount = ((Number) countQuery.getSingleResult()).intValue();

        return SearchItemResult.builder()
                .items(items)
                .totalCount(totalCount)
                .build();
    }

    private String getOrderByClause(ItemOrder itemOrder) {
        return switch (itemOrder) {
            case RECENT -> "ORDER BY i.repost_date DESC ";
            case LIKE -> "ORDER BY i.like_count DESC ";
            case RENTAL_FEE -> "ORDER BY i.rental_fee ASC ";
            case SALE_PRICE -> "ORDER BY i.sale_price ASC ";
        };
    }
}
