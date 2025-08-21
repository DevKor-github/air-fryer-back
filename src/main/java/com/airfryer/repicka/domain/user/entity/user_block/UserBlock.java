package com.airfryer.repicka.domain.user.entity.user_block;

import com.airfryer.repicka.common.entity.BaseEntity;
import com.airfryer.repicka.domain.user.entity.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "user-block")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
public class UserBlock extends BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 차단자
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocker")
    private User blocker;

    // 피차단자
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked")
    private User blocked;
}
