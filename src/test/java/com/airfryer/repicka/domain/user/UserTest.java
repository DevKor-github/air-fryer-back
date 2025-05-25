package com.airfryer.repicka.domain.user;

import com.airfryer.repicka.domain.user.entity.Gender;
import com.airfryer.repicka.domain.user.entity.LoginMethod;
import com.airfryer.repicka.domain.user.entity.Role;
import com.airfryer.repicka.domain.user.entity.User;
import com.airfryer.repicka.domain.user.repository.UserRepository;
import com.airfryer.repicka.util.CreateEntityUtil;
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
    @Autowired UserRepository userRepository;

    @Autowired CreateEntityUtil createEntityUtil;

    @Test
    @DisplayName("User 엔티티 생성 테스트")
    void createUser()
    {
        /// Given

        User user = createEntityUtil.createUser();

        /// Then

        User findUser = userRepository.findByOauthIdAndLoginMethod(user.getOauthId(), user.getLoginMethod()).orElse(null);

        assertThat(findUser).isNotNull();
        assertThat(findUser.getEmail()).isEqualTo(user.getEmail());
        assertThat(findUser.getNickname()).isEqualTo(user.getNickname());
        assertThat(findUser.getLoginMethod()).isEqualTo(user.getLoginMethod());
        assertThat(findUser.getOauthId()).isEqualTo(user.getOauthId());
        assertThat(findUser.getRole()).isEqualTo(user.getRole());
        assertThat(findUser.getProfileImageUrl()).isEqualTo(user.getProfileImageUrl());
        assertThat(findUser.getIsKoreaUnivVerified()).isEqualTo(user.getIsKoreaUnivVerified());
        assertThat(findUser.getGender()).isEqualTo(user.getGender());
        assertThat(findUser.getHeight()).isEqualTo(user.getHeight());
        assertThat(findUser.getWeight()).isEqualTo(user.getWeight());
        assertThat(findUser.getFcmToken()).isEqualTo(user.getFcmToken());
        assertThat(findUser.getTodayPostCount()).isEqualTo(user.getTodayPostCount());
        assertThat(findUser.getLastAccessDate()).isEqualTo(user.getLastAccessDate());
    }
}
