package com.airfryer.repicka.domain.chat.repository;

import com.airfryer.repicka.domain.chat.entity.ParticipateChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParticipateChatRoomRepository extends JpaRepository<ParticipateChatRoom, Long> {
}
