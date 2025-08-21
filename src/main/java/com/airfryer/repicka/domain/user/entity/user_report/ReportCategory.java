package com.airfryer.repicka.domain.user.entity.user_report;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ReportCategory
{
    SPAM("SPAM", "광고성 메시지, 도배, 무의미한 반복 글 또는 링크 전송"),
    OFFENSIVE("OFFENSIVE", "욕설, 혐오 발언, 차별적 표현, 비방 등의 공격적 언행"),
    SCAM("SCAM", "사기 거래 시도, 결제 후 미발송, 허위 정보로 상대방을 기만하는 행위"),
    FAKE_ITEM("FAKE_ITEM", "레플리카가 아닌 다른 상품, 위조품, 혹은 잘못된 상품 정보 제공"),
    PRICE_GOUGING("PRICE_GOUGING", "터무니없이 높은 가격 책정, 시세를 악의적으로 조작하려는 시도"),
    SAFETY("SAFETY", "오프라인 거래 과정에서의 위협, 스토킹, 불법 행위"),
    PRIVACY("PRIVACY", "다른 사용자의 개인정보 무단 수집, 공유, 노출"),
    INAPPROPRIATE_CONTENT("INAPPROPRIATE_CONTENT", "음란물, 과도한 노출, 불쾌감을 줄 수 있는 이미지 또는 게시글"),
    OTHER("OTHER", "기타");

    private final String code;
    private final String label;
}
