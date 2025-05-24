package com.airfryer.repicka.domain.item_image.repository;

import com.airfryer.repicka.domain.item_image.entity.ItemImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemImageRepository extends JpaRepository<ItemImage, Long> {
}
