package com.airfryer.repicka.domain.item.repository;

import com.airfryer.repicka.domain.item.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long>
{
    // 제품 타입에 해당하는 상품 모두 찾기
    @Query(value = "SELECT * FROM products WHERE product_type && ARRAY[:productType]::text[]", nativeQuery = true)
    List<Item> findByProductType(ProductType[] productTypes);

    // 사이즈에 해당하는 상품 모두 찾기
    List<Item> findBySize(ItemSize size);

    // 색상에 해당하는 상품 모두 찾기
    List<Item> findByColor(ItemColor color);

    // 품질에 해당하는 상품 모두 찾기
    List<Item> findByQuality(ItemQuality quality);

    // 거래 방식에 해당하는 상품 모두 찾기
    List<Item> findByTradeMethod(TradeMethod tradeMethod);
}
