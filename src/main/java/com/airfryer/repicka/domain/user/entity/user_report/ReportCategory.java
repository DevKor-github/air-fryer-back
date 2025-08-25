package com.airfryer.repicka.domain.user.entity.user_report;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ReportCategory
{
    OFFENSIVE("OFFENSIVE", "부적절한 언어 및 비속어를 사용했어요."),
    SEXUAL_CONTENT("SEXUAL_CONTENT", "음란물 또는 성적인 콘텐츠를 포함하고 있어요."),
    FAKE_ITEM("FAKE_ITEM", "허위 매물 및 정보를 포함하고 있어요."),
    PRIVACY("PRIVACY", "개인정보가 유출되어 있어요."),
    SPAM("SPAM", "스팸 및 광고성 게시물이에요."),
    OTHER("OTHER", "기타 (직접 입력)");

    private final String code;
    private final String label;
}
