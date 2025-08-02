package com.airfryer.repicka.domain.item.repository;

import com.airfryer.repicka.domain.item.entity.Item;
import com.airfryer.repicka.domain.item.dto.ItemOrder;
import com.airfryer.repicka.domain.item.dto.req.SearchItemCountReq;
import com.airfryer.repicka.domain.item.dto.req.SearchItemReq;
import com.airfryer.repicka.domain.item.entity.TransactionType;

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
    public List<Item> findItemsByConditionWithoutCount(SearchItemReq condition)
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
        if (condition.getTransactionTypes() != null && condition.getTransactionTypes().length == 1) {
            whereBuilder.append("AND i.transaction_types && ?::text[] ");
            parameters.add(Arrays.stream(condition.getTransactionTypes())
                    .map(Enum::name)
                    .toArray(String[]::new));
        }

        // 거래 방식 조건
        if (condition.getTradeMethods() != null && condition.getTradeMethods().length == 1) {
            whereBuilder.append("AND i.trade_methods && ?::text[] ");
            parameters.add(Arrays.stream(condition.getTradeMethods())
                    .map(Enum::name)
                    .toArray(String[]::new));
        }

        // 거래 금액 조건
        if (condition.getTransactionTypes() == null || condition.getTransactionTypes().length == 2) {
            // 둘 다 선택하거나 선택 안함 → 대여료 판매가 둘 중 하나라도 조건에 해당되는지 체크
            whereBuilder.append("AND (( i.rental_fee >= ? AND i.rental_fee <= ? ) OR ( i.sale_price >= ? AND i.sale_price <= ? ))");
            parameters.add(condition.getStartPrice());  // rental_fee >=
            parameters.add(condition.getEndPrice());    // rental_fee <=
            parameters.add(condition.getStartPrice());  // sale_price >=
            parameters.add(condition.getEndPrice());    // sale_price <=
        } else if (condition.getTransactionTypes().length == 1) {
            // 하나만 선택 → 해당 거래 타입의 금액만 체크
            TransactionType selectedType = condition.getTransactionTypes()[0];
            if (selectedType == TransactionType.RENTAL) {
                whereBuilder.append("AND i.rental_fee >= ? AND i.rental_fee <= ? ");
                parameters.add(condition.getStartPrice());
                parameters.add(condition.getEndPrice());
            } 
            else if (selectedType == TransactionType.SALE) {
                whereBuilder.append("AND i.sale_price >= ? AND i.sale_price <= ? ");
                parameters.add(condition.getStartPrice());
                parameters.add(condition.getEndPrice());
            }
        }

        // 거래 날짜 조건
        boolean hasDateFilter = condition.getStartDate() != null && condition.getEndDate() != null;
        
        // 커서 기반 페이지네이션 조건
        boolean hasCursor = condition.getCursorId() != null;
        
        // 커서 기반 WHERE 조건 추가
        if (hasCursor) {
            whereBuilder.append(getCursorWhereClause(condition.getItemOrder()));
            
            // 커서 파라미터 추가 (정렬 조건에 따라 다름)
            if (condition.getItemOrder() == ItemOrder.RECENT) {
                if (condition.getCursorDate() != null) {
                    parameters.add(condition.getCursorDate()); // repost_date <
                    parameters.add(condition.getCursorDate()); // repost_date =
                }
                parameters.add(condition.getCursorId());       // id <
            } else if (condition.getItemOrder() == ItemOrder.LIKE) {
                if (condition.getCursorLike() != null) {
                    parameters.add(condition.getCursorLike()); // like_count 비교
                    parameters.add(condition.getCursorLike()); // like_count =
                }
                parameters.add(condition.getCursorId());        // id 비교
            }
        }

        /// ====== Item 목록 조회 쿼리 ======

        StringBuilder itemQueryBuilder = new StringBuilder();
        
        if (hasDateFilter) {
            // 거래 날짜 조건이 있는 경우: 겹치는 약속 확인
            itemQueryBuilder.append("SELECT DISTINCT i.* FROM item i ");
            itemQueryBuilder.append("""
                LEFT JOIN appointment conflicting_apt ON i.id = conflicting_apt.item 
                AND conflicting_apt.state IN ('CONFIRMED', 'IN_PROGRESS')
                AND conflicting_apt.type = 'RENTAL'
                AND (
                    (conflicting_apt.rental_date >= ? AND conflicting_apt.rental_date <= ?) OR
                    (conflicting_apt.return_date >= ? AND conflicting_apt.return_date <= ?) OR
                    (conflicting_apt.rental_date <= ? AND conflicting_apt.return_date >= ?)
                ) """);
            
            // 겹치는 약속이 없는 아이템만 선택
            whereBuilder.append("AND conflicting_apt.id IS NULL ");
        } else {
            // 거래 날짜 조건이 없는 경우
            itemQueryBuilder.append("SELECT * FROM item i ");
        }
        
        // 그 외 조건절 추가
        itemQueryBuilder.append(whereBuilder);

        // 정렬 조건
        itemQueryBuilder.append(getOrderByClause(condition.getItemOrder()));
        itemQueryBuilder.append("LIMIT ? ");

        // 최종 쿼리
        Query itemQuery = entityManager.createNativeQuery(itemQueryBuilder.toString(), Item.class);

        // 파라미터 바인딩
        int paramIndex = 1;
        
        if (hasDateFilter) {
            // 거래 날짜 조건절용 파라미터들
            itemQuery.setParameter(paramIndex++, condition.getStartDate());  // rental_date >=
            itemQuery.setParameter(paramIndex++, condition.getEndDate());    // rental_date <=
            itemQuery.setParameter(paramIndex++, condition.getStartDate());  // return_date >=
            itemQuery.setParameter(paramIndex++, condition.getEndDate());    // return_date <=
            itemQuery.setParameter(paramIndex++, condition.getStartDate());  // rental_date <= (완전포함)
            itemQuery.setParameter(paramIndex++, condition.getEndDate());    // return_date >= (완전포함)
        }
        
        // 그 외 조건절용 파라미터들
        for (Object param : parameters) {
            itemQuery.setParameter(paramIndex++, param);
        }
        
        // LIMIT 파라미터 (pageSize)
        itemQuery.setParameter(paramIndex, condition.getPageSize() + 1);

        // 쿼리 실행 결과
        @SuppressWarnings("unchecked")
        List<Item> items = itemQuery.getResultList();

        return items;
    }

    @Override
    public int countItemsByCondition(SearchItemCountReq condition)
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
        if (condition.getTransactionTypes() != null && condition.getTransactionTypes().length == 1) {
            whereBuilder.append("AND i.transaction_types && ?::text[] ");
            parameters.add(Arrays.stream(condition.getTransactionTypes())
                    .map(Enum::name)
                    .toArray(String[]::new));
        }

        // 거래 방식 조건
        if (condition.getTradeMethods() != null && condition.getTradeMethods().length == 1) {
            whereBuilder.append("AND i.trade_methods && ?::text[] ");
            parameters.add(Arrays.stream(condition.getTradeMethods())
                    .map(Enum::name)
                    .toArray(String[]::new));
        }

        // 거래 금액 조건
        if (condition.getTransactionTypes() == null || condition.getTransactionTypes().length == 2) {
            // 둘 다 선택하거나 선택 안함 → 대여료 판매가 둘 중 하나라도 조건에 해당되는지 체크
            whereBuilder.append("AND (( i.rental_fee >= ? AND i.rental_fee <= ? ) OR ( i.sale_price >= ? AND i.sale_price <= ? ))");
            parameters.add(condition.getStartPrice());  // rental_fee >=
            parameters.add(condition.getEndPrice());    // rental_fee <=
            parameters.add(condition.getStartPrice());  // sale_price >=
            parameters.add(condition.getEndPrice());    // sale_price <=
        } else if (condition.getTransactionTypes().length == 1) {
            // 하나만 선택 → 해당 거래 타입의 금액만 체크
            TransactionType selectedType = condition.getTransactionTypes()[0];
            if (selectedType == TransactionType.RENTAL) {
                whereBuilder.append("AND i.rental_fee >= ? AND i.rental_fee <= ? ");
                parameters.add(condition.getStartPrice());
                parameters.add(condition.getEndPrice());
            } 
            else if (selectedType == TransactionType.SALE) {
                whereBuilder.append("AND i.sale_price >= ? AND i.sale_price <= ? ");
                parameters.add(condition.getStartPrice());
                parameters.add(condition.getEndPrice());
            }
        }

        // 거래 날짜 조건
        boolean hasDateFilter = condition.getStartDate() != null && condition.getEndDate() != null;

        /// ====== COUNT 쿼리 ======
        
        StringBuilder countQueryBuilder = new StringBuilder();
        
        if (hasDateFilter) {
            // 거래 날짜 조건이 있는 경우: 겹치는 약속 확인
            countQueryBuilder.append("SELECT COUNT(DISTINCT i.id) FROM item i ");
            countQueryBuilder.append("""
                LEFT JOIN appointment conflicting_apt ON i.id = conflicting_apt.item 
                AND conflicting_apt.state IN ('CONFIRMED', 'IN_PROGRESS')
                AND conflicting_apt.type = 'RENTAL'
                AND (
                    (conflicting_apt.rental_date >= ? AND conflicting_apt.rental_date <= ?) OR
                    (conflicting_apt.return_date >= ? AND conflicting_apt.return_date <= ?) OR
                    (conflicting_apt.rental_date <= ? AND conflicting_apt.return_date >= ?)
                ) """);
            
            // 겹치는 약속이 없는 아이템만 선택
            whereBuilder.append("AND conflicting_apt.id IS NULL ");
        } else {
            // 거래 날짜 조건이 없는 경우
            countQueryBuilder.append("SELECT COUNT(*) FROM item i ");
        }
        
        // 그 외 조건절 추가
        countQueryBuilder.append(whereBuilder);

        // 최종 쿼리
        Query countQuery = entityManager.createNativeQuery(countQueryBuilder.toString());

        // 파라미터 바인딩
        int paramIndex = 1;
        
        if (hasDateFilter) {
            // 거래 날짜 조건절용 파라미터들
            countQuery.setParameter(paramIndex++, condition.getStartDate());
            countQuery.setParameter(paramIndex++, condition.getEndDate());
            countQuery.setParameter(paramIndex++, condition.getStartDate());
            countQuery.setParameter(paramIndex++, condition.getEndDate());
            countQuery.setParameter(paramIndex++, condition.getStartDate());
            countQuery.setParameter(paramIndex++, condition.getEndDate());
        }
        
        // 그 외 조건절용 파라미터들 (커서 파라미터 포함)
        for (Object param : parameters) {
            countQuery.setParameter(paramIndex++, param);
        }

        // 쿼리 실행 결과
        int totalCount = ((Number) countQuery.getSingleResult()).intValue();

        return totalCount;
    }

    private String getOrderByClause(ItemOrder itemOrder) {
        return switch (itemOrder) {
            case RECENT -> "ORDER BY i.repost_date DESC, i.id DESC ";
            case LIKE -> "ORDER BY i.like_count DESC, i.id DESC ";
            // case RENTAL_FEE -> "ORDER BY i.rental_fee ASC, i.id DESC ";
            // case SALE_PRICE -> "ORDER BY i.sale_price ASC, i.id DESC ";
        };
    }
    
    private String getCursorWhereClause(ItemOrder itemOrder) {
        return switch (itemOrder) {
            case RECENT -> "AND (i.repost_date < ? OR (i.repost_date = ? AND i.id < ?)) ";
            case LIKE -> "AND (i.like_count < ? OR (i.like_count = ? AND i.id < ?)) ";
            // case RENTAL_FEE -> "AND (i.rental_fee > ? OR (i.rental_fee = ? AND i.id < ?)) ";
            // case SALE_PRICE -> "AND (i.sale_price > ? OR (i.sale_price = ? AND i.id < ?)) ";
        };
    }
}
