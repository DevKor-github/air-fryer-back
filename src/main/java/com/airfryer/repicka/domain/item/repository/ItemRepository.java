package com.airfryer.repicka.domain.item.repository;

import com.airfryer.repicka.domain.item.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long>
{
    // 소유자 ID로 삭제되지 않은 제품 리스트 조회
    List<Item> findAllByOwnerIdAndIsDeletedFalseOrderByCreatedAtDesc(Long ownerId);
}
