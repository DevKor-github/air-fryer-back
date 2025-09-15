package com.airfryer.repicka.domain.user.repository;

import com.airfryer.repicka.domain.user.entity.user.LoginMethod;
import com.airfryer.repicka.domain.user.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>
{
    // oauthId와 로그인 방식으로 사용자 찾기
    @Query("SELECT u FROM User u WHERE u.oauthId = :oauthId AND u.loginMethod = :loginMethod AND u.isDeleted = false")
    Optional<User> findByOauthIdAndLoginMethod(String oauthId, LoginMethod loginMethod);
}
