package com.airfryer.repicka.domain.user;

import com.airfryer.repicka.domain.user.entity.Gender;
import com.airfryer.repicka.domain.user.entity.LoginMethod;
import com.airfryer.repicka.domain.user.entity.Role;
import com.airfryer.repicka.domain.user.entity.User;
import com.airfryer.repicka.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class UserTest
{
    @Autowired
    UserRepository userRepository;

    @Test
    @DisplayName("User 엔티티 생성 테스트")
    void createUser()
    {
        /// Given

        String email = "test@naver.com";
        String nickname = "Test";
        LoginMethod loginMethod = LoginMethod.GOOGLE;
        String oauthId = "0000000000";
        Role role = Role.USER;
        String profileImageUrl = "/프로필-이미지-기본경로";
        Boolean isKoreanUnivVerified = false;
        Gender gender = Gender.MALE;
        Integer height = 180;
        Integer weight = 70;
        String fcmToken = "fcmToken";
        int todayPostCount = 0;
        LocalDate lastAccessDate = LocalDate.now();

        /// When

        User user = User.builder()
                .email(email)
                .nickname(nickname)
                .loginMethod(loginMethod)
                .oauthId(oauthId)
                .role(role)
                .profileImageUrl(profileImageUrl)
                .isKoreaUnivVerified(isKoreanUnivVerified)
                .gender(gender)
                .height(height)
                .weight(weight)
                .fcmToken(fcmToken)
                .todayPostCount(todayPostCount)
                .lastAccessDate(lastAccessDate)
                .build();

        userRepository.save(user);

        /// Then

        User findUser = userRepository.findByOauthIdAndLoginMethod(oauthId, loginMethod).get();

        assertThat(findUser).isNotNull();
        assertThat(findUser.getEmail()).isEqualTo(email);
        assertThat(findUser.getNickname()).isEqualTo(nickname);
        assertThat(findUser.getLoginMethod()).isEqualTo(loginMethod);
        assertThat(findUser.getOauthId()).isEqualTo(oauthId);
        assertThat(findUser.getRole()).isEqualTo(role);
        assertThat(findUser.getProfileImageUrl()).isEqualTo(profileImageUrl);
        assertThat(findUser.getIsKoreaUnivVerified()).isEqualTo(isKoreanUnivVerified);
        assertThat(findUser.getGender()).isEqualTo(gender);
        assertThat(findUser.getHeight()).isEqualTo(height);
        assertThat(findUser.getWeight()).isEqualTo(weight);
        assertThat(findUser.getFcmToken()).isEqualTo(fcmToken);
        assertThat(findUser.getTodayPostCount()).isEqualTo(todayPostCount);
        assertThat(findUser.getLastAccessDate()).isEqualTo(lastAccessDate);
    }
}
