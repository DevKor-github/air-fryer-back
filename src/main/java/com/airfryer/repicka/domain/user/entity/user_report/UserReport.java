package com.airfryer.repicka.domain.user.entity.user_report;

import com.airfryer.repicka.common.entity.BaseEntity;
import com.airfryer.repicka.domain.item.entity.Item;
import com.airfryer.repicka.domain.user.dto.ReportUserReq;
import com.airfryer.repicka.domain.user.entity.user.User;
import io.hypersistence.utils.hibernate.type.array.EnumArrayType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "user_report")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
public class UserReport extends BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 신고자
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter")
    private User reporter;

    // 피신고자
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported")
    private User reported;

    // 신고가 이루어진 제품
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item")
    private Item item;

    // 신고 퍼널
    @NotNull
    @Enumerated(EnumType.STRING)
    private ReportLocation location;

    // 신고 사유
    @NotEmpty
    @Type(
            value = EnumArrayType.class,
            parameters = @org.hibernate.annotations.Parameter(
                    name = EnumArrayType.SQL_ARRAY_TYPE,
                    value = "text"
            )
    )
    @Column(
            name = "categories",
            columnDefinition = "text[]"
    )
    private ReportCategory[] categories;

    // 부가 설명
    @Column(length = 1024)
    private String description;

    /// 수정

    public void update(ReportUserReq reportUserReq)
    {
        this.location = reportUserReq.getLocation();
        this.categories = reportUserReq.getCategories();
        this.description = reportUserReq.getDescription();
    }
}
